# 🚀 05 - Phase 3 & 4: Open-Source Strategy, SDK & Fundraising Plan

เอกสารนี้วางแผนกลยุทธ์ระยะที่ 3 และ 4 (เดือนที่ 10 – 18) เพื่อเปลี่ยนโปรเจกต์ทางเทคนิคให้กลายเป็น **Open-Source Ecosystem ที่มีชุมชนนักพัฒนาหนุนหลัง** และนำไปสู่ **การขอทุนให้เปล่า (Non-dilutive Grants) และการระดมทุนระดับ Pre-Seed / Seed Round**

---

## 1. Phase 3: กลยุทธ์ Open-Source & การสร้าง AgentOS SDK (เดือนที่ 10 – 13)

การทำ OS คนเดียวหรือทีมเล็กๆ จะชนะบริษัทใหญ่ได้ ต้องใช้พลังของ **Open-Source Community** และการวางตัวเป็น **"The Standard Agentic Layer for Android"**

### 1.1 โครงสร้างการเปิด Open-Source (Open-Core Model)
แบ่งซอร์สโค้ดออกเป็น 2 ส่วนชัดเจนเพื่อสร้างทั้งการยอมรับในชุมชนและความสามารถในการทำธุรกิจ:

| ส่วนประกอบ | สัญญาอนุญาต (License) | เหตุผลทางธุรกิจและชุมชน |
| :--- | :--- | :--- |
| **1. AgentOS Core Framework, `agentd`, Privacy Shield, & Client SDK** | **Apache License 2.0** | เปิดให้นักพัฒนา ชุมชน Custom ROM (เช่น LineageOS, GrapheneOS, PixelExperience) และผู้ผลิตฮาร์ดแวร์นำไปตรวจสอบความปลอดภัยและนำไปฝังในรอมของตัวเองได้ง่าย |
| **2. Cloud Memory Sync, Enterprise Policy Manager & Hosted Agent Orchestrator** | **Commercial / Proprietary (หรือ BSL)** | เป็นบริการเสริมฝั่ง Cloud และระบบจัดการสำหรับองค์กร ซึ่งเป็นแหล่งรายได้หลักของบริษัท |

### 1.2 การเปิดตัว `AgentOS SDK` สำหรับนักพัฒนาภายนอก
แทนที่เราจะเขียนความสามารถของ AI เองทั้งหมด เราจะเปิด SDK ให้นักพัฒนาทั่วโลกสร้าง **"Agent Skills / Plugins"** มาเสียบบน OS ของเราได้ภายใน 10 บรรทัด:

```kotlin
// ตัวอย่างโค้ดสำหรับนักพัฒนาภายนอกที่อยากสร้าง Agent Plugin บน OS ของเรา
val agentClient = AgentOS.getClient(context)

// 1. ค้นหาบริบทที่ผู้ใช้เพิ่งทำเมื่อเช้า (ระบบจัดการเรื่องสิทธิ์และ PII ให้เอง)
val memories = agentClient.memory.search(
    query = "ใบเสร็จค่าเดินทางหรือตั๋วเครื่องบิน",
    timeRange = TimeRange.lastDays(7)
)

// 2. สั่งให้ OS ช่วยทำงานข้ามแอปแบบปลอดภัย
agentClient.actions.executeGoal(
    goal = "นำยอดเงินจากใบเสร็จล่าสุดไปกรอกในแอปบันทึกค่าใช้จ่าย",
    requireUserConfirmation = true
)
```

### 1.3 กลยุทธ์สร้างกระแสเปิดตัวบน GitHub & Hacker News (Launch Playbook)
1. **ทำวิดีโอโชว์ของจริงแบบ Uncut (ห้ามตัดต่อหลอกเหมือนโปรเจกต์ที่เคยล้มเหลว):**
   * ถ่ายวิดีโอเห็นหน้าจอมือถือจริง วางคู่กับหน้าจอ Terminal ที่โชว์การทำงานของ Compact DOM Tree, การบล็อกรหัสผ่านแบบเรียลไทม์, และความเร็วในการค้นหาความจำย้อนหลัง
2. **ปล่อยให้ทดลองได้ใน 5 นาที (Low Friction Tryout):**
   * คนทั่วไปไม่กล้าแฟลชรอมลงมือถือหลักในวันแรก ดังนั้นต้องแจก **1) แอปรุ่น Shizuku (Phase 1)** ให้คนโหลดไปลองบนเครื่องเดิมได้ทันที และ **2) Docker / Waydroid Image สำเร็จรูป** ให้สาย Dev รันบนคอมพิวเตอร์ได้ในคำสั่งเดียว

---

## 2. Phase 4: แผนการขอทุนสนับสนุนและการระดมทุน (Fundraising & Grants)

### 2.1 แหล่งทุนให้เปล่า (Non-Dilutive Grants – สมัครได้ทันทีเมื่อจบ Phase 1–2)
ทุนเหล่านี้ไม่ต้องเสียหุ้นบริษัท และเหมาะมากกับโปรเจกต์ Open-Source ระดับ Infrastructure + Privacy:

| ชื่อกองทุน / โครงการ | วงเงินสนับสนุนโดยประมาณ | จุดเน้นที่ต้องเขียนในใบสมัคร |
| :--- | :--- | :--- |
| **NLnet Foundation (NGI Zero Core / Commons Fund)** | €5,000 – €50,000 (~2 แสน – 2 ล้านบาท) | เน้นเรื่อง Open-Source Operating System, Local-First Privacy AI และการคืนอำนาจการควบคุมข้อมูลให้ผู้ใช้ |
| **Mozilla Builders / Mozilla Ventures** | $10,000 – $100,000 | เน้นเรื่อง Local AI, On-device Privacy Agent ที่ไม่ผูกขาดกับบิ๊กเทค |
| **GitHub Secure Open Source Fund / Sovereign Tech Fund** | $10,000 – $50,000+ | เน้นโครงสร้างพื้นฐาน Open-Source ที่มีความปลอดภัยระดับระบบ (SELinux + Privacy Filter) |
| **ทุนนวัตกรรมในไทย (NIA Open Innovation / TED Fund)** | 500,000 – 1,500,000 บาท | เน้นการพัฒนา Deep Tech Software / AI Platform สำหรับอุปกรณ์พกพาและองค์กรในประเทศ |

### 2.2 Accelerator และ Venture Capital (Pre-Seed / Seed Round)
เมื่อมีตัวต้นแบบ Phase 2 + ยอด GitHub Stars (เป้าแรก 1,000 – 3,000 Stars):
* **Y Combinator (YC) / AI Grant (Nat Friedman & Daniel Gross) / HF0:** ชอบทีมเทคนิคที่กล้ามุดลงไปแก้ระดับ OS / Kernel / AOSP เพราะมีคู่แข่งน้อยมาก (High Technical Moat)
* **SEA / Regional Deep-Tech VCs:** เช่น 500 Global, Surge (Peak XV), Iterative, CyberAgent Capital

---

## 3. โมเดลรายได้ (Monetization & Business Models)

เพื่อให้ธุรกิจยั่งยืนโดยที่ตัว Base OS ยังคงเป็น Open-Source:

1. **B2C Freemium (AgentOS Cloud Pro - $9–$15/เดือน):**
   * *Free Tier (BYOK / Local):* ใช้ฟรี 100% ถ้ารันโมเดลในเครื่อง หรือใส่ API Key (Gemini/OpenAI/Claude) ของตัวเอง
   * *Pro Subscription:* บริการ Cloud AI Orchestrator สำเร็จรูป (รวมโควต้า LLM ชั้นนำ), ระบบสำรองความจำแบบ End-to-End Encrypted ข้ามอุปกรณ์ (มือถือเชื่อมกับคอมพิวเตอร์), และระบบสั่งงานระยะไกล
2. **B2B Enterprise / Field Workforce OS ($15–$30/เครื่อง/เดือน):**
   * สำหรับบริษัทโลจิสติกส์ ประกันภัย เซลล์ภาคสนาม หรือโรงพยาบาล ที่แจกแท็บเล็ต/มือถือให้พนักงาน
   * องค์กรต้องการให้มี AI คอยช่วยสรุปงาน กรอกฟอร์ม ERP/CRM อัตโนมัติจากหน้างาน โดยข้อมูลทั้งหมดถูกควบคุมไม่ให้รั่วไหลออกนอกเซิร์ฟเวอร์บริษัท (On-Premise / Private Cloud LLM)
3. **OEM & Specialty Hardware Licensing:**
   * จับมือกับผู้ผลิตมือถือรายย่อย (เช่น Fairphone, Nothing, ผู้ผลิตเครื่อง Handheld / Rugged Phone สำหรับงานอุตสาหกรรม) ที่ไม่มีทีม AI ขนาดใหญ่เป็นของตัวเอง ให้สามารถนำ AgentOS Layer ของเราไปฝังในรอมทางการของเครื่องรุ่นใหม่ได้ทันที

---

## 4. โครงสร้างสไลด์ Pitch Deck 10 หน้า (สำหรับคุยกับนักลงทุน)

1. **Title:** *Agentic Mobile OS – The Open System Layer for Autonomous Mobile AI*
2. **The Problem:** แอปมือถือถูกขังใน Sandbox ของยุคปี 2008 ทำให้ AI บนมือถือวันนี้เป็นได้แค่ "แชทบอทในกรง" ที่ไม่เข้าใจบริบทจริงของผู้ใช้
3. **Why Previous Attempts Failed:** ฮาร์ดแวร์แยกชิ้น (Humane/Rabbit) ล้มเหลวเพราะบังคับให้ผู้ใช้ทิ้งแอปเดิม ส่วนการใช้ Vision แคปหน้าจอตลอดเวลาก็ช้า เปลืองแบต และค่า Token แพงเกินจริง
4. **Our Solution:** AOSP/LineageOS-based Agentic Layer ที่ดึงโครงสร้างหน้าจอระดับ Framework (Compact DOM) + Read-only Native Indexing พร้อมรัน APK เดิมได้ 100%
5. **Live Demo (หัวใจสำคัญ):** วิดีโอโชว์ Killer Use Case ค้นหาความจำข้าม 3 แอปย้อนหลัง 7 วัน และสั่งทำงานอัตโนมัติใน 5 วินาที
6. **Privacy & Security Moat:** สถาปัตยกรรม 4-Stage PII Redaction + SELinux Read-only Daemon (`agentd`) ที่ปลอดภัยระดับ OS
7. **Traction & Community:** ผลการทดสอบ Benchmark (ประหยัด Token 30 เท่า), ยอด GitHub Stars, และจำนวนนักพัฒนาที่ทดลองใช้
8. **Business Model:** Open-Core (OS ฟรี + ขาย Cloud Orchestrator Pro & Enterprise Workforce Edition)
9. **Roadmap:** จาก User-space PoC -> Custom AOSP -> OEM & Enterprise Deployment
10. **Team & Ask:** แนะนำความเชี่ยวชาญของทีม และจำนวนเงินทุนที่ต้องการระดมทุนเพื่อขยายทีม Kernel/AOSP & AI Engineers

