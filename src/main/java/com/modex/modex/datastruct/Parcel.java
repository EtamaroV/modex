package com.modex.modex.datastruct;

import java.util.List;

public class Parcel {//เอาไว้เก็บข้อมูลพัสดุ 
    private final Province to; // จังหวัดปลายทาง (final เพราะปลายทางไม่ควรเปลี่ยนกลางทาง)
    private Province from; // จังหวัดต้นทาง
    private Province currentProvince; // จังหวัดที่พัสดุอยู่ ณ ปัจจุบัน
    private List<Edge> path; // รายการเส้นทาง (Edge/ถนน) ที่พัสดุต้องเดินทางผ่าน
    private int reward; // รายได้/รางวัลจากการส่งพัสดุชิ้นนี้

    // ข้อมูลเกี่ยวกับการเดินทาง
    private double estimatedArrivalTime; // เวลาที่คาดว่าจะส่งถึงปลายทาง
    private double distance_delivery; // ระยะทางในการจัดส่ง

    public Parcel(Province From, Province To) {// init
        from = From;
        to = To;
        currentProvince = From;
        path = null;
        reward = 0;
    }

    // Getters และ Setters สำหรับเข้าถึงและแก้ไขข้อมูล
    public Province getFrom() {
        return from;
    }


    public void setFrom(Province from) {
        this.from = from;
    }

    public Province getTo() {
        return to;
    }

    public Province getCurrentProvince() {
        return currentProvince;
    }
    
    public void setCurrentProvince(Province currentProvince) { //อัปเดตตำแหน่งปัจจุบันของพัสดุเมื่อเดินทางไปถึงจังหวัดใหม่
        this.currentProvince = currentProvince;
    }

    public List<Edge> getPath() {
        return path;
    }

    public void setPath(List<Edge> path) { // กำหนดเส้นทางการเดินทางของพัสดุ
        this.path = path;
    }

    public int getReward() {
        return reward;
    }

    public void setReward(int reward) { // กำหนดรายได้/รางวัลการส่งพัสดุชินนี้
        this.reward = reward;
    }

    public double getDistanceDelivery() {
        return distance_delivery;
    }


    public void setDistanceDelivery(double distance) {
        this.distance_delivery = distance;
    }

    public double getEstimatedArrivalTime() {
        return estimatedArrivalTime;
    }

    public void setEstimatedArrivalTime(double time) {
        this.estimatedArrivalTime = time;
    }
}
