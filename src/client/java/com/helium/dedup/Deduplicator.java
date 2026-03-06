package com.helium.dedup;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Deduplicator<T> {

    private final ObjectOpenCustomHashSet<T> pool;
    private final ReadWriteLock rwlock = new ReentrantReadWriteLock();

    private final AtomicInteger attemptedinsertions = new AtomicInteger(0);
    private final AtomicInteger deduplicated = new AtomicInteger(0);

    public Deduplicator() {
        this(new Hash.Strategy<>() {
            @Override
            public int hashCode(T o) {
                return Objects.hashCode(o);
            }

            @Override
            public boolean equals(T a, T b) {
                return Objects.equals(a, b);
            }
        });
    }

    public Deduplicator(Hash.Strategy<T> strategy) {
        this.pool = new ObjectOpenCustomHashSet<>(strategy);
    }

    public T deduplicate(T item) {
        this.attemptedinsertions.incrementAndGet();
        rwlock.writeLock().lock();
        try {
            T result = this.pool.addOrGet(item);
            if (result != item) this.deduplicated.incrementAndGet();
            return result;
        } finally {
            rwlock.writeLock().unlock();
        }
    }

    public void clearcache() {
        rwlock.writeLock().lock();
        try {
            this.attemptedinsertions.set(0);
            this.deduplicated.set(0);
            this.pool.clear();
        } finally {
            rwlock.writeLock().unlock();
        }
    }

    @Override
    public String toString() {
        return String.format("dedup(%d/%d deduped, %d pooled)", this.deduplicated.get(), this.attemptedinsertions.get(), this.pool.size());
    }
}
