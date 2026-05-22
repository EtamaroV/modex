package com.modex.modex.datastruct;

public class Stack<T> {
    private Node<T> top;
    private int size;


    public void push(T item) {
        Node<T> oldTop = top;
        top = new Node<>(item);
        top.next = oldTop;
        size++;
    }


    public T pop() {
        if (isEmpty()) return null;
        T data = top.data;
        top = top.next;
        size--;
        return data;
    }


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


    private static class Node<T> {
        private final T data;
        private Node<T> next;

        public Node(T data) {
            this.data = data;
        }
    }
}
