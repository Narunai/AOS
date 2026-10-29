# ✅ 06 - Master Task Tracker & Sprint Backlog (ตารางติดตามความคืบหน้าโครงการ)

ใช้ไฟล์นี้เป็นศูนย์กลางในการเช็คความคืบหน้าของโปรเจกต์ในระยะยาว โดยเปลี่ยนเครื่องหมาย `[ ]` เป็น `[x]` เมื่อทำงานแต่ละชิ้นเสร็จสมบูรณ์

---

## 🏁 Stage 0: Project Setup & Foundation (สัปดาห์ที่ 1)

- [x] สร้างโครงสร้างโฟลเดอร์แผนงานหลัก `d:\OS\planning`
- [x] กำหนดชื่อโค้ดเนมอย่างเป็นทางการของโปรเจกต์ (`Project Aether` / `AgentOS`)
- [x] ตั้งค่า Git Repository ใน `d:\OS` พร้อมไฟล์ `.gitignore`
- [x] ตรวจสอบสภาพแวดล้อมระบบ (JDK 17, Android SDK Platform 34/35/36, Gradle 8.11.1)
- [x] สร้างโครงสร้างโปรเจกต์ Android ต้นแบบแบบแยก 5 โมดูลใน `d:\OS\poc-userspace`

---

## 🧪 Phase 1: User-Space PoC (เดือนที่ 1 – 3)

### Sprint 1: Context & UI Collector (สัปดาห์ที่ 1 – 2)
- [x] สร้างโมดูล `:collector` และตั้งค่า `AccessibilityService` (`flagReportViewIds`, `flagRetrieveInteractiveWindows`)
- [x] ตั้งค่า `NotificationListenerService` เพื่อดักจับข้อความแจ้งเตือนขาเข้าแบบเรียลไทม์
- [x] เชื่อมต่อ **Shizuku SDK** เพื่อขอสิทธิ์ระดับ `shell` (UID 2000) สำหรับดึงข้อมูล `dumpsys activity top`
- [x] เขียนตัวแปลง `AccessibilityNodeInfo` ให้กลายเป็น **Compact DOM JSON** (ตัดโหนดว่างออก)

### Sprint 2: Debouncer, Deduplication & Privacy Shield (สัปดาห์ที่ 3 – 4)
- [x] เขียนระบบ **Event Debouncer** (หน่วงเวลา 1.2 วินาทีเมื่อมีการเลื่อนหน้าจอ ก่อนเก็บ Snapshot)
- [x] เขียนอัลกอริทึม **SimHash / Content Hash** (64-bit Hamming distance) เพื่อป้องกันการบันทึกหน้าจอเดิมซ้ำซ้อน
- [x] สร้างโมดูล `:privacy-shield`:
  - [x] บล็อกการเก็บข้อมูลอัตโนมัติเมื่อตรวจพบ `InputType` ประเภท Password/PIN
  - [x] สร้างบัญชีดำ (Blacklist) แอปธนาคารและแอปการเงินในไทยและสากล (`BankingPackageRegistry`)
  - [x] เขียน Regex ปิดบังเลขบัตรประชาชน 13 หลัก (พร้อม Checksum), บัตรเครดิต 16 หลัก, และรหัส OTP (`PiiRegexMasker`)
  - [x] สร้างระบบ Guardrail ตรวจจับปุ่มความเสี่ยงสูง (`ActionGuardrail`) เพื่อบังคับขอสิทธิ์ยืนยันก่อนกดจริง

### Sprint 3: Episodic Memory Engine (สัปดาห์ที่ 5 – 6)
- [x] สร้างโมดูล `:memory-engine` ด้วย **SQLite + FTS4/5** (`EpisodicSnapshotEntity`, `EpisodicDao`, `AgentDatabase`)
- [x] ผสานรวมอัลกอริทึม **SimHash Deduplicator** ข้ามการบันทึกหน้าจอที่คล้ายเดิมเกิน 85%
- [x] สร้างหน้า UI **"Memory Timeline Inspector"** ให้ผู้ใช้เปิดดูประวัติบริบทที่ระบบบันทึกไว้ ค้นหาคำสำคัญ และกดลบรายการได้

### Sprint 4: Hybrid AI & Cross-App Memory Recall (สัปดาห์ที่ 7 – 8)
- [x] ออกแบบโครงสร้าง Prompt Template สำหรับทำ Cross-App Memory Recall (`PromptTemplates.buildMemoryRecallPrompt`)
- [x] พัฒนาระบบค้นหาความจำแบบ Full-Text Search (FTS) และผสานรวมเข้ากับหน้า Co-pilot Q&A
- [x] เชื่อมต่อ Live Gemini 2.5 Flash Client (`GeminiClient.kt`) พร้อมกล่องตั้งค่า API Key แบบ BYOK

### Sprint 5: Action Executor & ReAct Automation (สัปดาห์ที่ 9 – 10)
- [x] สร้างระบบสั่งการกระทำ (`ActionExecutor`):
  - [x] `clickNode(nodeId)` / `clickCoordinates(x, y)`
  - [x] `inputText(nodeId, text)`
  - [x] `scroll(direction)`
  - [x] `launchIntent(uri / packageName)`
- [x] สร้างระบบความปลอดภัย **"Confirm Sensitive Action"** เมื่อปุ่มมีคำสำคัญเสี่ยง
- [x] เชื่อมต่อ Action Executor เข้ากับ Floating Overlay และ Gesture Dispatcher

### Sprint 6: Floating Co-pilot UI & PoC Demo (สัปดาห์ที่ 11 – 12)
- [x] ออกแบบและพัฒนา **Floating Agent Pill (`FloatingAgentService`)** ที่เรียกใช้ได้จากทุกหน้าจอ
- [x] สร้างหน้าต่างควบคุมแอปหลักด้วย Jetpack Compose (`MainActivity`, `HomeScreen`, `TimelineInspectorScreen`, `PrivacyDashboardScreen`)
- [x] สร้างและผ่านชุด Unit Tests 100% (`PiiRegexMaskerTest`, `SimHashDeduplicatorTest`)
- [x] คอมไพล์และสร้างไฟล์ติดตั้ง `app-debug.apk` (54.7 MB) สำเร็จสมบูรณ์

---

## ⚙️ Phase 2: Custom AOSP / LineageOS Alpha (เดือนที่ 4 – 9)

### Milestone 2.1: AOSP Build Environment & Emulator (เดือนที่ 4)
- [x] สร้างสคริปต์ `tools/apply-aosp-patches.sh` สำหรับติดตั้ง Patches ลง AOSP / LineageOS อัตโนมัติ
- [x] สร้างสคริปต์ `tools/cuttlefish/launch-cuttlefish.sh` สำหรับทดสอบบน Cuttlefish WebRTC Emulator
- [x] สร้าง Blueprint ฝังแอปเป็น Privileged System App (`packages/apps/AgentSystemUI/Android.bp` และ `privapp-permissions-agent.xml`)

### Milestone 2.2: Framework Hooks & Agent Bus (เดือนที่ 5 – 6)
- [x] สร้าง `IAgentContextManager.aidl` และ `IAgentEventListener.aidl` ใน `frameworks/base`
- [x] สร้าง Parcelable Models: `AgentContextSnapshot.java`, `AgentActionRequest.java`, `AgentManager.java`
- [x] พัฒนา `AgentContextManagerService.java` และสร้าง Patch ลงทะเบียนใน `SystemServer.java`
- [x] สร้าง Patch เพิ่ม Hook ใน `ActivityTaskManagerService` เพื่อดักจับการสลับแอปแบบเรียลไทม์
- [x] สร้าง Patch เพิ่ม Hook ใน `WindowManagerService` เพื่อดึงโครงสร้างหน้าจอความเร็วสูงโดยตรงในระดับ OS

### Milestone 2.3: Native Daemon (`agentd`) & SELinux (เดือนที่ 7)
- [x] พัฒนา Native C++ Daemon (`system_agentd`: `Android.bp`, `agentd.rc`, `main.cpp`)
- [x] เขียน `WalSqliteReader.cpp` สำหรับอ่าน SQLite WAL แบบ Strict Read-Only (`immutable=1` และ `PRAGMA query_only = ON`)
- [x] เขียน `InotifyWatcher.cpp` สำหรับเฝ้าดูการเปลี่ยนแปลงของไฟล์ฐานข้อมูลใน `/data/data/`
- [x] เขียนกฎ SELinux (`agentd.te`, `service.te`, `service_contexts`, `system_server.te`) พร้อมกฎ `neverallow` ป้องกัน Daemon ออกเน็ต

### Milestone 2.4: Hardware Porting & Daily Driver Test (เดือนที่ 8 – 9)
- [ ] ผสานรวม **microG** เพื่อให้รองรับแจ้งเตือน Push Notification และ Google Maps API
- [ ] บิลด์ Generic System Image (GSI) และรอมเฉพาะรุ่นสำหรับ Google Pixel
- [ ] ทดสอบใช้งานจริงในชีวิตประจำวันเป็นเวลา 14 วัน และปรับจูนการใช้พลังงาน

---

## 🌍 Phase 3 & 4: Open-Source, SDK & Fundraising (เดือนที่ 10 – 18)

- [ ] แยกโมดูล `AgentOS SDK` ออกเป็นไลบรารีสำหรับนักพัฒนาภายนอก
- [ ] เขียนเอกสารประกอบสำหรับนักพัฒนา (Architecture Docs, Security Whitepaper, SDK Quickstart)
- [ ] เปิดตัว Public Repository บน GitHub พร้อมปล่อย Waydroid Image และ Pixel ROM รุ่นทดลอง
- [ ] ยื่นขอทุนให้เปล่า (NLnet Foundation, Mozilla Builders, NIA)
- [ ] จัดทำ Pitch Deck 10 หน้า และเริ่มพูดคุยกับ Accelerator / Deep-Tech VCs

