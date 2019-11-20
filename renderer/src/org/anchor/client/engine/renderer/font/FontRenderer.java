package org.anchor.client.engine.renderer.font;

import java.util.List;

import org.anchor.client.engine.renderer.Graphics;
import org.anchor.client.engine.renderer.QuadRenderer;
import org.anchor.engine.common.utils.CoreMaths;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

public class FontRenderer {

    protected static FontShader shader = FontShader.getInstance();
    public static Font defaultFont = new Font("segoe");

    public static final float HORIZONTAL_PADDING = 0.0075f;
    public static final float VERTICAL_PADDING = 0.045f / ((float) Display.getHeight() / 720f);
    public static final int TAB_SIZE = 8;

    public static void render(List<Text> texts) {
        GL11.glDisable(GL11.GL_CULL_FACE);
        shader.start();
        QuadRenderer.bind();

        float horizontalMultiplier = 1280f / (float) Display.getWidth();
        for (Text text : texts) {
            if (text.getColour().w == 0)
                continue;

            Font font = text.getFont();
            float size = text.getSize();

            Graphics.bind2DTexture(font.getAtlas(), 0);

            float x = text.getAlignment().getAlignment(text.getLength());
            float y = text.getPosition().y;
            float startX = x;
            for (char c : text.getText().toCharArray()) {
                if (c == ' ' || c == '\t') {
                    x += (font.getSpaceWidth() * (c == '\t' ? TAB_SIZE : 1) + HORIZONTAL_PADDING) * size * horizontalMultiplier;

                    continue;
                } else if (c == '\n') {
                    x = startX;
                    y -= VERTICAL_PADDING * size;

                    continue;
                }

                Character character = font.getCharacter((int) c);
                shader.loadInformation(CoreMaths.createTransformationMatrix(new Vector2f(text.getPosition().x + x + character.getXOffset() * size, y - character.getYOffset() * size), new Vector2f(character.getSizeX() * size, character.getSizeY() * size), 180, 0), text.getColour(), character.getUV());

                x += (character.getXAdvance() + HORIZONTAL_PADDING) * size * horizontalMultiplier;
                QuadRenderer.render();
            }
        }

        QuadRenderer.unbind();
        shader.stop();
        GL11.glEnable(GL11.GL_CULL_FACE);
    }

    public static void render(Text text) {
        if (text.getColour().w == 0)
            return;

        GL11.glDisable(GL11.GL_CULL_FACE);
        float horizontalMultiplier = 1280f / (float) Display.getWidth();
        shader.start();
        QuadRenderer.bind();

        Font font = text.getFont();
        float size = text.getSize();

        Graphics.bind2DTexture(font.getAtlas(), 0);

        float x = text.getAlignment().getAlignment(text.getLength());
        float y = text.getPosition().y;
        float startX = x;
        for (char c : text.getText().toCharArray()) {
            if (c == ' ' || c == '\t') {
                x += (font.getSpaceWidth() * (c == '\t' ? TAB_SIZE : 1) + HORIZONTAL_PADDING) * size * horizontalMultiplier;

                continue;
            } else if (c == '\n') {
                x = startX;
                y -= VERTICAL_PADDING * size;

                continue;
            }

            Character character = font.getCharacter((int) c);
            if (character == null)
                continue;

            shader.loadInformation(CoreMaths.createTransformationMatrix(new Vector2f(text.getPosition().x + x + character.getXOffset() * size, y - character.getYOffset() * size), new Vector2f(character.getSizeX() * size, character.getSizeY() * size), 180, 0), text.getColour(), character.getUV());

            x += (character.getXAdvance() + HORIZONTAL_PADDING) * size * horizontalMultiplier;
            QuadRenderer.render();
        }

        Graphics.checkForErrors();
        QuadRenderer.unbind();
        shader.stop();
        GL11.glEnable(GL11.GL_CULL_FACE);
    }

}
