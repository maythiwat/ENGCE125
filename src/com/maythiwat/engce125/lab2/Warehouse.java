package com.maythiwat.engce125.lab2;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

public class Warehouse {
    private final LinkedList<String> stock;
    private static final AtomicInteger seqId = new AtomicInteger();

    public Warehouse() {
        this.stock = new LinkedList<>();
    }

    public synchronized void put(String item) throws InterruptedException {
        stock.addLast(item);
        System.out.println("[Stock] PUT  -> " + item + " (size=" + size() + ") by " + Thread.currentThread().getName());
        notifyAll();
    }

    public synchronized void take() throws InterruptedException {
        while (stock.isEmpty()) {
            System.out.println("[Stock] WAIT -> ** " + Thread.currentThread().getName() + " is waiting for stock **");
            wait();
        }
        String item = stock.removeFirst();
        System.out.println("[Stock] TAKE -> " + item + " (size=" + size() + ") by " + Thread.currentThread().getName());
        notifyAll();
    }

    public synchronized int size() {
        return stock.size();
    }

    public synchronized int getNextId() {
        return seqId.incrementAndGet();
    }
}
