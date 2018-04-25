package org.anchor.engine.common.utils;

public class StringUtils {

    public static String upperFirst(String input) {
        return Character.toUpperCase(input.charAt(0)) + input.substring(1);
    }

}
