package com.magenta.general.utils;

public class State<T> {
    private volatile T state;

    public State(T state) {
        this.state = state;
    }

    public T getState() {
        return state;
    }

    public void setState(T state) {
        this.state = state;
    }
}
