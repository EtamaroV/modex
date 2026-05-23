package com.modex.modex.datastruct;

import java.util.ArrayList;
import java.util.List;

public class Province { //เอาไว้เก็บจังหวัด(Node)
    // ข้อมูลพื้นฐาน
    public int id; // รหัสประจำจังหวัด
    public String name; // ชื่อจังหวัด
    public double lat; // ละติจูด
    public double lon; // ลองจิจูด

    // สถานะของจังหวัดในเกม
    public boolean isStartNode = false; // เป็นจังหวัดเริ่มต้นของเกมหรือไม่
    public boolean isUnlocked = false; // จังหวัดนี้ถูกปลดล็อกหรือยัง
    public boolean isDrawn = false; // ถูกสร้าง/วาดลงบนหน้าจอ UI แล้วหรือไม่

    // ระบบก่อสร้างและการปลดล็อก
    public boolean isConstructing = false; // กำลังอยู่ในระหว่างการก่อสร้างหรือไม่
    public int constructionFinishHour = 0; // เวลา (ชั่วโมงในเกม) ที่การก่อสร้างจะเสร็จสิ้น

    public int unlockCost = 1000; // ค่าใช้จ่ายที่ต้องใช้เพื่อปลดล็อกจังหวัดนี้

    // โครงสร้างข้อมูลกราฟ
    public List<Edge> edges = new ArrayList<>(); // รายการเส้นทาง (ถนน) ที่เชื่อมต่อจากจังหวัดนี้ไปยังจังหวัดอื่น

    // ตัวแปรสำหรับหา shotest path
    public boolean isVisited = false; //เคยไปหรือยัง
    public double distanceFormSource = 99999; // ระยะทางที่สั้นที่สุดจากจุดเริ่มต้น
    public Province from = null;  // เก็บจังหวัดก่อนหน้า
    public Province QNext = null; //next node

    public Province(int id, String name, double lat, double lon) { //init
        this.id = id;
        this.name = name;
        this.lat = lat;
        this.lon = lon;
    }

    @Override 
    public String toString() { //รีเทิร์นชื่อจังหวัดเป็นสตริง
        return name;
    }

    @Override
    public boolean equals(Object o) { //เช็กว่า 2 จังหวัดใช่จังหวัดเดียวกันมั้ย
        if (this == o) return true;
        if (!(o instanceof Province that)) return false;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}