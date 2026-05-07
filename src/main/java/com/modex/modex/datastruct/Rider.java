package com.modex.modex.datastruct;

import java.util.List;

public class Rider {
    public List<Parcel> parcelList;
    public List<Edge> path;
    public Province currentProvince;
    public double latestTime;

    public Rider(List<Parcel> p, Province province){
        this.currentProvince = province;
        this.parcelList = p;
        this.path = null;
    }

    public void assignParcel(){
        Province temp = parcelList.getFirst().getFrom();
        for (Parcel p : parcelList){

            if (p == parcelList.get(0)){
                if (path.isEmpty()){
                    path = Utility.dijkstra(p.getFrom(),p.getTo());
                }
                else{
                    path.addAll(Utility.dijkstra(temp,p.getTo()));
                }
                temp = p.getTo();
            }
        }
    }



    public void removePackage(){
        parcelList.removeFirst();
    }

    public void removeOldPath(){
        path.removeFirst();
    }
}
