package org.anchor.client.engine.renderer.gui;

import java.util.List;

import org.anchor.client.engine.renderer.Graphics;
import org.anchor.client.engine.renderer.QuadRenderer;
import org.anchor.engine.common.utils.CoreMaths;
import org.lwjgl.util.vector.Vector2f;

public class GUIRenderer {

    protected static GUIShader shader = GUIShader.getInstance();

    public static void render(List<GUI> guis) {
        shader.start();
        QuadRenderer.bind();

        for (GUI gui : guis) {
            Graphics.bind2DTexture(gui.getTexture(), 0);

            shader.loadInformation(gui.getTransformationMatrix());
            QuadRenderer.render();
        }

        QuadRenderer.unbind();
        shader.stop();
    }

    public static void perform(int texture) {
        shader.start();
        QuadRenderer.bind();

        Graphics.bind2DTexture(texture, 0);
        shader.loadInformation(CoreMaths.createTransformationMatrix(new Vector2f(), new Vector2f(1, 1), 0, 0));
        QuadRenderer.render();

        QuadRenderer.unbind();
        shader.stop();
    }

}
