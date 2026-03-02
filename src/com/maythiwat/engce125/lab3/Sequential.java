package com.maythiwat.engce125.lab3;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Sequential {
    public static void main(String[] args) {
        int[] sizes = {100, 1000, 10000, 100000, 1000000};

        for (int size : sizes) {
            System.out.println("\n--- Data Size: " + size + " ---");
            int[] numbers = randData(size);

            System.out.println("[Sequential Approach]");
            seqRun(numbers.clone());

            System.out.println("\n[Parallel Approach]");
            paraRun(numbers.clone());
        }
    }

    public static int[] randData(int size) {
        Random rand = new Random();
        int[] numbers = new int[size];
        for (int i = 0; i < size; i++) {
            numbers[i] = rand.nextInt(1, 100);
        }
        return numbers;
    }

    public static void seqRun(int[] numbers) {
        seqSort(numbers);
        seqStats(numbers);
    }

    public static void paraRun(int[] numbers) {
        paraSort(numbers.clone());
        paraStats(numbers.clone());
    }

    public static void seqSort(int[] numbers) {
        long tStart = System.nanoTime();
        Arrays.sort(numbers);
        System.out.println("Sequential Sort: " + (System.nanoTime() - tStart) / 1000 + " Microseconds");
        // System.out.println(Arrays.toString(numbers));
    }

    public static void seqStats(int[] numbers) {
        long tStart = System.nanoTime();

        int min = numbers[0];
        for (int i = 1; i < numbers.length; i++) {
            if (numbers[i] < min) min = numbers[i];
        }

        int max = numbers[0];
        for (int i = 1; i < numbers.length; i++) {
            if (numbers[i] > max) max = numbers[i];
        }

        long sum = 0;
        for (int number : numbers) {
            sum += number;
        }
        double avg = (double) sum / numbers.length;

        double median;
        if (numbers.length % 2 == 1) {
            median = numbers[numbers.length / 2];
        } else {
            median = (numbers[(numbers.length / 2) - 1] + numbers[numbers.length / 2]) / 2.0;
        }

        System.out.println("Sequential Stats: " + (System.nanoTime() - tStart) / 1000 + " Microseconds");
        // System.out.println("  Min=" + min + " Max=" + max + " Avg=" + String.format("%.2f", avg) + " Median=" + median);
    }

    public static void paraSort(int[] numbers) {
        int cores = Runtime.getRuntime().availableProcessors();
        int chunkSize = numbers.length / cores;
        int[][] chunks = new int[cores][];

        for (int i = 0; i < cores; i++) {
            int start = i * chunkSize;
            int end = (i == cores - 1) ? numbers.length : (i + 1) * chunkSize;
            chunks[i] = Arrays.copyOfRange(numbers, start, end);
        }

        ExecutorService executor = Executors.newFixedThreadPool(cores);
        long tStart = System.nanoTime();

        for (int i = 0; i < cores; i++) {
            int idx = i;
            executor.submit(() -> Arrays.sort(chunks[idx]));
        }

        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Data Parallel Sort: " + (System.nanoTime() - tStart) / 1000 + " Microseconds");
        // System.out.println(Arrays.toString(numbers));
    }

    public static void paraStats(int[] numbers) {
        int cores = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(cores);
        long tStart = System.nanoTime();

        executor.submit(() -> {
            int min = numbers[0];
            for (int n : numbers) if (n < min) min = n;
            // System.out.println("  " + Thread.currentThread().getName() + " Min=" + min);
        });

        executor.submit(() -> {
            int max = numbers[0];
            for (int n : numbers) if (n > max) max = n;
            // System.out.println("  " + Thread.currentThread().getName() + " Max=" + max);
        });

        executor.submit(() -> {
            long sum = 0;
            for (int n : numbers) sum += n;
            double avg = (double) sum / numbers.length;
            // System.out.println("  " + Thread.currentThread().getName() + " Avg=" + String.format("%.2f", avg));
        });

        executor.submit(() -> {
            int[] sorted = numbers.clone();
            Arrays.sort(sorted);
            double median = (sorted.length % 2 == 1)
                    ? sorted[sorted.length / 2]
                    : (sorted[(sorted.length / 2) - 1] + sorted[sorted.length / 2]) / 2.0;
            // System.out.println("  " + Thread.currentThread().getName() + " Median=" + median);
        });

        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Task Parallel Stats: " + (System.nanoTime() - tStart) / 1000 + " Microseconds");
    }
}