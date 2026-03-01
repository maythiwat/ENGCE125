package com.maythiwat.engce125.lab3;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Sequential {
    public static void main(String[] args) {
        int[] numbers = randData(100);
        seqRun(numbers.clone());
    }

    public static int[] randData(int size) {
        Random rand = new Random();

        int[] numbers = new int[size];
        for (int i = 0; i < size; i++) {
            numbers[i] = rand.nextInt(1, 99);
        }

        return numbers;
    }

    public static void seqRun(int[] numbers) {
        seqSort(numbers);
        seqStats(numbers);
    }

    public static void seqSort(int[] numbers) {
        long tStart = System.nanoTime();
        Arrays.sort(numbers);
        System.out.println("Sequential Sort: " + (System.nanoTime() - tStart) / 1000 + " Microseconds");
    }

    public static void seqStats(int[] numbers) {
        long tStart = System.nanoTime();

        int min = numbers[0];
        for (int number : numbers) {
            if (number < min) min = number;
        }

        int max = numbers[0];
        for (int number : numbers) {
            if (number > max) max = number;
        }

        int sum = 0;
        for (int number : numbers) {
            sum = sum + number;
        }
        double avg = (double) sum / numbers.length;

        double median;
        if (numbers.length % 2 == 1) {
            median = numbers[numbers.length / 2];
        } else {
            median = (numbers[(numbers.length / 2) - 1] + numbers[numbers.length / 2]) / 2.0;
        }

        System.out.println("Min: " + min);
        System.out.println("Max: " + max);
        System.out.println("Avg: " + avg);
        System.out.println("Median: " + median);

        System.out.println("Sequential Stats: " + (System.nanoTime() - tStart) / 1000 + " Microseconds");
    }

    public static void paraSort(int[] numbers) {
        int cores = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(cores);


    }
}
