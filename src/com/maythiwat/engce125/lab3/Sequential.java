package com.maythiwat.engce125.lab3;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.swing.SwingUtilities;

public class Sequential {

    static int[] sizes = {100, 1_000, 10_000, 100_000, 1000000, 10_000_000};
    // [sizeIndex][0=seqSort, 1=seqStats, 2=paraSort, 3=paraStats]
    static long[][] results = new long[sizes.length][4];

    public static void main(String[] args) {
        for (int s = 0; s < sizes.length; s++) {
            int size = sizes[s];
            System.out.println("\n--- Data Size: " + size + " ---");
            int[] numbers = randData(size);

            System.out.println("[Sequential Approach]");
            int[] x = numbers.clone();
            results[s][0] = seqSort(x);
            results[s][1] = seqStats(x);

            System.out.println("\n[Parallel Approach]");
            int[] y = numbers.clone();
            results[s][2] = paraSort(y);
            results[s][3] = paraStats(y);
        }

        SwingUtilities.invokeLater(() -> PerformanceChart.show(sizes, results));
    }

    public static int[] randData(int size) {
        Random rand = new Random();
        int[] numbers = new int[size];
        for (int i = 0; i < size; i++) {
            numbers[i] = rand.nextInt(1, 100);
        }
        return numbers;
    }

    public static long seqSort(int[] numbers) {
        long tStart = System.nanoTime();
        Arrays.sort(numbers);
        long elapsed = (System.nanoTime() - tStart) / 1000;
        System.out.println("Sequential Sort: " + elapsed + " Microseconds");
        return elapsed;
    }

    public static long seqStats(int[] numbers) {
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
        for (int number : numbers) sum += number;
        double avg = (double) sum / numbers.length;

        double median;
        if (numbers.length % 2 == 1) {
            median = numbers[numbers.length / 2];
        } else {
            median = (numbers[(numbers.length / 2) - 1] + numbers[numbers.length / 2]) / 2.0;
        }

        long elapsed = (System.nanoTime() - tStart) / 1000;
        System.out.println("Sequential Stats: " + elapsed + " Microseconds");
        return elapsed;
    }

    public static long paraSort(int[] numbers) {
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
        } catch (InterruptedException e) { e.printStackTrace(); }

        long elapsed = (System.nanoTime() - tStart) / 1000;
        System.out.println("Data Parallel Sort: " + elapsed + " Microseconds");
        return elapsed;
    }

    public static long paraStats(int[] numbers) {
        int cores = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(cores);
        long tStart = System.nanoTime();

        executor.submit(() -> {
            int min = numbers[0];
            for (int n : numbers) if (n < min) min = n;
        });
        executor.submit(() -> {
            int max = numbers[0];
            for (int n : numbers) if (n > max) max = n;
        });
        executor.submit(() -> {
            long sum = 0;
            for (int n : numbers) sum += n;
            double avg = (double) sum / numbers.length;
        });
        executor.submit(() -> {
            int[] sorted = numbers;
            //.clone();
            // Arrays.sort(sorted);
            double median = (sorted.length % 2 == 1)
                    ? sorted[sorted.length / 2]
                    : (sorted[(sorted.length / 2) - 1] + sorted[sorted.length / 2]) / 2.0;
        });

        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) { e.printStackTrace(); }

        long elapsed = (System.nanoTime() - tStart) / 1000;
        System.out.println("Task Parallel Stats: " + elapsed + " Microseconds");
        return elapsed;
    }
}