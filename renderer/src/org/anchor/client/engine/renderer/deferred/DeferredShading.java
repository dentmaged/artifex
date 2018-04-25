package org.anchor.client.engine.renderer.deferred;

import java.util.List;

import org.anchor.client.engine.renderer.QuadRenderer;
import org.anchor.client.engine.renderer.shadows.ShadowFrustum;
import org.anchor.client.engine.renderer.shadows.Shadows;
import org.anchor.client.engine.renderer.ssao.SSAO;
import org.anchor.client.engine.renderer.types.Framebuffer;
import org.anchor.client.engine.renderer.types.Light;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

public class DeferredShading {

    protected Framebuffer multisampleFBO, diffuseFBO, positionFBO, normalFBO, bloomFBO, godraysFBO;
    protected SSAO ssao;
    protected DeferredShader shader;

    public static boolean showLightmaps = false;
    public static float minDiffuse = 0.1f;
    public static float density = 0.0035f;
    public static float gradient = 5;

    public DeferredShading() {
        multisampleFBO = new Framebuffer(Display.getWidth(), Display.getHeight());
        diffuseFBO = new Framebuffer(Display.getWidth(), Display.getHeight(), Framebuffer.DEPTH_TEXTURE);
        positionFBO = new Framebuffer(Display.getWidth(), Display.getHeight(), Framebuffer.NONE);
        normalFBO = new Framebuffer(Display.getWidth(), Display.getHeight(), Framebuffer.NONE);

        bloomFBO = new Framebuffer(Display.getWidth(), Display.getHeight(), Framebuffer.NONE);
        godraysFBO = new Framebuffer(Display.getWidth(), Display.getHeight(), Framebuffer.NONE);

        ssao = new SSAO();
        shader = DeferredShader.getInstance();
    }

    public void start() {
        multisampleFBO.bindFrameBuffer();
        GL11.glDisable(GL11.GL_BLEND);
    }

    public void stop(Matrix4f viewMatrix, Matrix4f inverseViewMatrix, List<Light> lights, Vector3f skyColour, Shadows shadows) {
        resolve(diffuseFBO, positionFBO, normalFBO);

        ssao.perform(inverseViewMatrix, positionFBO.getColourTexture(), normalFBO.getColourTexture());

        multisampleFBO.bindFrameBuffer();

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        GL11.glDepthMask(false);

        shader.start();
        shader.loadInformation(viewMatrix, inverseViewMatrix, lights, showLightmaps, minDiffuse, density, gradient, skyColour, shadows.getToShadowMapSpaceMatrix(), ShadowFrustum.SHADOW_DISTANCE);
        QuadRenderer.bind();

        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, diffuseFBO.getColourTexture());

        GL13.glActiveTexture(GL13.GL_TEXTURE1);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, positionFBO.getColourTexture());

        GL13.glActiveTexture(GL13.GL_TEXTURE2);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, normalFBO.getColourTexture());

        GL13.glActiveTexture(GL13.GL_TEXTURE3);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, shadows.getPCFShadowMap());

        GL13.glActiveTexture(GL13.GL_TEXTURE4);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, ssao.getAmbientOcclusionTexture());

        QuadRenderer.render();
        QuadRenderer.unbind();
        shader.stop();

        GL11.glDepthMask(true);
        GL11.glDisable(GL11.GL_BLEND);
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
        positionFBO.shutdown();
        normalFBO.shutdown();
        bloomFBO.shutdown();
        godraysFBO.shutdown();
    }

    public void reflective() {
        resolve(GL30.GL_COLOR_ATTACHMENT0, diffuseFBO);
        multisampleFBO.bindFrameBuffer();
    }

    public void output() {
        resolve(GL30.GL_COLOR_ATTACHMENT0, diffuseFBO);
        resolve(GL30.GL_COLOR_ATTACHMENT3, bloomFBO);
        resolve(GL30.GL_COLOR_ATTACHMENT4, godraysFBO);
    }

    public Framebuffer getOutputFBO() {
        return diffuseFBO;
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

}
