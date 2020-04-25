package com.dmitriy.testtasks.computablehash;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class ComputableHashMapTest {
    private static final int NUM_VALUES = 10000;
    private static final int NUM_THREADS = 100;
    private static final double ACCURACY = 0.000001;

    @Test
    public void ConcurrencyTestForOneFunction() {
        ComputableHashMap<Double, Double> map = new ComputableHashMap<>();

        Runnable task = () -> {
            ArrayList<Future<Double>> futures = new ArrayList<>();

            for (double i = 1; i <= NUM_VALUES; i++)
                futures.add(map.compute(i, Math::sqrt));

            for (int i = 0; i < NUM_VALUES; i++) {
                try {
                    assertEquals(futures.get(i).get(), Math.sqrt(i + 1), ACCURACY);
                } catch (Exception e) {
                    fail();
                }
            }
        };

        ArrayList<Thread> threads = new ArrayList<>();
        //long s = System.currentTimeMillis();

        for (int t = 0; t < NUM_THREADS; t++) {
            Thread thread = new Thread(task);
            threads.add(thread);
            thread.start();
        }

        threads.forEach(thread -> {
            try {
                thread.join();
            } catch (InterruptedException e) {
                fail();
            }
        });

        //System.out.println("Time: " + (System.currentTimeMillis() - s));

        for (double i = 1; i <= NUM_VALUES; i++)
            assertEquals(map.get(i), Math.sqrt(i), ACCURACY);
    }

    @Test
    public void ConcurrencyTestForDifferentFunctions() {
        ComputableHashMap<String, String> map = new ComputableHashMap<>();
        AtomicInteger counter = new AtomicInteger(0);

        Consumer<Integer> task = (Integer numThread) -> {
            ArrayList<Future<String>> futures = new ArrayList<>();

            for (int i = 0; i < NUM_VALUES; i++)
                futures.add(map.compute("Str" + i, s -> s + "_" + numThread));

            for (int i = 0; i < NUM_VALUES; i++) {
                try {
                    String res = futures.get(i).get();
                    assertTrue(res.startsWith("Str" + i + "_"));

                    if (res.equals("Str" + i + "_" + numThread))
                        counter.incrementAndGet();

                } catch (Exception e) {
                    fail();
                }
            }
        };

        ArrayList<Thread> threads = new ArrayList<>();
        //long s = System.currentTimeMillis();

        for (int t = 0; t < NUM_THREADS; t++) {
            final int ft = t;
            Thread thread = new Thread(() -> task.accept(ft));
            threads.add(thread);
            thread.start();
        }

        threads.forEach(thread -> {
            try {
                thread.join();
            } catch (InterruptedException e) {
                fail();
            }
        });

        //System.out.println("Time: " + (System.currentTimeMillis() - s));

        assertEquals(counter.get(), NUM_VALUES);
    }
}
