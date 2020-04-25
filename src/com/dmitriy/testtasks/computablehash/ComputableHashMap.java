package com.dmitriy.testtasks.computablehash;

import java.util.concurrent.*;
import java.util.function.Function;

public class ComputableHashMap<K, V> extends ConcurrentHashMap<K, V> {

    private ExecutorService threadPool;

    ComputableHashMap() {
        this(8);
    }

    ComputableHashMap(int threadPoolSize) {
        super();

        threadPool = Executors.newFixedThreadPool(threadPoolSize);
    }

    Future<V> compute(K k, Function<K, V> f) {
        return CompletableFuture.supplyAsync(
            () -> super.computeIfAbsent(k, f),
            threadPool
        );
    }
}
