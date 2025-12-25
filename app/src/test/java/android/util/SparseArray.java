package android.util;

import java.util.HashMap;
import java.util.Map;

public class SparseArray<E> implements Cloneable {
    private Map<Integer, E> map = new HashMap<>();

    public SparseArray() {
    }

    public SparseArray(int initialCapacity) {
    }

    public E get(int key) {
        return map.get(key);
    }

    public E get(int key, E valueIfKeyNotFound) {
        E res = map.get(key);
        return res != null ? res : valueIfKeyNotFound;
    }

    public void put(int key, E value) {
        map.put(key, value);
    }

    public int size() {
        return map.size();
    }

    public void remove(int key) {
        map.remove(key);
    }

    public void clear() {
        map.clear();
    }

    public int keyAt(int index) {
        // 簡單實作，對於解決 FirebaseFirestoreException 初始化通常不需要此方法精確運作
        return 0;
    }

    public E valueAt(int index) {
        // 簡單實作
        return null;
    }

    @Override
    public SparseArray<E> clone() {
        SparseArray<E> clone = new SparseArray<>();
        clone.map = new HashMap<>(this.map);
        return clone;
    }
}
