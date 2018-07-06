package org.anchor.client.engine.renderer.deferred;

import java.util.List;

import org.anchor.client.engine.renderer.Graphics;
import org.anchor.client.engine.renderer.QuadRenderer;
import org.anchor.client.engine.renderer.Settings;
import org.anchor.client.engine.renderer.pbr.BRDF;
import org.anchor.client.engine.renderer.shadows.Shadows;
import org.anchor.client.engine.renderer.ssao.SSAO;
import org.anchor.client.engine.renderer.types.Framebuffer;
import org.anchor.client.engine.renderer.types.light.Light;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

public class DeferredShading {

    protected Framebuffer multisampleFBO, diffuseFBO, otherFBO, normalFBO, bloomFBO, godraysFBO;
    protected SSAO ssao;
    protected BRDF brdf;
    protected DeferredShader shader;

    public DeferredShading() {
        multisampleFBO = new Framebuffer(Display.getWidth(), Display.getHeight(), true);
        diffuseFBO = new Framebuffer(Display.getWidth(), Display.getHeight(), Framebuffer.DEPTH_TEXTURE, true);
        otherFBO = new Framebuffer(Display.getWidth(), Display.getHeight(), Framebuffer.NONE, true);
        normalFBO = new Framebuffer(Display.getWidth(), Display.getHeight(), Framebuffer.NONE, true);

        bloomFBO = new Framebuffer(Display.getWidth(), Display.getHeight(), Framebuffer.NONE);
        godraysFBO = new Framebuffer(Display.getWidth(), Display.getHeight(), Framebuffer.NONE);

        ssao = new SSAO();
        brdf = new BRDF();
        brdf.perform();
        shader = DeferredShader.getInstance();
    }

    public void start() {
        multisampleFBO.bindFramebuffer();
        GL11.glDisable(GL11.GL_BLEND);
    }

    public void stop(Framebuffer sceneFBO, Matrix4f viewMatrix, Matrix4f inverseViewMatrix, List<Light> lights, Vector3f baseColour, Vector3f topColour, int skybox, int irradiance, int prefilter, Shadows shadows) {
        if (!Settings.performLighting)
            return;

        resolve(diffuseFBO, otherFBO, normalFBO, bloomFBO, godraysFBO);
        ssao.perform(inverseViewMatrix, diffuseFBO.getDepthTexture(), normalFBO.getColourTexture());
        multisampleFBO.bindFramebuffer();

        GL11.glDepthMask(false);

        shader.start();
        shader.loadInformation(viewMatrix, inverseViewMatrix, lights, baseColour, topColour, shadows);
        QuadRenderer.bind();

        Graphics.bindColourTexture(diffuseFBO, 0);
        Graphics.bindColourTexture(otherFBO, 1);
        Graphics.bindColourTexture(normalFBO, 2);
        Graphics.bindColourTexture(bloomFBO, 3);
        Graphics.bindColourTexture(godraysFBO, 4);
        Graphics.bindDepthTexture(diffuseFBO, 5);
        Graphics.bind2DTexture(ssao.getAmbientOcclusionTexture(), 6);
        Graphics.bindColourTexture(sceneFBO, 7);
        Graphics.bindCubemap(skybox, 8);
        Graphics.bindCubemap(irradiance, 9);
        Graphics.bindCubemap(prefilter, 10);
        Graphics.bindColourTexture(brdf.getOutputFBO(), 11);

        for (int i = 0; i < Settings.shadowSplits; i++)
            Graphics.bind2DTexture(shadows.getPCFShadowMap(i), 12 + i);

        QuadRenderer.render();
        QuadRenderer.unbind();
        shader.stop();

        GL11.glDepthMask(true);
    }

    public void resolve(Framebuffer... fbos) {
        for (int i = 0; i < fbos.length; i++)
            resolve(GL30.GL_COLOR_ATTACHMENT0 + i, fbos[i]);
    }

    public void resolve(int attachment, Framebuffer fbo) {
        if (attachment == GL30.GL_COLOR_ATTACHMENT0)
            GL11.glEnable(GL13.GL_MULTISAMPLE);

        multisampleFBO.resolveToFBO(attachment, fbo);

        if (attachment == GL30.GL_COLOR_ATTACHMENT0)
            GL11.glDisable(GL13.GL_MULTISAMPLE);
    }

    public void shutdown() {
        shader.shutdown();

        multisampleFBO.shutdown();
        diffuseFBO.shutdown();
        otherFBO.shutdown();
        normalFBO.shutdown();
        bloomFBO.shutdown();
        godraysFBO.shutdown();
    }

    public void decals() {
        resolve(GL30.GL_COLOR_ATTACHMENT0, diffuseFBO);
        multisampleFBO.bindFramebuffer();
    }

    public void reflective() {
        resolve(GL30.GL_COLOR_ATTACHMENT0, diffuseFBO);
        multisampleFBO.bindFramebuffer();
    }

    public void output() {
        resolve(GL30.GL_COLOR_ATTACHMENT0, diffuseFBO);
        resolve(GL30.GL_COLOR_ATTACHMENT3, bloomFBO);
        resolve(GL30.GL_COLOR_ATTACHMENT4, godraysFBO);
    }

    public Framebuffer getOutputFBO() {
        return diffuseFBO;
    }

    public Framebuffer getPositionFBO() {
        return otherFBO;
    }

    public Framebuffer getBloomFBO() {
        return bloomFBO;
    }

    public Framebuffer getGodraysFBO() {
        return godraysFBO;
    }

    public int getAmbientOcclusionTexture() {
        return ssao.getAmbientOcclusionTexture();
    }

    public int getBRDF() {
        return brdf.getOutputFBO().getColourTexture();
    }

}
