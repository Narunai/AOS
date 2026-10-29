# 🧪 02 - Phase 1: User-Space PoC Plan (แผนพัฒนาระยะที่ 1 แบบไม่ต้องคอมไพล์ OS)

> **เป้าหมายของ Phase 1 (เดือนที่ 1 – 3):** พิสูจน์แนวคิดหลัก (Core Value Proposition) ว่า *"เมื่อ AI มีความจำบริบทข้ามแอปย้อนหลังและเข้าใจโครงสร้างหน้าจอ มันสามารถเป็นผู้ช่วยที่ทำงานข้ามแอปแทนผู้ใช้ได้จริง"* โดยพัฒนาเป็นแอปพลิเคชัน Android พิเศษที่ใช้สิทธิ์ระดับสูงผ่าน **Accessibility API + Notification Listener + Shizuku (ADB Privilege)** ก่อนลงมือแก้ไข AOSP ในเฟสถัดไป

---

## 1. ทำไมต้องเริ่มที่ User-Space Prototype?

1. **Iteration Speed เร็วกว่า 20 เท่า:** การแก้โค้ด Kotlin บน Android Studio แล้วกด Run ลงเครื่องจริงใช้เวลา **5–10 วินาที** ในขณะที่การแก้โค้ด AOSP แล้วบิลด์ System Image ใหม่ใช้เวลา **15–90 นาที**
2. **ทดสอบ UX และ Prompt Engineering ได้ทันที:** เราสามารถปรับแต่งโครงสร้าง Compact DOM, ระบบค้นหาความจำ (Memory Retrieval), และหน้าตา Floating Overlay ได้ทุกวันจนกว่าจะเจอ Flow ที่ผู้ใช้ชอบที่สุด
3. **โค้ด 70% นำไปใช้ต่อใน Phase 2 ได้ทันที:** โมดูล `memory-engine`, `pii-filter`, `dom-pruner`, และ `agent-core` ที่เขียนใน Phase 1 สามารถย้ายเข้าไปเป็น System Privileged App ใน AOSP ของ Phase 2 ได้เลยโดยแทบไม่ต้องเขียนใหม่

---

## 2. Tech Stack สำหรับ Phase 1 (`d:\OS\poc-userspace`)

| หมวดหมู่ | เทคโนโลยี / ไลบรารีที่เลือกใช้ | เหตุผลในการเลือก |
| :--- | :--- | :--- |
| **ภาษาและ UI** | **Kotlin** + **Jetpack Compose** | พัฒนาเร็ว รองรับการวาด Floating Overlay (`WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY`) ได้ง่าย |
| **System Privilege Bridge** | **Shizuku API** (`rikka.shizuku`) | ให้สิทธิ์เทียบเท่า `shell` (UID 2000) หรือ `root` โดยไม่ต้องปลดล็อก Bootloader ช่วยให้อ่าน `dumpsys activity`, `dumpsys window` และสั่ง Input Injection ระดับระบบได้ |
| **UI & Event Capture** | `AccessibilityService` + `NotificationListenerService` + `UsageStatsManager` | ดักจับ View Hierarchy ของทุกแอป, ดักข้อความแจ้งเตือนขาเข้า และติดตามลำดับการเปิดแอป |
| **Local Database & Search** | **Room / SQLite FTS5** + **sqlite-vec** (หรือ **ObjectBox Vector Search**) | ค้นหาได้ทั้งคำค้นตรงตัว (Full-Text Search ภาษาไทย/อังกฤษ) และความหมายใกล้เคียง (Semantic Vector Search) บนมือถือโดยไม่ต้องพึ่ง Server |
| **On-Device AI / Embedding** | **MediaPipe Text Embedder** หรือ **ONNX Runtime Mobile** (`multilingual-e5-small`) | แปลงข้อความสรุปหน้าจอภาษาไทยและอังกฤษเป็น Vector ขนาด 384 มิติภายในเวลา < 30 ms บนมือถือ |
| **Agent Reasoning (Cloud/Local)** | **Gemini 2.5 Flash / Pro API** (ผ่าน `google-genai` / REST) + รองรับ Local Ollama/OpenRouter | ใช้โมเดลที่ฉลาดและเร็วในการวางแผน ReAct Loop ในช่วง PoC เพื่อพิสูจน์ผลลัพธ์สูงสุดก่อน |

---

## 3. รายละเอียดโมดูลที่จะสร้างใน Phase 1

### โมดูลที่ 1: Smart Screen & Event Collector (`:collector`)
* **Event Debouncer:**
  * ป้องกันไม่ให้ `TYPE_WINDOW_CONTENT_CHANGED` รัวเกินไป (เช่น ตอนผู้ใช้กำลังไถ Feed Facebook หรือ TikTok)
  * กฎการเก็บข้อมูล: บันทึก Snapshot เฉพาะเมื่อหน้าจอนิ่งเกิน **1.2 วินาที** หรือเมื่อเกิดเหตุการณ์สำคัญ (`TYPE_VIEW_CLICKED`, `TYPE_WINDOW_STATE_CHANGED`, หรือมีการสลับแอป)
* **DOM Pruner & Deduplicator:**
  * แปลง `AccessibilityNodeInfo` ทั้งหน้าจอเป็น JSON ขนาดเล็ก
  * คำนวณ **SimHash / Content Hash** ของข้อความบนหน้าจอ หากคล้ายกับ Snapshot ก่อนหน้าเกิน 85% (เช่น เลื่อนหน้าจอลงมาแค่เล็กน้อย) ให้ทำเป็นการ Merge แทนการสร้างแถวใหม่ใน Database

### โมดูลที่ 2: Privacy & Sensitive Shield (`:privacy-shield`)
* **Auto-Pause List:** หยุดดักจับทันทีเมื่อ `packageName` อยู่ในกลุ่มแอปธนาคาร (เช่น K PLUS, SCB EASY, Krungthai NEXT), แอปกระเป๋าเงินดิจิทัล, หรือหน้าตั้งค่ารหัสผ่าน
* **Password Field Check:** หาก `node.isPassword == true` ให้แทนที่ค่าด้วย `[REDACTED_PASSWORD]` เสมอในระดับโค้ดก่อนแตะต้องหน่วยความจำ
* **Regex PII Masker:** ปิดบังเลขบัตรประชาชน 13 หลัก, เลขบัตรเครดิต 16 หลัก, และรหัส OTP 6 หลักโดยอัตโนมัติ

### โมดูลที่ 3: Episodic Memory Engine (`:memory-engine`)
ออกแบบตารางฐานข้อมูลหลักใน SQLite ดังนี้:

```sql
-- ตารางเก็บประวัติบริบทตามลำดับเวลา (Episodic Snapshots)
CREATE TABLE episodic_snapshots (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    timestamp_ms INTEGER NOT NULL,
    package_name TEXT NOT NULL,
    app_label TEXT NOT NULL,
    activity_name TEXT,
    window_title TEXT,
    compact_dom_json TEXT NOT NULL,    -- โครงสร้างปุ่มและข้อความบนหน้าจอ
    extracted_text TEXT NOT NULL,      -- ข้อความล้วนสำหรับค้นหา
    ai_summary TEXT,                   -- สรุปสั้น 1 บรรทัดว่าผู้ใช้ทำอะไรในหน้านี้
    content_hash TEXT NOT NULL
);

-- ตาราง Full-Text Search (FTS5) สำหรับค้นหาคำสำคัญความเร็วสูง
CREATE VIRTUAL TABLE episodic_fts USING fts5(
    app_label,
    window_title,
    extracted_text,
    ai_summary,
    content='episodic_snapshots',
    content_rowid='id'
);
```

### โมดูลที่ 4: ReAct Agent & Action Executor (`:agent-core`)
เมื่อผู้ใช้กดปุ่ม Floating Orb แล้วพิมพ์หรือพูดคำสั่ง (เช่น *"เอาที่อยู่คลินิกที่เพื่อนส่งมาใน LINE เมื่อเช้า ไปเปิดหาเส้นทางใน Google Maps ให้หน่อย"*):
1. **Step 1 - Memory Retrieval:** ค้นหาใน `episodic_fts` และ Vector Index ด้วยคำว่า `"คลินิก"` + `package_name = jp.naver.line.android` ช่วงเวลาเช้าวันนี้
2. **Step 2 - Context Assembly:** ดึงข้อความที่อยู่คลินิกออกมาได้สำเร็จโดยไม่ต้องเปิดแอป LINE ซ้ำ
3. **Step 3 - Action Execution:**
   * ทางเลือกที่เร็วที่สุด (Fast Path): สร้าง Android `Intent` (`geo:0,0?q=...`) เพื่อเปิด Google Maps ไปยังพิกัดนั้นทันที
   * ทางเลือก UI Automation (UI Path): หากแอปเป้าหมายไม่มี DeepLink ให้ใช้ `AccessibilityNodeInfo.performAction(ACTION_CLICK)` หรือ `dispatchGesture()` เพื่อกดปุ่มและกรอกข้อความแทนผู้ใช้ทีละขั้น พร้อมแสดงกรอบไฟวิ่ง (Visual Feedback) ว่า AI กำลังกดปุ่มไหนอยู่

---

## 4. แผนการทำงานรายสัปดาห์สำหรับ Phase 1 (12-Week Sprint Plan)

| สัปดาห์ | เป้าหมายหลัก (Sprint Goal) | สิ่งส่งมอบที่จับต้องได้ (Deliverables) |
| :--- | :--- | :--- |
| **Week 1–2** | โครงสร้างโปรเจกต์ + Collector ต้นแบบ | แอป Android ที่ขอสิทธิ์ Accessibility + Notification + Shizuku ได้ครบ และแสดง Compact DOM ของแอปที่เปิดอยู่บน Logcat แบบเรียลไทม์ |
| **Week 3–4** | DOM Pruner + Event Debouncer + PII Shield | ระบบกรองโหนดขยะ ลดขนาด JSON ลงเหลือ < 3KB ต่อหน้าจอ พร้อมระบบบล็อกแอปธนาคารและซ่อนรหัสผ่านอัตโนมัติ |
| **Week 5–6** | Episodic Memory DB (SQLite FTS5 + Vector) | ระบบบันทึก Timeline การใช้งานลงฐานข้อมูลในเครื่อง พร้อมหน้า UI **"Timeline Inspector"** ให้ผู้ใช้เลื่อนดูและค้นหาประวัติย้อนหลังของตัวเองได้ |
| **Week 7–8** | Hybrid AI Summarizer & Memory Q&A | เชื่อมต่อโมเดลสรุปบริบทและระบบ Chat Q&A ที่ถามตอบข้อมูลข้ามแอปย้อนหลัง 7 วันได้แม่นยำ (สำเร็จ **Killer Use Case #1**) |
| **Week 9–10** | Action Executor & ReAct Loop | ระบบสั่งคลิก พิมพ์ เลื่อนหน้าจอ และยิง Intent ข้ามแอป พร้อมระบบ **"Confirm Before Sensitive Action"** (ถามก่อนกดปุ่มจ่ายเงิน/ส่งข้อความ) (สำเร็จ **Killer Use Case #2**) |
| **Week 11** | Floating Co-pilot Overlay UI | หน้าต่างลอยขนาดเล็ก (Dynamic Pill) ที่เรียกใช้ได้จากทุกหน้าจอ รองรับคำสั่งเสียงและแสดงสถานะการทำงานของ Agent แบบสวยงาม |
| **Week 12** | Benchmark, Polish & Demo Video | ทดสอบอัตราการกินแบตเตอรี่ (< 4% ต่อวัน), วัดความแม่นยำในการทำภารกิจ 20 โจทย์ และอัดวิดีโอ Demo 3 นาทีสำหรับเผยแพร่และหาแนวร่วม |

---

## 5. เกณฑ์วัดความสำเร็จก่อนข้ามไป Phase 2 (Phase 1 Exit Criteria)

* [ ] **Token Efficiency:** ขนาดเฉลี่ยของ Compact DOM ต่อ 1 หน้าจอต้องไม่เกิน **1,000 Tokens**
* [ ] **Search Latency:** การค้นหาความจำย้อนหลัง 10,000 Snapshots ในเครื่องต้องใช้เวลาไม่เกิน **150 ms**
* [ ] **Battery Overhead:** รันเบื้องหลังต่อเนื่อง 8 ชั่วโมง กินแบตเตอรี่เพิ่มไม่เกิน **3–5%**
* [ ] **End-to-End Task Success:** ทดสอบภารกิจข้ามแอป 10 สถานการณ์จริง (เช่น LINE -> Calendar, Gmail -> Notes, Shopee -> Keep) สำเร็จอย่างน้อย **8 ใน 10 สถานการณ์**

