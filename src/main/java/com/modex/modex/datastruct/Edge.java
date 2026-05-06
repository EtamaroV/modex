package com.modex.modex.datastruct;

public class Edge {
    public Province target;
    public Province source;
    public double distance;


    public Edge(Province source,Province target, double distance) {
        this.target = target;
        this.distance = distance;
        this.source = source;
    }


}