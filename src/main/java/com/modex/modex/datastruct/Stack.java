package com.modex.modex.datastruct;

public class Stack<T> {
    private Node<T> top; // โหนดบนสุด
    private int size;

    // คลาสโหนดสำหรับเก็บข้อมูลภายใน Stack
    private static class Node<T> {
        private T data;
        private Node<T> next;

        public Node(T data) {
            this.data = data;
        }
    }

    // เพิ่มข้อมูลลงบนสุดของ Stack (Push)
    public void push(T item) {
        Node<T> oldTop = top;
        top = new Node<>(item);
        top.next = oldTop;
        size++;
    }

    // เอาข้อมูลบนสุดออกจาก Stack (Pop)
    public T pop() {
        if (isEmpty()) return null;
        T data = top.data;
        top = top.next;
        size--;
        return data;
    }

    // ขอดูข้อมูลบนสุดแต่ไม่เอาออก (Peek)
    public T peek() {
        if (isEmpty()) return null;
        return top.data;
    }

    public boolean isEmpty() {
        return top == null;
    }

    public int size() {
        return size;
    }
}
