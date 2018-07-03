package org.anchor.client.engine.renderer.deferred;

import java.util.List;

import org.anchor.client.engine.renderer.Settings;
import org.anchor.client.engine.renderer.Shader;
import org.anchor.client.engine.renderer.shadows.Shadows;
import org.anchor.client.engine.renderer.types.light.Light;
import org.anchor.client.engine.renderer.types.light.LightType;
import org.anchor.engine.common.utils.Mathf;
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
        loadInt("other", 1);
        loadInt("normal", 2);
        loadInt("bloom", 3);
        loadInt("godrays", 4);
        loadInt("depthMap", 5);
        loadInt("ssao", 6);
        loadInt("scene", 7);
        loadInt("skybox", 8);
        loadInt("irradianceMap", 9);
        loadInt("prefilter", 10);
        loadInt("brdf", 11);

        for (int i = 0; i < Settings.shadowSplits; i++)
            loadInt("shadowMaps[" + i + "]", 12 + i);
    }

    public void loadInformation(Matrix4f viewMatrix, Matrix4f inverseViewMatrix, List<Light> lights, Vector3f baseColour, Vector3f topColour, Shadows shadows) {
        loadMatrix("viewMatrix", viewMatrix);
        loadMatrix("inverseViewMatrix", inverseViewMatrix);
        loadBoolean("showLightmaps", Settings.showLightmaps);
        loadFloat("minDiffuse", Settings.minDiffuse);

        loadFloat("density", Settings.density);
        loadFloat("gradient", Settings.gradient);
        loadVector("skyColour", baseColour);

        loadVector("baseColour", baseColour);
        loadVector("topColour", topColour);
        loadBoolean("proceduralSky", Settings.proceduralSky);

        for (int i = 0; i < Settings.shadowSplits; i++) {
            loadMatrix("toShadowMapSpaces[" + i + "]", shadows.getToShadowMapSpaceMatrix(i));
            loadFloat("shadowDistances[" + i + "]", shadows.getFarPlane(i) / 2f);
        }

        for (int i = 0; i < DeferredShader.MAX_LIGHTS; i++) {
            if (i < lights.size()) {
                Light light = lights.get(i);

                Vector3f position = light.getPosition();
                Vector3f direction = light.getDirection();

                Vector4f v = Matrix4f.transform(viewMatrix, new Vector4f(position.x, position.y, position.z, 1), null);
                if (light.getLightType() == LightType.POINT)
                    v.w = 0;
                else if (light.getLightType() == LightType.DIRECTIONAL)
                    v.w = 1;
                else
                    v.w = 2;

                loadVector("lightPosition[" + i + "]", v);
                loadVector("lightColour[" + i + "]", light.getColour());
                loadVector("attenuation[" + i + "]", light.getAttenuation());
                loadAs3DVector("lightDirection[" + i + "]", Matrix4f.transform(viewMatrix, new Vector4f(direction.x, direction.y, direction.z, 0), null));
                loadVector("lightCutoff[" + i + "]", Mathf.cos(Mathf.toRadians(light.getCutoff())), Mathf.cos(Mathf.toRadians(light.getOuterCutoff()))); // performance
            } else {
                loadVector("lightPosition[" + i + "]", 0, 0, 0, 0);
                loadVector("lightColour[" + i + "]", 0, 0, 0);
                loadVector("attenuation[" + i + "]", 1, 0, 0);
                loadVector("lightDirection[" + i + "]", 0, -1, 0);
                loadVector("lightCutoff[" + i + "]", 35, 45);
            }
        }
    }

    @Override
    protected void bindAttributes() {
        super.bindFragOutput(0, "out_diffuse");
        super.bindFragOutput(1, "out_other");
        super.bindFragOutput(2, "out_normal");
        super.bindFragOutput(3, "out_bloom");

        super.bindAttribute(0, "position");
    }

    public static DeferredShader getInstance() {
        return instance;
    }

}
