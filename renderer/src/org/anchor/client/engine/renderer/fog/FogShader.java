package org.anchor.client.engine.renderer.fog;

import org.anchor.client.engine.renderer.Settings;
import org.anchor.client.engine.renderer.Shader;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

public class FogShader extends Shader {

    private static FogShader instance = new FogShader();

    public FogShader() {
        super("fog");
    }

    @Override
    public void start() {
        super.start();

        loadInt("scene", 0);
        loadInt("depthMap", 1);
    }

    public void loadInformation(Vector3f baseColour, Vector3f sunDirection, Vector3f sunColour, Matrix4f viewMatrix) {
        loadFloat("density", Settings.density);
        loadFloat("gradient", Settings.gradient);
        loadVector("skyColour", baseColour);

        loadAs3DVector("sunDirection", Matrix4f.transform(viewMatrix, new Vector4f(sunDirection.x, sunDirection.y, sunDirection.z, 0), null));
        loadVector("sunColour", sunColour);
    }

    @Override
    protected void bindAttributes() {
        bindFragOutput(0, "out_colour");

        bindAttribute(0, "position");
    }

    public static FogShader getInstance() {
        return instance;
    }

}
