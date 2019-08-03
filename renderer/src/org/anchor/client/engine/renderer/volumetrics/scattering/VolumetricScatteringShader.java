package org.anchor.client.engine.renderer.volumetrics.scattering;

import org.anchor.client.engine.renderer.Settings;
import org.anchor.client.engine.renderer.Shader;
import org.anchor.client.engine.renderer.types.light.Light;
import org.anchor.client.engine.renderer.types.light.LightType;
import org.anchor.engine.common.utils.Mathf;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

public class VolumetricScatteringShader extends Shader {

    private static VolumetricScatteringShader instance = new VolumetricScatteringShader();

    public VolumetricScatteringShader() {
        super("volumetricScattering");
    }

    @Override
    public void start() {
        super.start();

        loadInt("depthMap", 0);
        loadInt("shadowMap", 1);
        loadInt("exposure", 2);
        loadInt("normal", 3);
    }

    public void loadInformation(Light light, Matrix4f viewMatrix, Matrix4f toShadowMapSpace) {
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

        loadVector("lightPosition", v);
        loadAs3DVector("lightDirection", Matrix4f.transform(viewMatrix, new Vector4f(direction.x, direction.y, direction.z, 0), null));
        loadVector("lightAttenuation", light.getAttenuation());
        loadVector("lightColour", light.getColour());
        loadVector("lightCutoff", Mathf.cos(Mathf.toRadians(light.getCutoff())), Mathf.cos(Mathf.toRadians(light.getOuterCutoff())));
        loadFloat("lightVolumetricStrength", light.getVolumetricStrength());
        loadFloat("G_SCATTERING", Settings.volumetricScattering);

        loadMatrix("toShadowMapSpace", toShadowMapSpace);
    }

    @Override
    protected void bindAttributes() {
        bindFragOutput(0, "out_colour");

        bindAttribute(0, "position");
    }

    public static VolumetricScatteringShader getInstance() {
        return instance;
    }

}
