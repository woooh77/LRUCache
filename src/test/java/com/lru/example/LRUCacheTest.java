package com.lru.example;

import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;
import java.util.stream.IntStream;

public class LRUCacheTest extends TestCase {
    LRUCache cache;
    int capacity = 5;

    public LRUCacheTest(String name) {
        super(name);
    }

    @Before
    public void setUp() throws Exception {
        cache = new LRUCache(capacity);
    }

    @Test
    public void testSinglePut() {
        cache.put(0, 0);
        assertEquals(1, cache.getSize());
    }

    @Test
    public void testMoreThanCapacityPut() {
        cache.clearCache();
        IntStream.range(0, capacity + 1).forEach(value -> cache.put(value, value));
        assertEquals(capacity, cache.getSize());
    }

    @Test
    public void testSingleGet() {
        cache.clearCache();
        cache.put(0, 0);
        assertEquals(0, cache.get(0).get().getKey());
    }

    @Test
    public void testNoEvict() {
        cache.clearCache();
        IntStream.range(0, capacity).forEach(value -> {
            Optional<Node> evicted = cache.put(value, value);
            if (evicted.isPresent())
                fail();
        });
    }

    @Test
    public void testEvict() {
        cache.clearCache();
        IntStream.range(0, capacity).forEach(value -> cache.put(value, value));
        Optional<Node> evict = cache.put(100, 100);
        if (evict.isPresent())
            assertEquals(0, evict.get().getKey());
        else
            fail("Evict test failed.");
    }

    @Test
    public void testRecentlyUsed() {
        cache.clearCache();
        IntStream.range(0, capacity).forEach(value -> cache.put(value, value));
        cache.put(1, 1);

        assertEquals(1, cache.head.getKey());

    }
}