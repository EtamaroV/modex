package com.modex.modex.datastruct;

public class Edge { // เอาไว้เก็บเส้นทาง (ถนน)
    public Province target; // ปลายทาง
    public Province source; // ต้นทาง
    public double distance; // ระยะทาง


    public Edge(Province source, Province target, double distance) { // init
        this.target = target;
        this.distance = distance;
        this.source = source;
    }


}