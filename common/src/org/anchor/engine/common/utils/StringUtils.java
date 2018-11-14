package org.anchor.engine.common.utils;

import java.util.Arrays;
import java.util.List;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

public class StringUtils {

    private static List<Character> validCharacters = Arrays.asList('~', '#', '/', '\\', '.', ',', '|', '`', '!', '"', '£', '$', '%', '^', '&', '*', '(', ')', '-', '_', '+', '=', '[', ']', '{', '}', ':', ';', '@', '\'', '<', '>', ' ');

    public static String upperCaseFirst(String input) {
        return Character.toUpperCase(input.charAt(0)) + input.substring(1);
    }

    public static String lowerCaseFirst(String input) {
        return Character.toLowerCase(input.charAt(0)) + input.substring(1);
    }

    public static String toString(Vector2f v) {
        return v.x + " " + v.y;
    }

    public static String toString(Vector3f v) {
        return v.x + " " + v.y + " " + v.z;
    }

    public static String toString(Vector4f v) {
        return v.x + " " + v.y + " " + v.z + " " + v.w;
    }

    public static boolean isValidCharacter(char c) {
        return Character.isLetterOrDigit(c) || validCharacters.contains(c);
    }

}
