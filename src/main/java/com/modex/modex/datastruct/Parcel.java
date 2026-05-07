package com.modex.modex.datastruct;

import java.util.List;

public class Parcel {
    private Province from;
    private Province to;
    private Province currentProvince;
    private List<Edge> path;
    private int reward;

    private double estimatedArrivalTime;
    private double distance_delivery;

    public Parcel(Province From,Province To){
        from = From;
        to = To;
        currentProvince = From;
        path = null;
        reward = 0;
    }

    public Province getFrom() {
        return from;
    }

    public Province getTo() {
        return to;
    }

    public Province getCurrentProvince() {
        return currentProvince;
    }

    public  List<Edge> getPath() {
        return path;
    }

    public int getReward() {
        return reward;
    }

    public void setCurrentProvince(Province currentProvince) {
        this.currentProvince = currentProvince;
    }

    public void setPath( List<Edge> path) {
        this.path = path;
    }

    public void setReward(int reward) {
        this.reward = reward;
    }

    // เพิ่มเข้าไปใน Class Parcel
    public void setFrom(Province from) {
        this.from = from;
    }

    // เพิ่มใน Parcel.java
    public void setDistanceDelivery(double distance) {
        this.distance_delivery = distance;
    }

    public double getDistanceDelivery() {
        return distance_delivery;
    }

    public void setEstimatedArrivalTime(double time) {
        this.estimatedArrivalTime = time;
    }

    public double getEstimatedArrivalTime() {
        return estimatedArrivalTime;
    }
}
