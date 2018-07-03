package org.anchor.client.engine.renderer.ssao;

import java.util.List;

import org.anchor.client.engine.renderer.Shader;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

public class SSAOShader extends Shader {

    private static SSAOShader instance = new SSAOShader();

    public SSAOShader() {
        super("ssao");
    }

    @Override
    protected void bindAttributes() {
        super.bindFragOutput(0, "out_colour");

        super.bindAttribute(0, "position");
    }

    @Override
    public void start() {
        super.start();

        loadInt("depthMap", 0);
        loadInt("normal", 1);
        loadInt("noise", 2);
    }

    public void loadInformation(Vector2f noiseTextureScale, List<Vector3f> samples) {
        loadVector("noiseTextureScale", noiseTextureScale);

        loadInt("kernelSize", samples.size());
        loadFloat("radius", 0.5f);
        loadFloat("bias", 0.025f);

        for (int i = 0; i < 64; i++)
            loadVector("samples[" + i + "]", samples.get(i));
    }

    public void loadInverseViewMatrix(Matrix4f inverseViewMatrix) {
        loadMatrix("inverseViewMatrix", inverseViewMatrix);
    }

    public static SSAOShader getInstance() {
        return instance;
    }

}
