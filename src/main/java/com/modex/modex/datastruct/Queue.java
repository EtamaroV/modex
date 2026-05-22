package com.modex.modex.datastruct;

public class Queue<T> {
    private Node<T> first;
    private Node<T> last;
    private int size;


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


    public T dequeue() {
        if (isEmpty()) return null;
        T data = first.data;
        first = first.next;
        size--;
        if (isEmpty()) last = null;
        return data;
    }

    public T peek() {
        if (isEmpty()) return null;
        return first.data;
    }

    public boolean isEmpty() {
        return first == null;
    }

    public int size() {
        return size;
    }


    private static class Node<T> {
        private final T data;
        private Node<T> next;

        public Node(T data) {
            this.data = data;
        }
    }
}