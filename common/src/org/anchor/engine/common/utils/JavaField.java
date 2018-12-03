package org.anchor.engine.common.utils;

public interface JavaField {

    public Object get(Object instance);

    public void set(Object instance, Object value);

    public String getName();

    public Class<?> getType();

}
