package com.modex.modex.datastruct;

public class Queue<T> {
    private Node<T> first; // หัวแถว
    private Node<T> last;  // ท้ายแถว
    private int size;

    // คลาสโหนดสำหรับเก็บข้อมูลภายใน Queue
    private static class Node<T> {
        private T data;
        private Node<T> next;

        public Node(T data) {
            this.data = data;
        }
    }

    // เพิ่มข้อมูลเข้าท้ายแถว (Enqueue)
    public void enqueue(T item) {
        Node<T> oldLast = last;
        last = new Node<>(item);
        if (isEmpty()) {
            first = last;
        } else {
            oldLast.next = last;
        }
        size++;
    }

    // เอาข้อมูลออกจากหัวแถว (Dequeue)
    public T dequeue() {
        if (isEmpty()) return null;
        T data = first.data;
        first = first.next;
        size--;
        if (isEmpty()) last = null;
        return data;
    }

    public boolean isEmpty() {
        return first == null;
    }

    public int size() {
        return size;
    }
}