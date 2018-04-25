package org.anchor.client.engine.renderer.shadows;

import org.anchor.client.engine.renderer.Shader;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;

public class ShadowShader extends Shader {

    private static ShadowShader instance = new ShadowShader();

    public ShadowShader() {
        super("shadows");
    }

    @Override
    public void start() {
        super.start();

        loadInt("modelTexture", 0);
    }

    public void load(Matrix4f shadowProjectionViewMatrix) {
        loadMatrix("shadowProjectionViewMatrix", shadowProjectionViewMatrix);
    }

    public void loadEntitySpecificInformation(Matrix4f transformationMatrix, float numberOfRows, Vector2f offset) {
        loadMatrix("transformationMatrix", transformationMatrix);
        loadFloat("numberOfRows", numberOfRows);
        loadVector("offset", offset);
    }

    @Override
    protected void bindAttributes() {
        super.bindFragOutput(0, "out_colour");

        super.bindAttribute(0, "position");
        super.bindAttribute(1, "textureCoordinates");
        super.bindAttribute(2, "normal");
    }

    public static ShadowShader getInstance() {
        return instance;
    }

}
