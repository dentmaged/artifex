package org.anchor.client.engine.renderer.blur;

import org.anchor.client.engine.renderer.Shader;
import org.lwjgl.util.vector.Vector2f;

public class BlurShader extends Shader {

    private static BlurShader instance = new BlurShader();

    public BlurShader() {
        super("blur");
    }

    public BlurShader(String shader) {
        super(shader);
    }

    @Override
    public void start() {
        super.start();

        loadInt("originalTexture", 0);
    }

    public void loadInformation(boolean horizontal, float dimension) {
        Vector2f direction = new Vector2f();
        if (horizontal)
            direction.x = 1f / dimension;
        else
            direction.y = 1f / dimension;
        loadVector("unit", direction);
    }

    @Override
    protected void bindAttributes() {
        super.bindFragOutput(0, "out_colour");
        super.bindAttribute(0, "position");
    }

    public static BlurShader getInstance() {
        return instance;
    }

}
