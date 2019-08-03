package org.anchor.client.engine.renderer.deferred;

import java.util.List;

import org.anchor.client.engine.renderer.Settings;
import org.anchor.client.engine.renderer.Shader;
import org.anchor.client.engine.renderer.types.light.Light;
import org.anchor.client.engine.renderer.types.light.LightType;
import org.anchor.engine.common.console.CoreGameVariableManager;
import org.anchor.engine.common.console.IGameVariable;
import org.anchor.engine.common.utils.Mathf;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

public class DeferredShader extends Shader {

    private static DeferredShader instance = new DeferredShader();

    private IGameVariable r_showLightmaps;

    public DeferredShader() {
        super("deferred");

        r_showLightmaps = CoreGameVariableManager.getByName("r_showLightmaps");
    }

    @Override
    public void start() {
        super.start();

        loadInt("diffuse", 0);
        loadInt("other", 1);
        loadInt("normal", 2);
        loadInt("depthMap", 3);
        loadInt("shadow", 4);
    }

    public void loadInformation(Matrix4f viewMatrix, Matrix4f inverseViewMatrix, List<Light> lights) {
        loadMatrix("viewMatrix", viewMatrix);
        loadMatrix("inverseViewMatrix", inverseViewMatrix);
        loadBoolean("showLightmaps", r_showLightmaps.getValueAsBool());

        for (int i = 0; i < Settings.maxLights; i++) {
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

                if (light.castsShadows())
                    v.w += 3;

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
        bindFragOutput(0, "out_diffuse");
        bindFragOutput(1, "out_other");
        bindFragOutput(2, "out_normal");
        bindFragOutput(3, "out_albedo");

        bindAttribute(0, "position");
    }

    public static DeferredShader getInstance() {
        return instance;
    }

}
