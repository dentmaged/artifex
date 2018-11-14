package org.anchor.client.engine.renderer;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Keyboard;

public class KeyboardUtils {

    private static List<Integer> keysDown = new ArrayList<Integer>();
    private static List<Integer> keysPressed = new ArrayList<Integer>();
    private static List<Character> charactersDown = new ArrayList<Character>();

    public static void update() {
        while (Keyboard.next()) {
            int key = Keyboard.getEventKey();
            if (Keyboard.getEventKeyState()) {
                keysDown.add(Integer.valueOf(key));
                keysPressed.add(Integer.valueOf(key));
                charactersDown.add(Character.valueOf(Keyboard.getEventCharacter()));
            } else {
                keysDown.remove(Integer.valueOf(key));
                keysPressed.remove(Integer.valueOf(key));
                charactersDown.remove(Character.valueOf(Keyboard.getEventCharacter()));
            }
        }

    }

    public static boolean isKeyDown(int key) {
        return keysDown.contains(key);
    }

    public static boolean wasKeyJustPressed(int key) {
        boolean pressed = keysPressed.contains(key);
        keysPressed.remove(Integer.valueOf(key));

        return pressed;
    }

    public static boolean hasAnyKeyJustBeenPressed() {
        return keysPressed.size() > 0;
    }

    public static List<Character> getPressedCharacters() {
        return charactersDown;
    }

}
