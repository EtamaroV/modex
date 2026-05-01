package com.modex.modex.datastruct;

import java.util.*;

public class ProvinceNode {
    public int id;
    public String name;
    public double lat;
    public double lon;

    public List<Edge> edges = new ArrayList<>();

    public ProvinceNode(int id, String name, double lat, double lon) {
        this.id = id;
        this.name = name;
        this.lat = lat;
        this.lon = lon;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProvinceNode)) return false;
        ProvinceNode that = (ProvinceNode) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}