package org.anchor.client.engine.renderer.deferred;

import java.util.List;

import org.anchor.client.engine.renderer.Graphics;
import org.anchor.client.engine.renderer.QuadRenderer;
import org.anchor.client.engine.renderer.Settings;
import org.anchor.client.engine.renderer.pbr.BRDF;
import org.anchor.client.engine.renderer.shadows.Shadows;
import org.anchor.client.engine.renderer.ssao.SSAO;
import org.anchor.client.engine.renderer.types.Framebuffer;
import org.anchor.client.engine.renderer.types.ImageFormat;
import org.anchor.client.engine.renderer.types.light.Light;
import org.anchor.engine.common.console.CoreGameVariableManager;
import org.anchor.engine.common.console.IGameVariable;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Matrix4f;

public class DeferredShading {

    protected Framebuffer multisampleFBO, diffuseFBO, otherFBO, normalFBO, albedoFBO;
    protected SSAO ssao;
    protected BRDF brdf;
    protected DeferredShader shader;

    private IGameVariable r_performLighting;

    public DeferredShading() {
        multisampleFBO = new Framebuffer(Display.getWidth(), Display.getHeight(), ImageFormat.RGBA16F);
        diffuseFBO = new Framebuffer(Display.getWidth(), Display.getHeight(), Framebuffer.DEPTH_TEXTURE, ImageFormat.RGBA16F, true);
        otherFBO = new Framebuffer(Display.getWidth(), Display.getHeight(), Framebuffer.NONE, ImageFormat.RGBA16F);
        normalFBO = new Framebuffer(Display.getWidth(), Display.getHeight(), Framebuffer.NONE, ImageFormat.RGBA16F);
        albedoFBO = new Framebuffer(Display.getWidth(), Display.getHeight(), Framebuffer.NONE);

        ssao = new SSAO();
        brdf = new BRDF();
        brdf.perform();
        shader = DeferredShader.getInstance();

        r_performLighting = CoreGameVariableManager.getByName("r_performLighting");
    }

    public void start() {
        multisampleFBO.bindFramebuffer();
        GL11.glDisable(GL11.GL_BLEND);
    }

    public void stop(Matrix4f viewMatrix, Matrix4f inverseViewMatrix, List<Light> lights, Shadows shadows) {
        if (!r_performLighting.getValueAsBool())
            return;

        GL30.glColorMaski(3, false, false, false, false);
        resolve(diffuseFBO, otherFBO, normalFBO);
        multisampleFBO.bindFramebuffer();

        GL11.glDepthMask(false);

        shader.start();
        shader.loadInformation(viewMatrix, inverseViewMatrix, lights, shadows);
        QuadRenderer.bind();

        // Deferred
        Graphics.bindColourTexture(diffuseFBO, 0);
        Graphics.bindColourTexture(otherFBO, 1);
        Graphics.bindColourTexture(normalFBO, 2);
        Graphics.bindDepthMap(diffuseFBO, 3);

        for (int i = 0; i < Settings.shadowSplits; i++)
            Graphics.bind2DTexture(shadows.getShadowMap(i), 13 + i);

        QuadRenderer.render();
        QuadRenderer.unbind();
        shader.stop();

        GL11.glDepthMask(true);
        GL30.glColorMaski(3, true, true, true, true);
    }

    public void performSSAO(Matrix4f inverseViewMatrix) {
        ssao.perform(inverseViewMatrix, diffuseFBO.getDepthTexture(), normalFBO.getColourTexture());
    }

    public void resolve(Framebuffer... fbos) {
        for (int i = 0; i < fbos.length; i++)
            resolve(GL30.GL_COLOR_ATTACHMENT0 + i, fbos[i]);
    }

    public void resolve(int attachment, Framebuffer fbo) {
        multisampleFBO.resolveToFBO(attachment, fbo);
    }

    public void shutdown() {
        shader.shutdown();

        multisampleFBO.shutdown();
        diffuseFBO.shutdown();
        otherFBO.shutdown();
        normalFBO.shutdown();
        albedoFBO.shutdown();
    }

    public void decals() {
        resolve(GL30.GL_COLOR_ATTACHMENT0, diffuseFBO);
        multisampleFBO.bindFramebuffer();
    }

    public void ibl() {
        resolve(diffuseFBO, otherFBO, normalFBO, albedoFBO);
        multisampleFBO.bindFramebuffer();
    }

    public void output() {
        resolve(GL30.GL_COLOR_ATTACHMENT0, diffuseFBO);
    }

    public Framebuffer getOutputFBO() {
        return diffuseFBO;
    }

    public Framebuffer getOtherFBO() {
        return otherFBO;
    }

    public Framebuffer getAlbedoFBO() {
        return albedoFBO;
    }

    public Framebuffer getNormalFBO() {
        return normalFBO;
    }

    public int getAmbientOcclusionTexture() {
        return ssao.getAmbientOcclusionTexture();
    }

    public int getBRDF() {
        return brdf.getOutputFBO().getColourTexture();
    }

}
