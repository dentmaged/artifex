package org.anchor.engine.common.events.handler;

import java.lang.reflect.Method;

import org.anchor.engine.common.events.Listener;

public class Handler {

    private Listener listener;
    private Method method;

    public Handler(Listener listener, Method method) {
        this.listener = listener;
        this.method = method;
        method.setAccessible(true);
    }

    public Listener getListener() {
        return listener;
    }

    public Method getMethod() {
        return method;
    }

}
