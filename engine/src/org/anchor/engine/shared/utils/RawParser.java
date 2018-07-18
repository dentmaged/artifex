package org.anchor.engine.shared.utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import org.anchor.engine.common.utils.EnumUtils;
import org.anchor.engine.shared.monitoring.cache.CacheInformation;
import org.anchor.engine.shared.physics.Material;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

public class RawParser {

    private static RawParser instance;

    public Object decode(String value, Class<?> type) {
        if (value.length() == 0)
            return null;

        String[] parts = value.split("@");
        if (type == Vector3f.class)
            return new Vector3f(Float.parseFloat(parts[0]), Float.parseFloat(parts[1]), Float.parseFloat(parts[2]));

        if (type == Vector4f.class)
            return new Vector4f(Float.parseFloat(parts[0]), Float.parseFloat(parts[1]), Float.parseFloat(parts[2]), Float.parseFloat(parts[3]));

        if (type == Material.class)
            return new Material(Float.parseFloat(parts[0]), Float.parseFloat(parts[1]), Float.parseFloat(parts[2]));

        if (type == float.class)
            return Float.parseFloat(value);

        if (type == int.class)
            return Integer.parseInt(value);

        if (type == boolean.class)
            return Boolean.parseBoolean(value);

        if (type == String.class)
            return value;

        if (type.isEnum())
            return EnumUtils.getEnumValue(type.getEnumConstants(), value);

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

        if (value instanceof Material) {
            Material material = (Material) value;

            return material.getRestition() + "@" + material.getDensity() + "@" + material.getFriction();
        }

        if (value.getClass().isEnum())
            return ((Enum<?>) value).name();

        if (value instanceof Float || value instanceof Integer || value instanceof Boolean || value instanceof String)
            return value + "";

        return "";
    }

    public void write(DataOutputStream stream, Object value) {
        if (value instanceof CacheInformation) {
            write(stream, ((CacheInformation) value).getOriginal());

            return;
        }

        try {
            if (value instanceof Vector3f) {
                Vector3f v = (Vector3f) value;

                stream.writeFloat(v.x);
                stream.writeFloat(v.y);
                stream.writeFloat(v.z);
            }

            if (value instanceof Vector4f) {
                Vector4f v = (Vector4f) value;

                stream.writeFloat(v.x);
                stream.writeFloat(v.y);
                stream.writeFloat(v.z);
                stream.writeFloat(v.w);
            }

            if (value instanceof Material) {
                Material material = (Material) value;

                stream.writeFloat(material.getRestition());
                stream.writeFloat(material.getDensity());
                stream.writeFloat(material.getFriction());
            }

            if (value instanceof Float)
                stream.writeFloat((float) value);

            if (value instanceof Integer)
                stream.writeInt((int) value);

            if (value instanceof Boolean)
                stream.writeBoolean((boolean) value);

            if (value instanceof String)
                stream.writeUTF((String) value);

            if (value.getClass().isEnum())
                stream.writeUTF(((Enum<?>) value).name());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Object read(DataInputStream stream, Class<?> type) {
        try {
            if (type == Vector3f.class)
                return new Vector3f(stream.readFloat(), stream.readFloat(), stream.readFloat());

            if (type == Vector4f.class)
                return new Vector4f(stream.readFloat(), stream.readFloat(), stream.readFloat(), stream.readFloat());

            if (type == Material.class)
                return new Material(stream.readFloat(), stream.readFloat(), stream.readFloat());

            if (type == float.class)
                return stream.readFloat();

            if (type == int.class)
                return stream.readInt();

            if (type == boolean.class)
                return stream.readBoolean();

            if (type == String.class)
                return stream.readUTF();

            if (type.isEnum())
                return EnumUtils.getEnumValue(type.getConstructors(), stream.readUTF());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
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
