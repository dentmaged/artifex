package org.anchor.client.engine.renderer.font;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

public class TextBuilder {

    private Vector2f position = new Vector2f();
    private String text = "";
    private Font font = FontRenderer.defaultFont;
    private float size = 1;
    private Vector4f colour = new Vector4f(0, 0, 0, 1);
    private Alignment alignment = Alignment.LEFT;

    public TextBuilder position(Vector2f position) {
        return position(position.x, position.y);
    }

    public TextBuilder position(float x, float y) {
        position.set(x, y);

        return this;
    }

    public TextBuilder text(String text) {
        this.text = text;

        return this;
    }

    public TextBuilder font(Font font) {
        this.font = font;

        return this;
    }

    public TextBuilder size(float size) {
        this.size = size;

        return this;
    }

    public TextBuilder colour(Vector3f colour) {
        return colour(colour.x, colour.y, colour.z);
    }

    public TextBuilder colour(float x, float y, float z) {
        return colour(x, y, z, colour.w);
    }

    public TextBuilder colour(float x, float y, float z, float w) {
        this.colour.set(x, y, z, w);

        return this;
    }

    public TextBuilder align(Alignment alignment) {
        this.alignment = alignment;

        return this;
    }

    public Text build() {
        return new Text(position, text, font, size, colour, alignment);
    }

}
