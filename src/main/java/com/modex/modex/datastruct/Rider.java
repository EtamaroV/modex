package com.modex.modex.datastruct;

import com.modex.modex.view.UIControl;

import java.util.List;

public class Rider {
    // เปลี่ยนจาก List มาใช้ Queue ของคุณเอง
    public Queue<Parcel> parcelQueue;
    public Queue<Edge> pathQueue;
    
    public Province currentProvince;
    public double latestTime;


    public UIControl.TruckSprite truckSprite;
    public double edgeProgress = 0.0;
    public double edgeSpeed = 0.0;

    public Rider(List<Parcel> p, Province province) {
        this.currentProvince = province;

        // นำข้อมูลพัสดุจาก List ลง Queue ของเรา
        this.parcelQueue = new Queue<>();
        if (p != null) {
            for (Parcel parcel : p) {
                this.parcelQueue.enqueue(parcel);
            }
        }
        
        // สร้างคิวว่างสำหรับเก็บเส้นทาง
        this.pathQueue = new Queue<>();
    }

    public void removePackage() {
        // ใช้คำสั่ง dequeue() ของ Queue แทน removeFirst()
        if (parcelQueue != null && !parcelQueue.isEmpty()) {
            parcelQueue.dequeue();
        }
    }

    public void removeOldPath() {
        // ใช้คำสั่ง dequeue() ของ Queue แทน removeFirst()
        if (pathQueue != null && !pathQueue.isEmpty()) {
            pathQueue.dequeue();
        }
    }
}