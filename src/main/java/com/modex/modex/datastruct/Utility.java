package com.modex.modex.datastruct;

import com.modex.modex.mechanic.GameController;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;


public class Utility {

    public static List<Edge> dijkstra(Province From, Province To){
        if (From == null || To == null) return null;
        if (From.edges == null) return null;
        ProvinceQueue p = new ProvinceQueue();
        From.distanceFormSource = 0;
        p.enqueue(From);
        while (p.peek() != null){
            Province ptr = p.dequeue();
            if (ptr == To) break;
            if (ptr.edges == null) continue;
            for (Edge e : ptr.edges){
                if (!e.target.isVisited){
                    e.target.distanceFormSource = e.source.distanceFormSource + e.distance;
                    e.target.isVisited = true;
                    e.target.from = e.source;
                    p.enqueue(e.target);
                }
                else if (e.target.distanceFormSource > e.source.distanceFormSource + e.distance){
                    e.target.distanceFormSource = e.source.distanceFormSource + e.distance;
                    e.target.from = e.source;
                }
            }
        }
        List<Edge> e = new ArrayList<>();
        Province ptr = From;
        while (ptr.from != null){
            e.add(new Edge(ptr.from, ptr, ptr.distanceFormSource = ptr.from.distanceFormSource));
        }
        Collections.reverse(e);

        return e;
    }

    public void resetVariable(GameController gc){
        for (Province node : gc.getProvinceGraph().getNodes().values()) {
            node.distanceFormSource = 99999;
            node.from = null;
            node.isVisited = false;
            node.QNext = null;
        }
    }
}
