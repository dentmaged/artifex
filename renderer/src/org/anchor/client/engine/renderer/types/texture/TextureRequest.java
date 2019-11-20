package org.anchor.client.engine.renderer.types.texture;

import org.anchor.engine.common.Log;
import org.lwjgl.opengl.GL11;

public class TextureRequest {

    private String name;
    private int texture = -1;

    public TextureRequest(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setTexture(int texture) {
        this.texture = texture;
    }

    public int getTexture() {
        return texture;
    }

    public boolean isLoaded() {
        return texture != -1;
    }

    public void unload() {
        if (!isLoaded()) {
            Log.debug("Failed to unload " + name + ": texture isn't loaded!");
            return;
        }

        GL11.glDeleteTextures(texture);
    }

}
