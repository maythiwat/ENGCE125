package com.maythiwat.engce125.lab3;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Parallelism {
    public static void main(String[] args) {
        int size = 100;
        int cores = Runtime.getRuntime().availableProcessors();
        // ExecutorService executor = Executors.newFixedThreadPool(cores);
        System.out.println(cores + " cores");

        int chunkSize = size / cores;
        int rm = size % cores;

        System.out.println(chunkSize);

        System.out.println(chunkSize * cores);
        System.out.println(rm);
        System.out.println(chunkSize * cores + rm);
    }
}
