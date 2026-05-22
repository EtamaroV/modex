package com.modex.modex.datastruct;

import java.util.ArrayList;
import java.util.List;

public class Province {
    public int id;
    public String name;
    public double lat;
    public double lon;

    public boolean isStartNode = false;
    public boolean isUnlocked = false;
    public boolean isDrawn = false;

    public boolean isConstructing = false;
    public int constructionFinishHour = 0;

    public int unlockCost = 1000;

    public List<Edge> edges = new ArrayList<>();


    public boolean isVisited = false;
    public double distanceFormSource = 99999;
    public Province from = null;
    public Province QNext = null;

    public Province(int id, String name, double lat, double lon) {
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
        if (!(o instanceof Province that)) return false;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}