package org.anchor.client.engine.renderer.font;

import org.anchor.engine.common.Log;
import org.anchor.engine.common.utils.FileHelper;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

public class Text {

    private Vector2f position;
    private String text = "";
    private Font font;
    private float size, length;
    private Vector4f colour;
    private Alignment alignment;

    public Text(Vector2f position, String text, Font font, float size) {
        this(position, text, font, size, new Vector3f(), Alignment.LEFT);
    }

    public Text(Vector2f position, String text, Font font, float size, Vector3f colour, Alignment alignment) {
        this(position, text, font, size, colour, alignment, 1);
    }

    public Text(Vector2f position, String text, Font font, float size, Vector3f colour, Alignment alignment, float alpha) {
        this.position = position;
        this.font = font;
        this.size = size;
        this.colour = new Vector4f(colour.x, colour.y, colour.z, alpha);
        this.alignment = alignment;

        setText(text);
    }

    public Text(Vector2f position, String text, Font font, float size, Vector4f colour, Alignment alignment) {
        this.position = position;
        this.font = font;
        this.size = size;
        this.colour = colour;
        this.alignment = alignment;

        setText(text);
    }

    public Vector2f getPosition() {
        return position;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        if (this.text.equals(text))
            return;

        this.text = text;

        length = 0;
        float longestLength = 0;
        for (char c : text.toCharArray()) {
            if (c == ' ' || c == '\t') {
                length += (font.getSpaceWidth() * (c == '\t' ? FontRenderer.TAB_SIZE : 1) + FontRenderer.HORIZONTAL_PADDING) * size;
            } else if (c == '\n') {
                longestLength = Math.max(longestLength, length);
                length = 0;
            } else {
                Character character = font.getCharacter((int) c);
                if (character == null) {
                    Log.showPopup("Font", "Missing character \"" + c + "\" (" + (int) c + ") in " + FileHelper.localFileName(font.getFile()));

                    continue;
                }

                length += (character.getXAdvance() + FontRenderer.HORIZONTAL_PADDING) * size;
            }
        }
        length = Math.max(longestLength, length);
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

    public Alignment getAlignment() {
        return alignment;
    }

    public float getLength() {
        return length;
    }

    public void setAlpha(float alpha) {
        this.colour.w = alpha;
    }

    public void setAlignment(Alignment alignment) {
        this.alignment = alignment;
    }

}
