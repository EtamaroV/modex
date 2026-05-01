package com.modex.modex.datastruct;

public class Edge {
    public ProvinceNode target;
    public double distance;

    public Edge(ProvinceNode target, double distance) {
        this.target = target;
        this.distance = distance;
    }
}