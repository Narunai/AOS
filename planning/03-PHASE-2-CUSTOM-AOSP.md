# ⚙️ 03 - Phase 2: Custom AOSP / LineageOS Implementation Plan

> **เป้าหมายของ Phase 2 (เดือนที่ 4 – 9):** ยกระดับจาก User-space Prototype ใน Phase 1 ไปสู่ **Custom Mobile OS จริงบนฐาน LineageOS / AOSP (Android 15/16)** เพื่อปลดล็อกข้อจำกัดที่แอปทั่วไปทำไม่ได้ เช่น บริการที่ไม่ถูกระบบสั่งปิด (Zero Service Kill), การดึง View Tree ความเร็วสูงจาก `WindowManagerService` โดยไม่ติด `FLAG_SECURE` ในแอปที่ผู้ใช้ปลดล็อกเอง, และการทำ Read-only Database Indexing ผ่าน Native Daemon (`agentd`)

---

## 1. การเลือกฐาน OS และสภาพแวดล้อมการพัฒนา (Base OS & Dev Environment)

### 1.1 เลือก Fork จากอะไรดี?
* **ตัวเลือกหลัก (แนะนำ): LineageOS 22.x (Android 15)**
  * **ข้อดี:** มี Device Tree, Kernel และ Hardware Abstraction Layer (HAL) พร้อมใช้งานสำหรับมือถือจริงกว่าร้อยรุ่น (โดยเฉพาะตระกูล Google Pixel, Xiaomi, OnePlus, Nothing Phone) และมีโครงสร้างการปรับแต่งระบบที่สะอาดกว่า AOSP ดิบ
* **ตัวเลือกเสริมสำหรับการแจกจ่ายวงกว้าง: AOSP Generic System Image (GSI - Project Treble)**
  * **ข้อดี:** บิลด์ไฟล์ `system.img` เพียงไฟล์เดียว สามารถนำไปแฟลชทับลงมือถือ Android ยุคใหม่ที่รองรับ Project Treble และปลดล็อก Bootloader ได้ทันทีโดยไม่ต้องดูแล Device Tree รายรุ่น

### 1.2 สเปกเครื่องสำหรับบิลด์ AOSP และการจำลอง (Build & Test Setup)
เนื่องจากคุณทำงานบน Windows (`d:\OS`) แนวทางที่เร็วและคุ้มค่าที่สุดในการบิลด์ AOSP คือ:
1. **Local Dev & Emulator:**
   * ใช้ **WSL2 (Ubuntu 22.04/24.04 LTS)** หรือ Dual-boot Linux สำหรับแก้ไขโค้ดและรัน **Cuttlefish** (Android Virtual Device ระดับ OS ของ Google ที่จำลอง Binder, HAL และ SELinux ได้เหมือนเครื่องจริง 100%) หรือ **Waydroid**
2. **Build Server (ทางเลือกประหยัดเวลา):**
   * ต้องใช้พื้นที่ว่างอย่างน้อย **400 GB SSD (NVMe)** และ **RAM 32–64 GB**
   * หากคอมพิวเตอร์ส่วนตัวมีสเปกไม่ถึง สามารถเช่า Cloud Spot Instance (เช่น Hetzner / AWS EC2 `c7a.8xlarge` 32 vCPU / 64GB RAM) เฉพาะตอนสั่งคอมไพล์ใหญ่ แล้วใช้ `ccache` ช่วยลดเวลาบิลด์รอบถัดไปเหลือเพียง 5–10 นาที

---

## 2. แผนผังการแก้ไขซอร์สโค้ดใน AOSP Tree

```text
aosp-root/
├── frameworks/base/
│   ├── core/java/android/app/agent/
│   │   ├── IAgentContextManager.aidl          # [ใหม่] AIDL Interface หลักของ Agent Bus
│   │   ├── IAgentEventListener.aidl           # [ใหม่] Callback สำหรับส่ง Event ให้ Agent
│   │   ├── AgentContextSnapshot.java          # [ใหม่] Parcelable เก็บ Compact DOM
│   │   └── AgentManager.java                  # [ใหม่] System API สำหรับเรียกผ่าน context.getSystemService()
│   └── services/core/java/com/android/server/
│       ├── SystemServer.java                  # [แก้] ลงทะเบียน AgentContextManagerService ตอนบูต
│       ├── agent/
│       │   └── AgentContextManagerService.java # [ใหม่] บริการศูนย์กลางจัดการบริบทและสั่ง Action
│       └── wm/
│           ├── ActivityTaskManagerService.java # [แก้] เพิ่ม Hook แจ้งเตือนเมื่อสลับ Activity/Task
│           └── WindowManagerService.java       # [แก้] เพิ่ม Hook ดึง View Hierarchy ความเร็วสูง
├── system/agentd/                              # [ใหม่] Native C++ Daemon สำหรับ Read-only Data Index
│   ├── Android.bp
│   ├── agentd.rc                              # สคริปต์เริ่มต้นทำงานตั้งแต่บูตระบบ
│   ├── main.cpp
│   ├── WalSqliteReader.cpp                    # ตัวอ่าน SQLite แบบ Immutable/Read-only
│   └── InotifyWatcher.cpp                     # ตัวจับการเปลี่ยนแปลงไฟล์ใน /data/data (เฉพาะแอปที่อนุญาต)
├── system/sepolicy/                            # [แก้] เพิ่มกฎ SELinux ให้ agentd และ AgentService
│   └── private/
│       └── agentd.te
└── packages/apps/AgentSystemUI/                # [ใหม่] Privileged System App (ย้ายโค้ดจาก Phase 1 มาฝังในระบบ)
    ├── Android.bp                             # กำหนด privileged: true, certificate: "platform"
    └── src/...
```

---

## 3. รายละเอียดการพัฒนาเชิงลึกในแต่ละส่วนของ AOSP

### 3.1 การสร้าง `AgentContextManagerService` ใน `SystemServer`
1. สร้างคลาสสืบทอดจาก `com.android.server.SystemService` และ Implement `IAgentContextManager.Stub`
2. ใน `SystemServer.java` ช่วง `startOtherServices()` ให้เพิ่ม:
   ```java
   t.traceBegin("StartAgentContextManagerService");
   mSystemServiceManager.startService(AgentContextManagerService.class);
   t.traceEnd();
   ```
3. **ระบบป้องกันการเข้าถึงโดยไม่ได้รับอนุญาต (Custom Permission):**
   * ประกาศ Permission ระดับ `signature|privileged` ชื่อ `android.permission.ACCESS_AGENT_CONTEXT_BUS`
   * ทุกเมธอดใน `AgentContextManagerService` ต้องเรียก `mContext.enforceCallingOrSelfPermission(...)` เพื่อให้เฉพาะ System Agent หรือแอปที่ผู้ใช้กดอนุมัติผ่านระบบเท่านั้นที่เรียกใช้งานได้

### 3.2 การพัฒนา Native C++ Daemon (`agentd`)
ทำไมต้องแยก `agentd` ออกมาเป็น C++ Daemon แทนที่จะเขียนรวมใน `system_server`?
* ตามหลักความปลอดภัยของ Android **`system_server` ไม่ควรมีสิทธิ์อ่านไฟล์โดยตรงใน `/data/data/<app>`**
* การแยก `agentd` ออกมาช่วยให้เราจำกัดสิทธิ์ด้วย **SELinux Domain เฉพาะ (`u:r:agentd:s0`)** ที่มีสิทธิ์แบบ **Read-Only เท่านั้น (`r_file_perms`, `r_dir_perms`)** บน `app_data_file` โดยไม่มีสิทธิ์ `write` หรือ `unlink` เลยแม้แต่บิตเดียว!
* สื่อสารกับ `AgentContextManagerService` ผ่าน `libbinder_ndk`

### 3.3 การเขียนกฎ SELinux (`agentd.te`) อย่างปลอดภัย
ตัวอย่างโครงสร้างกฎ SELinux ที่ต้องเขียนใน Phase 2:

```te
# system/sepolicy/private/agentd.te
type agentd, domain;
type agentd_exec, system_file_type, exec_type, file_type;

init_daemon_domain(agentd)
binder_use(agentd)
binder_service(agentd)

# อนุญาตให้อ่านไฟล์ฐานข้อมูลใน app_data_file แบบ Read-Only เท่านั้น (ห้ามเขียนเด็ดขาด)
allow agentd app_data_file:dir r_dir_perms;
allow agentd app_data_file:file r_file_perms;

# ห้าม agentd เชื่อมต่ออินเทอร์เน็ตโดยตรง (ป้องกันข้อมูลรั่วไหลจากระดับ Daemon)
neverallow agentd { domain -system_server }:tcp_socket *;
neverallow agentd { domain -system_server }:udp_socket *;
```
> **จุดเด่นด้านความปลอดภัย:** การใส่ `neverallow` ไม่ให้ `agentd` ออกอินเทอร์เน็ตได้เอง เป็นจุดขายสำคัญที่จะทำให้นักพัฒนาและนักลงทุนมั่นใจว่า Daemon ที่อ่านข้อมูลระดับลึกไม่สามารถแอบส่งข้อมูลออกไปข้างนอกได้เอง

---

## 4. แผนการทำงานรายเดือนสำหรับ Phase 2 (Months 4 – 9)

| เดือนที่ | เป้าหมายหลัก (Milestone) | รายละเอียดงานทางเทคนิค |
| :--- | :--- | :--- |
| **Month 4** | **AOSP Build & Cuttlefish Setup** | ซิงค์ซอร์สโค้ด LineageOS/AOSP, ตั้งค่า `ccache`, บิลด์รอมสะอาดรันบน Cuttlefish Emulator สำเร็จ และฝังแอปจาก Phase 1 เข้าไปเป็น `priv-app` ในพาร์ทิชัน `/system_ext/priv-app` |
| **Month 5** | **Agent Bus (AIDL) & SystemServer** | เขียน `IAgentContextManager.aidl`, สร้าง `AgentContextManagerService`, ทดสอบส่งข้อมูลผ่าน Binder IPC ระหว่าง `system_server` กับ `AgentSystemUI` |
| **Month 6** | **WMS & ATMS Deep Hooks** | เชื่อมต่อ Hook ใน `ActivityTaskManagerService` และ `WindowManagerService` เพื่อดึง Compact DOM โดยตรงจากระบบ (เร็วขึ้น 5 เท่าและไม่ทำให้ UI ของแอปกระตุกเหมือนการใช้ Accessibility API ปกติ) |
| **Month 7** | **Native Daemon (`agentd`) & SELinux** | พัฒนา `agentd` ด้วย C++ สำหรับเฝ้าดูและอ่าน SQLite WAL แบบ Read-only พร้อมเขียนกฎ SELinux `agentd.te` และทดสอบความปลอดภัย (`neverallow` checks) |
| **Month 8** | **System-level Overlay & Hardware Key Trigger** | ผสาน UI ของ Agent เข้ากับ `SystemUI` (เช่น กดปุ่ม Power ค้าง 0.5 วินาที หรือปัดจากมุมจอเพื่อเรียก Agent Pill พร้อมเอฟเฟกต์เรืองแสงขอบจอเมื่อ AI กำลังทำงาน) |
| **Month 9** | **Device Porting (Pixel / GSI) & Alpha Test** | บิลด์เป็น GSI Image และรอมตรงรุ่นสำหรับ Google Pixel (เช่น Pixel 7/8) แฟลชลงเครื่องจริงเพื่อทดสอบใช้งานเป็นเครื่องหลัก (Daily Driver) นาน 2 สัปดาห์ |

---

## 5. เกณฑ์วัดความสำเร็จของ Phase 2 (Phase 2 Exit Criteria)

* [ ] บูต Custom OS บน Cuttlefish และมือถือเครื่องจริง (Pixel หรือเครื่องที่รองรับ GSI) ได้เสถียรโดยไม่มี System Crash (`Watchdog` ไม่ทำงานผิดปกติ)
* [ ] `sepolicy` คอมไพล์ผ่านแบบ **Enforcing Mode** (ห้ามปล่อยเป็น Permissive Mode เด็ดขาดในรุ่นแจกจ่าย)
* [ ] ความหน่วง (Latency) ในการดึงโครงสร้างหน้าจอผ่าน `IAgentContextManager` ต่ำกว่า **15 ms**
* [ ] สามารถเรียกใช้งาน Agent ได้ทันทีด้วยการกดปุ่มลัดของฮาร์ดแวร์ และสั่งทำงานข้าม 3 แอปต่อเนื่องได้สำเร็จ

