package org.anchor.client.engine.renderer.types.cubemap;

public class CubemapRequest {

    private String[] textures;
    private int texture = -1;

    public CubemapRequest(String[] textures) {
        this.textures = textures;
    }

    public String[] getTextures() {
        return textures;
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

}
