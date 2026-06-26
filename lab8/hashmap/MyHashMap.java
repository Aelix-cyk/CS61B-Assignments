package hashmap;

import afu.org.checkerframework.checker.oigj.qual.O;

import java.util.*;

/**
 *  A hash table-backed Map implementation. Provides amortized constant time
 *  access to elements via get(), remove(), and put() in the best case.
 *
 *  Assumes null keys will never be inserted, and does not resize down upon remove().
 *  @author YOUR NAME HERE
 */
public class MyHashMap<K, V> implements Map61B<K, V> {

    /**
     * Protected helper class to store key/value pairs
     * The protected qualifier allows subclass access
     */
    protected class Node {
        K key;
        V value;

        Node(K k, V v) {
            key = k;
            value = v;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Node other = (Node) o;
            return this.key.equals(other.key);
        }
    }

    /* Instance Variables */
    private final int INITIAL_SIZE = 16;
    private final double DEFAULT_LOAD_FACTOR = 0.75;

    private Collection<Node>[] buckets;
    private Set<K> hashSet;
    private double loadFactor;
    private int size;
    // You should probably define some more!

    /** Initialize variables */
    private void init() {
        loadFactor = DEFAULT_LOAD_FACTOR;
        size = 0;
        hashSet = new HashSet<K>();
    }

    /** Compute index from key */
    private int getIndex(K key) {
        return Math.floorMod(key.hashCode(), buckets.length);
    }

    /** Increase size of buckets */
    private void resize(int size) {
       Collection<Node>[] newBuckets = createTable(size);
       for (K key : hashSet) {
           Node node = new Node(key, get(key));
           int index = Math.floorMod(key.hashCode(), newBuckets.length);
           if (newBuckets[index] == null) {
               newBuckets[index] = createBucket();
           }
           newBuckets[index].add(node);
       }
       buckets = newBuckets;
    }

    /** Constructors */
    public MyHashMap() {
        init();
        buckets = createTable(INITIAL_SIZE);
    }

    public MyHashMap(int initialSize) {
        init();
        buckets = createTable(initialSize);
    }

    /**
     * MyHashMap constructor that creates a backing array of initialSize.
     * The load factor (# items / # buckets) should always be <= loadFactor
     *
     * @param initialSize initial size of backing array
     * @param maxLoad maximum load factor
     */
    public MyHashMap(int initialSize, double maxLoad) {
        init();
        buckets = createTable(initialSize);
        loadFactor = maxLoad;
    }

    /**
     * Returns a new node to be placed in a hash table bucket
     */
    private Node createNode(K key, V value) {
        return new Node(key, value);
    }

    /**
     * Returns a data structure to be a hash table bucket
     *
     * The only requirements of a hash table bucket are that we can:
     *  1. Insert items (`add` method)
     *  2. Remove items (`remove` method)
     *  3. Iterate through items (`iterator` method)
     *
     * Each of these methods is supported by java.util.Collection,
     * Most data structures in Java inherit from Collection, so we
     * can use almost any data structure as our buckets.
     *
     * Override this method to use different data structures as
     * the underlying bucket type
     *
     * BE SURE TO CALL THIS FACTORY METHOD INSTEAD OF CREATING YOUR
     * OWN BUCKET DATA STRUCTURES WITH THE NEW OPERATOR!
     */
    protected Collection<Node> createBucket() {
        return new ArrayList<Node>();
    }

    /**
     * Returns a table to back our hash table. As per the comment
     * above, this table can be an array of Collection objects
     *
     * BE SURE TO CALL THIS FACTORY METHOD WHEN CREATING A TABLE SO
     * THAT ALL BUCKET TYPES ARE OF JAVA.UTIL.COLLECTION
     *
     * @param tableSize the size of the table to create
     */
    private Collection<Node>[] createTable(int tableSize) {
        return new Collection[tableSize];
    }

    // TODO: Implement the methods of the Map61B Interface below
    // Your code won't compile until you do so!

    /** Removes all of the mappings from this map. */
    public void clear() {
        for (int i = 0; i < buckets.length; i += 1) {
            buckets[i] = null;
        }
        hashSet.clear();
        size = 0;
    }

    /** Returns true if this map contains a mapping for the specified key. */
    public boolean containsKey(K key){
        return hashSet.contains(key);
    }

    /**
     * Returns the value to which the specified key is mapped, or null if this
     * map contains no mapping for the key.
     */
    public V get(K key) {
        int index = getIndex(key);
        if (buckets[index] != null) {
            for (Node node : buckets[index]) {
                if (node.key.equals(key)) {
                    return node.value;
                }
            }
        }
        return null;
    }

    /** Returns the number of key-value mappings in this map. */
    public int size() {
        return size;
    }

    /**
     * Associates the specified value with the specified key in this map.
     * If the map previously contained a mapping for the key,
     * the old value is replaced.
     */
    public void put(K key, V value) {
        int index = getIndex(key);
        if (!containsKey(key)) {
            if (buckets[index] == null) {
                buckets[index] = createBucket();
            }
            buckets[index].add(new Node(key, value));
            hashSet.add(key);
            size += 1;
        } else {
            for (Node node : buckets[index]) {
                if (node.key.equals(key)) {
                    node.value = value;
                }
            }
        }
        if (size / (double) buckets.length > loadFactor) {
            resize(buckets.length * 2);
        }
    }

    /** Returns a Set view of the keys contained in this map. */
    public Set<K> keySet() {
        return Collections.unmodifiableSet(hashSet);
    }

    /**
     * Removes the mapping for the specified key from this map if present.
     * Not required for Lab 8. If you don't implement this, throw an
     * UnsupportedOperationException.
     */
    public V remove(K key) {
        if (!containsKey(key)) {
            return null;
        } else {
            V oldValue = get(key);
            buckets[getIndex(key)].remove(new Node(key, null));
            size -= 1;
            hashSet.remove(key);
            return oldValue;
        }
    }

    /**
     * Removes the entry for the specified key only if it is currently mapped to
     * the specified value. Not required for Lab 8. If you don't implement this,
     * throw an UnsupportedOperationException.
     */
    public V remove(K key, V value) {
        if (!containsKey(key)) {
            return null;
        } else if (value != get(key)) {
            return null;
        } else {
            buckets[getIndex(key)].remove(new Node(key, null));
            size -= 1;
            hashSet.remove(key);
            return value;
        }
    }

    public Iterator<K> iterator() {
        return hashSet.iterator();
    }
}
