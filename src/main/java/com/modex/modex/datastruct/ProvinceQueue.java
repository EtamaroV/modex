package com.modex.modex.datastruct;

public class ProvinceQueue {
    public Province front;
    public Province rear;

    public ProvinceQueue() {
        front = null;
        rear = null;
    }

    public void enqueue(Province p) {
        if (this.front == null) {
            this.front = p;
            this.rear = p;
            return;
        }

        Province ptr = front;
        while (ptr.QNext != null) {
            if (p.distanceFormSource < ptr.QNext.distanceFormSource) {
                p.QNext = ptr.QNext;
                ptr.QNext = p;
                return;
            }
        }
        if (p == this.rear) {
            rear.QNext = p;
            rear = p;
        }

    }

    public Province dequeue() {
        Province p = front;
        front = front.QNext;
        return p;
    }

    public Province peek() {
        return front;
    }
}