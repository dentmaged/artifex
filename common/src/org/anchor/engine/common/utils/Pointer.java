package org.anchor.engine.common.utils;

public class Pointer<T> {

    protected T value;

    public Pointer() {

    }

    public Pointer(T value) {
        this.value = value;
    }

    public T get() {
        return value;
    }

    public void set(T value) {
        this.value = value;
    }

}
