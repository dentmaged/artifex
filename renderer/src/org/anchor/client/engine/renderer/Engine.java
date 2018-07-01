package org.anchor.client.engine.renderer;

import org.anchor.client.engine.renderer.types.Framebuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;

public class Engine {

    private static int[] slots;

    public static void init() {
        slots = new int[GL11.glGetInteger(GL20.GL_MAX_TEXTURE_IMAGE_UNITS)];
    }

    public static void bind2DTexture(int texture, int slot) {
        texture = Math.max(texture, 0);
        if (slots[slot] == texture)
            return;

        slots[slot] = texture;
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + slot);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
    }

    public static void bindColourTexture(Framebuffer framebuffer, int slot) {
        int texture = framebuffer.getColourTexture();
        if (slots[slot] == texture)
            return;

        slots[slot] = texture;
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + slot);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
    }

    public static void bindDepthTexture(Framebuffer framebuffer, int slot) {
        int texture = framebuffer.getDepthTexture();
        if (slots[slot] == texture)
            return;

        slots[slot] = texture;
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + slot);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
    }

    public static void bindCubemap(int texture, int slot) {
        texture = Math.max(texture, 0);
        if (slots[slot] == texture)
            return;

        slots[slot] = texture;
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + slot);
        GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, texture);
    }

    public static void frameEnd() {
        // reset or else glitches occur - why or when is unknown
        // only noticed with slots 2 and 12
        for (int i = 0; i < slots.length; i++) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + i);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0); // fixes 2

            slots[i] = 0; // fixes 12
        }
    }

}
