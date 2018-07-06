package org.anchor.client.engine.renderer.types.cubemap;

import org.anchor.client.engine.renderer.Shader;
import org.lwjgl.util.vector.Matrix4f;

public class BakedShader extends Shader {

    protected BakedShader(String shader) {
        super(shader);
    }

    @Override
    protected void bindAttributes() {
        bindAttribute(0, "position");
        bindAttribute(1, "textureCoordinates");
        bindAttribute(2, "normal");

        bindFragOutput(0, "out_colour");
    }

    public void loadInformation(Matrix4f viewMatrix, Matrix4f transformationMatrix, float mip, int face) {
        loadMatrix("viewMatrix", viewMatrix);
        loadMatrix("transformationMatrix", transformationMatrix);
        loadFloat("mip", mip);
        loadFloat("face", face);

        loadInt("environment", 1);
    }

}
