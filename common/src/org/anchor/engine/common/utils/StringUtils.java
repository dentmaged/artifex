package org.anchor.engine.common.utils;

public class StringUtils {

    public static String upperCaseFirst(String input) {
        return Character.toUpperCase(input.charAt(0)) + input.substring(1);
    }

    public static String lowerCaseFirst(String input) {
        return Character.toLowerCase(input.charAt(0)) + input.substring(1);
    }

}
