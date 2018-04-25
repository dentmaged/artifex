package org.anchor.engine.shared.utils;

import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

public class RawParser {

    private static RawParser instance;

    public Object decode(String value, Class<?> type) {
        String[] parts = value.split("@");
        if (type == float.class)
            return Float.parseFloat(value);
        if (type == int.class)
            return Integer.parseInt(value);
        if (type == Vector3f.class)
            return new Vector3f(Float.parseFloat(parts[0]), Float.parseFloat(parts[1]), Float.parseFloat(parts[2]));
        if (type == Vector4f.class)
            return new Vector4f(Float.parseFloat(parts[0]), Float.parseFloat(parts[1]), Float.parseFloat(parts[2]), Float.parseFloat(parts[3]));
        if (type == boolean.class)
            return Boolean.parseBoolean(value);
        if (type == String.class)
            return value;

        return null;
    }

    public String encode(Object value) {
        if (value instanceof Vector3f) {
            Vector3f v = (Vector3f) value;

            return v.x + "@" + v.y + "@" + v.z;
        }

        if (value instanceof Vector4f) {
            Vector4f v = (Vector4f) value;

            return v.x + "@" + v.y + "@" + v.z + "@" + v.w;
        }

        if (value instanceof Float || value instanceof Integer || value instanceof Boolean)
            return value + "";

        if (value instanceof String)
            return (String) value;

        return "";
    }

    public static RawParser getInstance() {
        if (instance == null)
            instance = new RawParser();

        return instance;
    }

    public static void setInstance(RawParser instance) {
        RawParser.instance = instance;
    }

}
