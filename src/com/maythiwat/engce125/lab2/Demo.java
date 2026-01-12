package com.maythiwat.engce125.lab2;

import java.util.Random;

public class Demo {
    public static void main(String[] args) {
        Warehouse wh = new Warehouse();
        Random rand = new Random();

        Thread p1 = new Thread(() -> {
            try {
                // 2 batches, 5 items per batch
                for (int j = 0; j < 2; j++) {
                    for (int i = 0; i < 5; i++) {
                        wh.put("item-" + String.format("%03d", wh.getNextId()));
                    }
                    Thread.sleep(rand.nextInt(100, 1000));
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "P1");

//        Thread p2 = new Thread(() -> {
//            try {
//                // 2 batches, 5 items per batch
//                for (int j = 0; j < 2; j++) {
//                    for (int i = 0; i < 5; i++) {
//                        wh.put("item-" + String.format("%03d", wh.getNextId()));
//                    }
//                    Thread.sleep(rand.nextInt(100, 1000));
//                }
//            } catch (InterruptedException e) {
//                Thread.currentThread().interrupt();
//            }
//        }, "P2");

        // 2 consumers randomly taking stock
        Thread c1 = new Thread(() -> consumerFunction(wh), "C1");
        Thread c2 = new Thread(() -> consumerFunction(wh), "C2");
        Thread c3 = new Thread(() -> consumerFunction(wh), "C3");

        p1.start(); // p2.start();
        c1.start(); c2.start(); c3.start();
    }

    static void consumerFunction(Warehouse wh) {
        System.out.println("Thread Spawn: " + Thread.currentThread().getName());
        Random rand = new Random();
        try {
            while (true) {
                wh.take();
                Thread.sleep(rand.nextInt(100, 200));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println("Thread Exit: " + Thread.currentThread().getName());
    }
}
