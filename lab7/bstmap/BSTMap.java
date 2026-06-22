package bstmap;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

public class BSTMap<K extends Comparable<K>, V> implements Map61B<K, V> {

    private class Node {
       K key;
       V val;
       Node left, right;

       public Node (K key, V val) {
           this.key = key;
           this.val = val;
       }
    }

    private Node root;
    private int size;

    private Node findClosestNode (K key) {
        Node currentNode = root;
        if (root == null) {
            return null;
        } else {
           while (true) {
               if (currentNode.key.compareTo(key) < 0) {
                   if (currentNode.right == null) {
                       return currentNode;
                   } else {
                       currentNode = currentNode.right;
                   }
               } else if (currentNode.key.compareTo(key) > 0) {
                   if (currentNode.left == null) {
                       return currentNode;
                   } else {
                       currentNode = currentNode.left;
                   }
               } else {
                   return currentNode;
               }
           }
        }
    }

    @Override
    public void clear() {
        root = null;
        size = 0;
    }

    @Override
    public boolean containsKey(K key) {
        if (root == null) {
            return false;
        } else {
            Node node = findClosestNode(key);
            return node.key.compareTo(key) == 0;
        }
    }

    @Override
    public V get(K key) {
        Node node = findClosestNode(key);
        if (node == null) {
            return null;
        } else if (node.key.compareTo(key) == 0) {
            return node.val;
        } else {
            return null;
        }
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void put(K key, V value) {
        Node node = new Node(key, value);

        if (root == null) {
            root = node;
        } else {
            Node closestNode = findClosestNode(key);
            if (closestNode.key.compareTo(key) > 0) {
                closestNode.left = node;
            } else if (closestNode.key.compareTo(key) < 0) {
                closestNode.right = node;
            } else {
                closestNode.val = value;
                return;
            }
        }
        size += 1;
    }

    @Override
    public Set<K> keySet() {
        Set<K> set = new TreeSet<>();
        for (K key : this) {
           set.add(key);
        }
        return set;
    }

    private Node deleteRootNode(Node node) {
        if (node.right == null) {
            return node.left;
        } else if (node.left == null) {
            return node.right;
        } else if (node.left.right == null) {
           node.left.right = node.right;
           return node.left;
        } else {
            Node parentNode = node.left;
            Node subNode = parentNode.right;
            while (subNode.right != null) {
                parentNode = subNode;
                subNode = subNode.right;
            }
            parentNode.right = null;
            subNode.right = node.right;
            subNode.left = node.left;
            return subNode;
        }

    }

    @Override
    public V remove(K key) {
        Node currentNode;
        V val;
        if (!containsKey(key)) {
            return null;
        }
        if (root.key == key) {
            val = root.val;
            root = deleteRootNode(root);
        } else {
            Node newNode;
            currentNode = root;
            while (true) {
                if (currentNode.key.compareTo(key) < 0) {
                    if (currentNode.right.key.compareTo(key) == 0) {
                        val = currentNode.right.val;
                        newNode = deleteRootNode(currentNode.right);
                        currentNode.right = newNode;
                        break;
                    } else {
                        currentNode = currentNode.right;
                    }
                } else {
                    if (currentNode.left.key.compareTo(key) == 0) {
                        val = currentNode.left.val;
                        newNode = deleteRootNode(currentNode.left);
                        currentNode.left = newNode;
                        break;
                    } else {
                        currentNode = currentNode.left;
                    }
                }
            }
        }
        size -= 1;
        return val;
    }

    @Override
    public V remove(K key, V value) {
        if (get(key).equals(value)) {
           return remove(key);
        } else {
            return null;
        }
    }

    @Override
    public Iterator<K> iterator() {
        return new BSTIter();
    }

    private class BSTIter implements Iterator<K> {

        private ArrayDeque<Node> arrayDeque;

        public BSTIter() {
            arrayDeque = new ArrayDeque<>();
            if (root != null) {
                arrayDeque.addFirst(root);
            }
        }

        @Override
        public boolean hasNext() {
            return !arrayDeque.isEmpty();
        }

        @Override
        public K next() {
            Node first = arrayDeque.removeFirst();
            if (first.right != null) {
                arrayDeque.addFirst(first.right);
            }
            if (first.left != null) {
                arrayDeque.addFirst(first.left);
            }
            return first.key;
        }
    }
}