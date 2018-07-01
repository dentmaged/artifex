package org.anchor.client.engine.renderer.types.cubemap;

import org.anchor.client.engine.renderer.Shader;
import org.lwjgl.util.vector.Matrix4f;

public class BakedShader extends Shader {

    protected BakedShader(String shader) {
        super(shader);
    }

    @Override
    protected void bindAttributes() {
        super.bindAttribute(0, "position");
        super.bindAttribute(1, "textureCoordinates");
        super.bindAttribute(2, "normal");

        super.bindFragOutput(0, "out_colour");
    }

    public void loadInformation(Matrix4f viewMatrix, Matrix4f transformationMatrix, float mip, int face) {
        loadMatrix("viewMatrix", viewMatrix);
        loadMatrix("transformationMatrix", transformationMatrix);
        loadFloat("mip", mip);
        loadFloat("face", face);

        loadInt("environment", 1);
    }

}
