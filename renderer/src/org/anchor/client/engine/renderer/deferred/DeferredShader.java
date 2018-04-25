package org.anchor.client.engine.renderer.deferred;

import java.util.List;

import org.anchor.client.engine.renderer.Shader;
import org.anchor.client.engine.renderer.types.Light;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

public class DeferredShader extends Shader {

    public static final int MAX_LIGHTS = 20;

    private static DeferredShader instance = new DeferredShader();

    public DeferredShader() {
        super("deferred");
    }

    @Override
    public void start() {
        super.start();

        loadInt("diffuse", 0);
        loadInt("position", 1);
        loadInt("normal", 2);
        loadInt("shadowMap", 3);
        loadInt("ssao", 4);
    }

    public void loadInformation(Matrix4f viewMatrix, Matrix4f inverseViewMatrix, List<Light> lights, boolean showLightmaps, float minDiffuse, float density, float gradient, Vector3f skyColour, Matrix4f toShadowMapSpace, float shadowDistance) {
        loadMatrix("inverseViewMatrix", inverseViewMatrix);
        loadBoolean("showLightmaps", showLightmaps);
        loadFloat("minDiffuse", minDiffuse);

        loadFloat("density", density);
        loadFloat("gradient", gradient);
        loadVector("skyColour", skyColour);

        loadMatrix("toShadowMapSpace", toShadowMapSpace);
        loadFloat("shadowDistance", shadowDistance);
        for (int i = 0; i < MAX_LIGHTS; i++) {
            if (i < lights.size()) {
                Vector3f position = lights.get(i).getPosition();
                super.loadVector("lightPosition[" + i + "]", new Vector3f(Matrix4f.transform(viewMatrix, new Vector4f(position.x, position.y, position.z, 1), null)));
                super.loadVector("lightColour[" + i + "]", lights.get(i).getColour());
                super.loadVector("attenuation[" + i + "]", lights.get(i).getAttenuation());
            } else {
                super.loadVector("lightPosition[" + i + "]", new Vector3f(0, 0, 0));
                super.loadVector("lightColour[" + i + "]", new Vector3f(0, 0, 0));
                super.loadVector("attenuation[" + i + "]", new Vector3f(1, 0, 0));
            }
        }
    }

    @Override
    protected void bindAttributes() {
        super.bindFragOutput(0, "out_diffuse");
        super.bindFragOutput(1, "out_position");
        super.bindFragOutput(2, "out_normal");
        super.bindFragOutput(3, "out_bloom");

        super.bindAttribute(0, "position");
    }

    public static DeferredShader getInstance() {
        return instance;
    }

}
