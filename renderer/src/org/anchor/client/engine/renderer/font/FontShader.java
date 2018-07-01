package org.anchor.client.engine.renderer.font;

import org.anchor.client.engine.renderer.Shader;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector4f;

public class FontShader extends Shader {

    private static FontShader instance = new FontShader();

    public FontShader() {
        super("font");
    }

    @Override
    public void start() {
        super.start();

        loadInt("font", 0);
    }

    @Override
    protected void bindAttributes() {
        super.bindFragOutput(0, "out_colour");

        super.bindAttribute(0, "position");
    }

    public void loadInformation(Matrix4f transformationMatrix, Vector4f colour, Vector4f uv) {
        loadMatrix("transformationMatrix", transformationMatrix);
        loadVector("colour", colour);
        loadVector("uv", uv);
    }

    public static FontShader getInstance() {
        return instance;
    }

}
