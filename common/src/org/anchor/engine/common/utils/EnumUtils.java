package org.anchor.engine.common.utils;

public class EnumUtils {

    public static Object getEnumValue(Object[] constants, String name) {
        for (Object constant : constants)
            if (constant.toString().equalsIgnoreCase(name))
                return constant;

        return null;
    }

}
