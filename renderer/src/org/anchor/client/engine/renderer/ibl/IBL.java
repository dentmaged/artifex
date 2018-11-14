package org.anchor.client.engine.renderer.ibl;

import java.util.List;

import org.anchor.client.engine.renderer.Graphics;
import org.anchor.client.engine.renderer.QuadRenderer;
import org.anchor.client.engine.renderer.Settings;
import org.anchor.client.engine.renderer.deferred.DeferredShading;
import org.anchor.client.engine.renderer.pbr.BRDF;
import org.anchor.client.engine.renderer.types.ibl.LightProbe;
import org.anchor.client.engine.renderer.types.ibl.ReflectionProbe;
import org.anchor.engine.common.console.CoreGameVariableManager;
import org.anchor.engine.common.console.IGameVariable;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL40;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

public class IBL {

    protected DeferredShading deferred;
    protected BRDF brdf;
    protected IBLShader shader;

    private IGameVariable r_performLighting, r_showLightmaps;

    public IBL(DeferredShading deferred) {
        this.deferred = deferred;
        brdf = new BRDF();
        brdf.perform();
        shader = IBLShader.getInstance();

        r_performLighting = CoreGameVariableManager.getByName("r_performLighting");
        r_showLightmaps = CoreGameVariableManager.getByName("r_showLightmaps");
    }

    public void perform(Matrix4f viewMatrix, Matrix4f inverseViewMatrix, int irradiance, int prefilter, List<ReflectionProbe> reflectionProbes, List<LightProbe> lightProbes, Vector3f baseColour) {
        if (!r_performLighting.getValueAsBool() || r_showLightmaps.getValueAsBool())
            return;

        deferred.performSSAO(inverseViewMatrix);
        deferred.ibl();

        GL40.glBlendFunci(0, GL11.GL_ONE, GL11.GL_ONE);
        GL11.glEnable(GL11.GL_BLEND);
        Graphics.bindColourTexture(deferred.getOutputFBO(), 0);
        GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);

        GL11.glDepthMask(false);

        shader.start();
        shader.loadInformation(viewMatrix, inverseViewMatrix, reflectionProbes, lightProbes, baseColour);
        QuadRenderer.bind();

        Graphics.bindColourTexture(deferred.getOutputFBO(), 0);
        Graphics.bindColourTexture(deferred.getOtherFBO(), 1);
        Graphics.bindColourTexture(deferred.getNormalFBO(), 2);
        Graphics.bind2DTexture(deferred.getAmbientOcclusionTexture(), 3);
        Graphics.bindDepthMap(deferred.getOutputFBO(), 4);
        Graphics.bindColourTexture(brdf.getOutputFBO(), 5);
        Graphics.bindColourTexture(deferred.getAlbedoFBO(), 6);

        // Skybox
        Graphics.bindCubemap(prefilter, 8);
        Graphics.bindCubemap(irradiance, 12);

        for (int i = 0; i < reflectionProbes.size(); i++) {
            if (i + 1 >= Settings.maxProbes)
                break;

            Graphics.bindCubemap(reflectionProbes.get(i).getPrefilteredCubemap(), 9 + i);
        }

        for (int i = 0; i < lightProbes.size(); i++) {
            if (i + 1 >= Settings.maxProbes)
                break;

            Graphics.bindCubemap(lightProbes.get(i).getIrradianceCubemap(), 13 + i);
        }

        QuadRenderer.render();
        QuadRenderer.unbind();
        shader.stop();

        GL11.glDepthMask(true);
        GL11.glDisable(GL11.GL_BLEND);
    }

    public void shutdown() {
        shader.shutdown();
    }

}
