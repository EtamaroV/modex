<p align="center">
  <img src="src/main/resources/images/ModEx_rmbg.png" alt="ModEx Logo" width="300">
</p>

<h1 align="center">ModEx 📦🚚</h1>

**ModEx** คือโปรแกรมเกมจำลองการขนส่งและจัดส่งพัสดุเชิงกลยุทธ์บนแผนที่ 77 จังหวัดของประเทศไทย พัฒนาขึ้นด้วยภาษา Java เพื่อประยุกต์ใช้ความรู้ด้านโครงสร้างข้อมูล (Data Structures) และอัลกอริทึมในการแก้ปัญหาการหาเส้นทางที่สั้นที่สุด 

โปรเจกต์นี้เป็นส่วนหนึ่งของวิชา **CPE112 Programming with Data Structures**

## 🔗 เอกสารที่เกี่ยวข้อง (Documents)
- 📄 **[รายงานโครงงาน (Project Report)](https://drive.google.com/file/d/1DnHvg6AtdBpnvZkFPXgmFS7fZzWMWN7A/view?usp=sharing)**
- 📊 **[สไลด์นำเสนอ (Presentation Slides)](https://canva.link/t4fmmfid5kkoetf)**

---

## ✨ ฟีเจอร์หลัก (Key Features)

- **ระบบจัดการพัสดุบนแผนที่ 77 จังหวัด:** จำลองพื้นที่การจัดส่งทั่วประเทศไทย โดยผู้เล่นสามารถรับพัสดุและกำหนดลำดับเป้าหมายการจัดส่งได้
- **Smart Routing (Dijkstra's Algorithm):** ประยุกต์ใช้โครงสร้างข้อมูลกราฟ (Graph) และอัลกอริทึม Dijkstra ในการคำนวณหาเส้นทางที่สั้นที่สุดตามลำดับการจัดส่ง
- **Game Mechanics:** ระบบกลไกการเล่นเกมที่ท้าทาย ประกอบด้วย 
  - การจำลองเวลาเดินในเกม (Tick System)
  - การจัดการเงินทุน (Fund Management)
  - โควตาการจัดส่งรายวัน
  - ระบบการปลดล็อกสาขาและพื้นที่ใหม่ๆ
- **Interactive GUI & Animation:** ส่วนติดต่อผู้ใช้งานแบบกราฟิกที่สวยงาม พร้อมระบบแอนิเมชันแสดงการเดินทางของพนักงานส่งของ (Rider) บนแผนที่แบบเรียลไทม์
- **Save & Load System:** ระบบบันทึกและโหลดสถานะการเล่นเกม รวมถึงข้อมูลแผนที่ตั้งต้น โดยจัดการผ่านไฟล์รูปแบบ `JSON`

## 🛠️ เทคโนโลยีที่ใช้ (Tech Stack)
- **Language:** Java
- **Game Engine / GUI:** JavaFX & FXGL
- **Data Storage:** JSON
- **Algorithms & Data Structures:** Graph, Dijkstra's Algorithm, Queue, Stack, List

---

## 🚀 วิธีการติดตั้งและใช้งาน (Getting Started)

### 🎮 สำหรับผู้ที่ต้องการเล่นเกม (For Players)

1. ไปที่หน้า **[Releases (Latest)](https://github.com/EtamaroV/modex/releases/latest)**
2. ดาวน์โหลดไฟล์ตัวเกมล่าสุด
3. ดับเบิลคลิกไฟล์เพื่อรันเกมได้เลย

### 💻 สำหรับการนำไปพัฒนาต่อ (For Developers)

1. **Clone repository**
   ```bash
   git clone https://github.com/EtamaroV/modex.git

    ```

2. **เปิดโปรเจกต์ใน IDE** ที่รองรับ Java (เช่น IntelliJ IDEA, Eclipse หรือ VS Code)
3. **Build และรันโปรแกรม** ผ่านไฟล์ `Launcher.java` หรือใช้คำสั่ง Maven:
    ```bash
    mvn clean javafx:run
    ```

*(หมายเหตุ: สำหรับนักพัฒนา จำเป็นต้องติดตั้ง Java JDK 21 ไว้ในเครื่อง)*

---

## 👥 ผู้จัดทำ (Contributors)

* นายกฤษติธี อัศวดิษฐเลิศ (68070501002)
* นายจักราวุธ เปรมศักดิ์เสถียร (68070501008)
* นายวรเมธ กิตติโชติรัตน์ (68070501045)
* นายอรรถเชษฐ์ อนันต์คูศรี (68070501059)
* นายปริญญา เส็มเส็น (68070501073)
* นายพัชรพล ธรรมมล (68070501077)
