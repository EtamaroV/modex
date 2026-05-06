package com.modex.modex.datastruct;

public class LinkedList<T> {
    private Node<T> head; // โหนดแรกของรายการ
    private int size;

    // คลาสโหนดสำหรับเก็บข้อมูลและที่อยู่ของโหนดถัดไป
    private static class Node<T> {
        private T data;
        private Node<T> next;

        public Node(T data) {
            this.data = data;
        }
    }

    // เพิ่มข้อมูลต่อท้ายรายการ (Add Last)
    public void add(T item) {
        Node<T> newNode = new Node<>(item);
        if (head == null) {
            head = newNode;
        } else {
            Node<T> current = head;
            while (current.next != null) {
                current = current.next;
            }
            current.next = newNode;
        }
        size++;
    }

    // ลบข้อมูลโดยระบุตำแหน่ง (Remove by Index)
    public void remove(int index) {
        if (index < 0 || index >= size || head == null) return;

        if (index == 0) {
            head = head.next;
        } else {
            Node<T> current = head;
            for (int i = 0; i < index - 1; i++) {
                current = current.next;
            }
            current.next = current.next.next;
        }
        size--;
    }

    // ดึงข้อมูลตามตำแหน่ง (Get by Index)
    public T get(int index) {
        if (index < 0 || index >= size) return null;
        Node<T> current = head;
        for (int i = 0; i < index; i++) {
            current = current.next;
        }
        return current.data;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return head == null;
    }
}