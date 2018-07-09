package org.anchor.client.engine.renderer.gui;

import org.anchor.engine.common.utils.CoreMaths;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;

public class GUI {

    private int texture;
    private Vector2f position, scale;

    public GUI(int texture) {
        this(texture, new Vector2f(0, 0), new Vector2f(1, 1));
    }

    public GUI(int texture, Vector2f position, Vector2f scale) {
        this.texture = texture;
        this.position = position;
        this.scale = scale;
    }

    public int getTexture() {
        return texture;
    }

    public Matrix4f getTransformationMatrix() {
        return CoreMaths.createTransformationMatrix(position, scale, 0, 0);
    }

}
