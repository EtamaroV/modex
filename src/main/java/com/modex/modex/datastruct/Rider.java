package com.modex.modex.datastruct;

import com.modex.modex.view.UIControl;

import java.util.ArrayList;
import java.util.List;

public class Rider {
    public List<Parcel> parcelList;
    public List<Edge> path;
    public Province currentProvince;
    public double latestTime;


    public UIControl.TruckSprite truckSprite;
    public double edgeProgress = 0.0;
    public double edgeSpeed = 0.0;

    public Rider(List<Parcel> p, Province province) {
        this.currentProvince = province;

        this.parcelList = new ArrayList<>(p);
        this.path = new ArrayList<>();
    }

    public void removePackage() {
        if (!parcelList.isEmpty()) {
            parcelList.removeFirst();
        }
    }

    public void removeOldPath() {
        if (!path.isEmpty()) {
            path.removeFirst();
        }
    }
}