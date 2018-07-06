package org.anchor.engine.common.utils;

public class ObjectUtils {

    public static boolean compare(Object one, Object two) {
        return one == null ? two == null : one.equals(two);
    }

}
