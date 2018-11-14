package org.anchor.game.client.shaders;

public class ParticleShader extends ModelShader {

    private static ParticleShader instance = new ParticleShader();

    protected ParticleShader() {
        super("particle");
    }

    public void loadRows(int rows) {
        loadFloat("rows", rows);
    }

    @Override
    protected void bindAttributes() {
        bindFragOutput(0, "out_diffuse");
        bindFragOutput(1, "out_other");
        bindFragOutput(2, "out_normal");
        bindFragOutput(3, "out_bloom");
        bindFragOutput(4, "out_godrays");

        bindAttribute(0, "position");
        bindAttribute(1, "projectionViewTransformationMatrix");
        bindAttribute(5, "textureOffsets");
        bindAttribute(6, "blendFactor");
    }

    public static ParticleShader getInstance() {
        return instance;
    }

}
