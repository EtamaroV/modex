package com.modex.modex.datastruct;

public class Queue<T> { //โครงสร้างข้อมูลแบบคิว
    private Node<T> first; // ชี้ไปยังโหนดแรกสุด
    private Node<T> last; // ชี้ไปยังโหนดสุดท้าย
    private int size;  // จำนวนสมาชิกที่อยู่ในคิว


    public void enqueue(T item) { //เพิ่มข้อมูลใหม่เข้าสู่คิว
        Node<T> oldLast = last;  // เก็บตำแหน่งของคิวสุดท้าย
        last = new Node<>(item); // สร้างโหนดใหม่และกำหนดให้เป็นคิวสุดท้าย

        if (isEmpty()) { //คิวว่าง
            first = last;
        } else { //
            oldLast.next = last; //นำคิวใหม่ไปต่อท้าย
        }
        size++; // เพิ่มจำนวนสมาชิก
    } 


    public T dequeue() { // ลบโหนดแรกสุดออกจากคิว
        if (isEmpty()) return null; // ถ้าคิวว่างให้รีเทิน null
        T data = first.data; //เก็บคิวแรก
        first = first.next; //เลื่อนคิวไปยังคิวถัดไป
        size--;  // ลดจำนวนสมาชิก
        if (isEmpty()) last = null;  // หากลบข้อมูลออกไปแล้วทำให้คิวว่างเปล่า ต้องเคลียร์สุดท้าย (last) ให้เป็น null ด้วย
        return data;
    }

    public T peek() { //ดูข้อมูลที่อยู่หน้าสุดของคิว
        if (isEmpty()) return null;
        return first.data;
    }

    public boolean isEmpty() { //ตรวจสอบว่าคิวว่างเปล่าหรือไม่
        return first == null;
    }

    public int size() { //รีเทิร์นจำนวนสมาชิกทั้งหมดที่อยู่ในคิว
        return size;
    }


    private static class Node<T> { // คลาสภายในสำหรับสร้างโหนด
        private final T data;
        private Node<T> next;

        public Node(T data) {
            this.data = data;
        }
    }
}