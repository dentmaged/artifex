package org.anchor.client.engine.renderer.ibl;

import java.util.List;

import org.anchor.client.engine.renderer.Settings;
import org.anchor.client.engine.renderer.Shader;
import org.anchor.client.engine.renderer.types.ibl.LightProbe;
import org.anchor.client.engine.renderer.types.ibl.ReflectionProbe;
import org.anchor.engine.common.utils.Mathf;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;

public class IBLShader extends Shader {

    private static IBLShader instance = new IBLShader();
    private Vector2f pcc = new Vector2f(0, 1);

    public IBLShader() {
        super("ibl");
    }

    @Override
    public void start() {
        super.start();

        loadInt("scene", 0);
        loadInt("other", 1);
        loadInt("normal", 2);
        loadInt("ssao", 3);
        loadInt("depthMap", 4);
        loadInt("brdf", 5);
        loadInt("albedo", 6);

        for (int i = 0; i < Settings.maxProbes; i++) {
            loadInt("prefilter[" + i + "]", 8 + i);
            loadInt("irradianceMap[" + i + "]", 12 + i);
        }
    }

    public void loadInformation(Matrix4f viewMatrix, Matrix4f inverseViewMatrix, List<ReflectionProbe> reflectionProbes, List<LightProbe> lightProbes) {
        loadMatrix("viewMatrix", viewMatrix);
        loadMatrix("inverseViewMatrix", inverseViewMatrix);

        loadFloat("irradianceScale", Settings.irradianceScale);
        loadFloat("ambientScale", Settings.ambientScale);
        loadFloat("mips", Mathf.floor(Mathf.log(Display.getWidth()) / Mathf.LOG_2) - 1);

        pcc.x = 0;
        loadVector("prefilterPccSize[0]", pcc);
        loadVector("irradiancePccSize[0]", pcc);
        for (int i = 0; i < Settings.maxProbes; i++) {
            if (i < reflectionProbes.size()) {
                ReflectionProbe probe = reflectionProbes.get(i);

                pcc.x = probe.getSize();
                loadVector("prefilterPccSize[" + (i + 1) + "]", pcc);
                loadVector("prefilterPccPosition[" + (i + 1) + "]", probe.getPosition());
            } else {
                pcc.y = 0;
                loadVector("prefilterPccSize[" + (i + 1) + "]", pcc);
                pcc.y = 1;
            }

            if (i < lightProbes.size()) {
                LightProbe probe = lightProbes.get(i);

                pcc.x = probe.getSize();
                loadVector("irradiancePccSize[" + (i + 1) + "]", pcc);
                loadVector("irradiancePccPosition[" + (i + 1) + "]", probe.getPosition());
            } else {
                pcc.y = 0;
                loadVector("irradiancePccSize[" + (i + 1) + "]", pcc);
                pcc.y = 1;
            }
        }
    }

    @Override
    protected void bindAttributes() {
        bindFragOutput(0, "out_colour");

        bindAttribute(0, "position");
    }

    public static IBLShader getInstance() {
        return instance;
    }

}
