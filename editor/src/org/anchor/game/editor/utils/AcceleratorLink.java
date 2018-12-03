package org.anchor.game.editor.utils;

import java.awt.event.InputEvent;

import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

import org.anchor.client.engine.renderer.KeyboardUtils;
import org.lwjgl.input.Keyboard;

public class AcceleratorLink {

    private JMenuItem item;
    private int key, mask;

    private static int[] masks = new int[] { InputEvent.SHIFT_MASK, InputEvent.CTRL_MASK, InputEvent.META_MASK, InputEvent.ALT_MASK };

    private static int[] lwjgl = new int[] { Keyboard.KEY_LSHIFT, Keyboard.KEY_LCONTROL, Keyboard.KEY_LMETA, Keyboard.KEY_LMETA };

    public AcceleratorLink(JMenuItem item, int key, int mask) {
        this.item = item;
        this.key = key;
        this.mask = mask;
    }

    public void check() {
        boolean maskPressed = true;
        for (int i = 0; i < masks.length; i++) {
            boolean required = (masks[i] & mask) > 0;
            if (required && !Keyboard.isKeyDown(lwjgl[i]) || (!required && Keyboard.isKeyDown(lwjgl[i])))
                maskPressed = false;
        }

        if (maskPressed && KeyboardUtils.wasKeyJustPressed(key)) {
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    item.doClick();
                }

            });
        }
    }

}
