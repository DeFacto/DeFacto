package org.aksw.defacto.dataweb;

/**
 * Created by esteves on 24.09.15.
 */
public class HolderURI<T> {
    T value;
    public HolderURI(T value) {
        this.value = value;
    }
    public void set(T anotherValue) {
        value = anotherValue;
    }
    public T get() {
        return value;
    }
    @Override
    public String toString() {
        return value.toString();
    }

    @Override
    public boolean equals(Object obj) {
        return value.equals(obj);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
