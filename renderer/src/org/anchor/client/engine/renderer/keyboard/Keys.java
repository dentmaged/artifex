package org.anchor.client.engine.renderer.keyboard;

import java.util.HashMap;
import java.util.Map;

public class Keys {

    private static Map<String, Boolean> pressed = new HashMap<String, Boolean>();

    public static boolean isKeyPressed(String name) {
        Boolean val = pressed.get(name);
        if (val == null)
            return false;

        return (boolean) val;
    }

    public static void setPressed(String name, boolean isPressed) {
        pressed.put(name, isPressed);
    }

}
