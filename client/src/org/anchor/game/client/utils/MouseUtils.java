package org.anchor.game.client.utils;

import org.lwjgl.input.Mouse;

public class MouseUtils {

    private static boolean left, right, middle;

    public static void update() {
        left = false;
        right = false;
        middle = false;

        while (Mouse.next()) {
            if (Mouse.isButtonDown(0) && Mouse.getEventButtonState() && Mouse.getEventButton() > -1)
                left = true;

            if (Mouse.isButtonDown(1) && Mouse.getEventButtonState() && Mouse.getEventButton() > -1)
                right = true;

            if (Mouse.isButtonDown(2) && Mouse.getEventButtonState() && Mouse.getEventButton() > -1)
                middle = true;
        }
    }

    public static boolean canLeftClick() {
        return left;
    }

    public static boolean canRightClick() {
        return right;
    }

    public static boolean canMiddleClick() {
        return middle;
    }

}
