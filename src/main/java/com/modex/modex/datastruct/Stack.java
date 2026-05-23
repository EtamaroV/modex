package com.modex.modex.datastruct;

public class Stack<T> { //โครงสร้างข้อมูลแบบสแต็ก
    private Node<T> top; // ชี้ไปยังโหนดที่อยู่บนสุดของสแต็ก
    private int size; // จำนวนสมาชิกทั้งหมดที่อยู่ในสแต็กปัจจุบัน

    //เพิ่มข้อมูลใหม่เข้าสู่สแต็ก
    public void push(T item) {
        Node<T> oldTop = top;// เก็บโหนดที่อยู่บนสุดเดิมเอาไว้ก่อน
        top = new Node<>(item); // สร้างโหนดใหม่
        top.next = oldTop; //กำหนดให้เป็นตัวบนสุด (top) ตัวใหม่
        size++; // เพิ่มจำนวนสมาชิก
    }

    //ดึงข้อมูลที่อยู่บนสุดออกจากสแต็ก
    public T pop() {
        if (isEmpty()) return null;  // ถ้าสแต็กว่างให้คืนค่า null
        T data = top.data; // เก็บโหนดที่อยู่บนสุดเดิมเอาไว้ก่อน
        top = top.next; // เลื่อนตัวชี้ top ไปยังโหนดตัวถัดไป
        size--; // ลดจำนวนสมาชิก
        return data;
    }

    //ดูข้อมูลที่อยู่บนสุดของสแต็ก
    public T peek() {
        if (isEmpty()) return null;
        return top.data;
    }

    //ตรวจสอบว่าสแต็กว่างเปล่าหรือไม่
    public boolean isEmpty() {
        return top == null;
    }

    //รีเทิร์นจำนวนสมาชิกทั้งหมดที่อยู่ในคิว
    public int size() {
        return size;
    }

    // คลาสภายในสำหรับสร้างโหนด
    private static class Node<T> {
        private final T data;
        private Node<T> next;

        public Node(T data) {
            this.data = data;
        }
    }
}
