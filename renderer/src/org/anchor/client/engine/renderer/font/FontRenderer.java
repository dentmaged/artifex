package org.anchor.client.engine.renderer.font;

import java.util.List;

import org.anchor.client.engine.renderer.Graphics;
import org.anchor.client.engine.renderer.QuadRenderer;
import org.anchor.engine.common.utils.CoreMaths;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

public class FontRenderer {

    protected static FontShader shader = FontShader.getInstance();

    public static final float advance = 0.008f;

    public static void render(List<Text> texts) {
        for (Text text : texts) {
            Font font = text.getFont();
            float size = text.getSize();

            shader.start();
            QuadRenderer.bind();

            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            Graphics.bind2DTexture(font.getAtlas(), 0);

            float x = text.getAlignment().getAlignment(text.getLength());
            for (char c : text.getText().toCharArray()) {
                if (c == ' ') {
                    x += (font.getSpaceWidth() + advance) * size;

                    continue;
                }

                Character character = font.getCharacter((int) c);
                shader.loadInformation(CoreMaths.createTransformationMatrix(new Vector2f(text.getPosition().x + x + character.getXOffset() * size, text.getPosition().y - character.getYOffset() * size), new Vector2f(character.getSizeX() * size, character.getSizeY() * size), 180, 0), text.getColour(), character.getUV());

                x += (character.getXAdvance() + advance) * size;
                QuadRenderer.render();
            }

            GL11.glDisable(GL11.GL_BLEND);
            QuadRenderer.unbind();
            shader.stop();
        }
    }

}
