package org.anchor.game.client.utils;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Keyboard;

public class KeyboardUtils {

    private static List<Integer> keysDown = new ArrayList<Integer>();
    private static List<Integer> keysPressed = new ArrayList<Integer>();

    public static void update() {
        while (Keyboard.next()) {
            int key = Keyboard.getEventKey();
            if (Keyboard.getEventKeyState()) {
                keysDown.add(Integer.valueOf(key));
                keysPressed.add(Integer.valueOf(key));
            } else {
                keysDown.remove(Integer.valueOf(key));
                keysPressed.remove(Integer.valueOf(key));
            }
        }

    }

    public static boolean isKeyDown(int key) {
        return keysDown.contains(key);
    }

    public static boolean isKeyPressed(int key) {
        boolean pressed = keysPressed.contains(key);
        keysPressed.remove(Integer.valueOf(key));

        return pressed;
    }

}
