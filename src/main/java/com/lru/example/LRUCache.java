package com.lru.example;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class LRUCache {
    private final static Logger LOG = LoggerFactory.getLogger(LRUCache.class);

    public final int CAPACITY;
    public ConcurrentHashMap<Integer, Node> map;
    Node head = null;
    Node tail = null;

    protected LRUCache(int capacity) {
        this.CAPACITY = capacity;
        this.map = new ConcurrentHashMap<>();
    }

    protected void remove(Node node) {
        if (node.left != null)
            node.left.right = node.right;
        else
            head = node.right;

        if (node.right != null)
            node.right.left = node.left;
        else
            tail = node.left;
    }

    protected void addHead(Node node) {
        if (head != null)
            head.left = node;

        node.right = head;
        head = node;
        head.left = null;

        if (tail == null)
            tail = head;
    }

    protected Optional<Node> put(int key, int value) {
        LOG.info("Adding new entry, key : " + key + " value : " + value);
        Optional<Node> returnValue = Optional.empty();
        if (map.containsKey(key)) {
            Node node = map.get(key);
            remove(node);
            addHead(node);
        } else {
            if (map.size() >= CAPACITY) {
                returnValue = Optional.of(tail);
                map.remove(tail.key);
                remove(tail);
            }

            Node newNode = new Node(key, value);
            addHead(newNode);
            map.put(key, newNode);
        }

        return returnValue;
    }

    protected Optional<Node> get(int key) {
        return (map.containsKey(key) ? Optional.of(map.get(key)) : Optional.empty());
    }

    // Only for debugging
    protected void printCurrentStack() {
        StringBuilder sb = new StringBuilder();
        Node temp = head;
        while (temp != null) {
            sb.append(" (" + temp.key + ":" + temp.value + ") ");
            temp = temp.right;
        }
        System.out.println("Current Cache = [ " + sb.toString() + "]");
    }

}
