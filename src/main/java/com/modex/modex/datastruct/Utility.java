package com.modex.modex.datastruct;

import com.modex.modex.mechanic.GameController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class Utility {


    public static List<Edge> dijkstra(Province From, Province To) {
        if (From == null || To == null || From.edges == null) return null;

        ProvinceQueue p = new ProvinceQueue();

        From.distanceFormSource = 0;
        p.enqueue(From);

        while (p.peek() != null) {
            Province ptr = p.dequeue();

            if (ptr == To) break;

            if (ptr.isVisited) continue;
            ptr.isVisited = true;

            if (ptr.edges == null) continue;

            for (Edge e : ptr.edges) {
                if (e.target.isVisited) continue;

                double newDistance = ptr.distanceFormSource + e.distance;

                if (newDistance < e.target.distanceFormSource) {
                    e.target.distanceFormSource = newDistance;
                    e.target.from = ptr;

                    p.enqueue(e.target);
                }
            }
        }

        List<Edge> path = new ArrayList<>();
        Province ptrPath = To;

        if (ptrPath.from == null && ptrPath != From) return null;

        while (ptrPath.from != null) {
            double edgeWeight = ptrPath.distanceFormSource - ptrPath.from.distanceFormSource;
            path.add(new Edge(ptrPath.from, ptrPath, edgeWeight));

            ptrPath = ptrPath.from;
        }

        Collections.reverse(path);
        return path;
    }

    public void resetVariable(GameController gc) {
        for (Province node : gc.getProvinceGraph().getNodes().values()) {
            node.distanceFormSource = 99999;
            node.from = null;
            node.isVisited = false;
            node.QNext = null;
        }
    }
}
