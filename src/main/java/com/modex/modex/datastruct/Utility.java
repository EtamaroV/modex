package com.modex.modex.datastruct;

import com.modex.modex.mechanic.GameController;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;


public class Utility {

    public static class ProvinceQueue{
        public Province front;
        public Province rear;

        public ProvinceQueue(){
            front = null;
            rear = null;
        }

        public void enqueue(Province p){
            if (this.front == null){
                this.front = p;
                this.rear = p;
                return;
            }

            Province ptr = front;
            while (ptr.QNext != null){
                if (p.distanceFormSource < ptr.QNext.distanceFormSource){
                    p.QNext = ptr.QNext;
                    ptr.QNext = p;
                    return;
                }
            }
            if (p == this.rear){
                rear.QNext = p;
                rear = p;
                return;
            }

        }

        public Province dequeue(){
            Province p = front;
            front = front.QNext;
            return p;
        }

        public Province peek(){
            return front;
        }
    }



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
