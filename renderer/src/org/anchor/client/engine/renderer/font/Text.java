package org.anchor.client.engine.renderer.font;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

public class Text {

    private Vector2f position;
    private String text;
    private Font font;
    private float size, length;
    private Vector4f colour;
    private boolean centered;

    public Text(Vector2f position, String text, Font font, float size) {
        this(position, text, font, size, new Vector3f(), false);
    }

    public Text(Vector2f position, String text, Font font, float size, Vector3f colour, boolean centered) {
        this.position = position;
        this.font = font;
        this.size = size;
        this.colour = new Vector4f(colour.x, colour.y, colour.z, 1);
        this.centered = centered;

        setText(text);
    }

    public Vector2f getPosition() {
        return position;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;

        for (char c : text.toCharArray()) {
            if (c == ' ')
                length += (font.getSpaceWidth() + FontRenderer.advance) * size;
            else
                length += (font.getCharacter((int) c).getXAdvance() + FontRenderer.advance) * size;
        }
    }

    public Font getFont() {
        return font;
    }

    public float getSize() {
        return size;
    }

    public Vector4f getColour() {
        return colour;
    }

    public boolean isCentered() {
        return centered;
    }

    public float getLength() {
        return length;
    }

    public void setAlpha(float alpha) {
        this.colour.w = alpha;
    }

}