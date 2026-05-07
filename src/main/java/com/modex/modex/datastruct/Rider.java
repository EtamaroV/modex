package com.modex.modex.datastruct;

import java.util.ArrayList;
import java.util.List;
import com.modex.modex.view.UIControl; // นำเข้าเพื่อใช้กับ Sprite

public class Rider {
    public List<Parcel> parcelList;
    public List<Edge> path;
    public Province currentProvince;
    public double latestTime;

    // --- ตัวแปรสำหรับเชื่อมกับ UI และระบบ Animation ---
    public UIControl.TruckSprite truckSprite;
    public double edgeProgress = 0.0;
    public double edgeSpeed = 0.0;

    public Rider(List<Parcel> p, Province province){
        this.currentProvince = province;
        // ป้องกันการแก้ List ต้นทาง โดยการสร้าง List ใหม่ครอบไว้
        this.parcelList = new ArrayList<>(p);
        this.path = new ArrayList<>(); // แก้บั๊ก NullPointerException
    }

    public void assignParcel(){
        this.path.clear(); // ล้างเส้นทางเก่าเผื่อไว้
        Province tempStart = this.currentProvince; // รถเริ่มจากจุดที่อยู่ปัจจุบัน

        for (Parcel p : parcelList){
            // หาเส้นทางจากจุดที่รถอยู่ ไปยังเป้าหมายของพัสดุกล่องนั้น
            List<Edge> routeToNext = Utility.dijkstra(tempStart, p.getTo());

            if (routeToNext != null) {
                this.path.addAll(routeToNext); // นำเส้นทางถนนทั้งหมดมาต่อคิวกัน
                tempStart = p.getTo(); // 🌟 อัปเดตจุดสตาร์ทใหม่ เป็นปลายทางของกล่องนี้ เพื่อให้หากล่องถัดไปต่อได้
            } else {
                System.out.println("⚠️ หาเส้นทางไป " + p.getTo().name + " ไม่เจอ!");
            }
        }
    }

    public void removePackage(){
        if (!parcelList.isEmpty()) {
            parcelList.removeFirst();
        }
    }

    public void removeOldPath(){
        if (!path.isEmpty()) {
            path.removeFirst();
        }
    }
}