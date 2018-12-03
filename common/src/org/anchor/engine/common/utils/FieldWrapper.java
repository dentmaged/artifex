package org.anchor.engine.common.utils;

import java.lang.reflect.Field;

public class FieldWrapper implements JavaField {

    protected Field field;

    public FieldWrapper(Field field) {
        this.field = field;
    }

    @Override
    public Object get(Object instance) {
        try {
            return field.get(instance);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void set(Object instance, Object value) {
        try {
            field.set(instance, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getName() {
        return field.getName();
    }

    @Override
    public Class<?> getType() {
        return field.getType();
    }

}
