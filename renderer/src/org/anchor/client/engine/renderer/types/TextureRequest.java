package org.anchor.client.engine.renderer.types;

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

}
