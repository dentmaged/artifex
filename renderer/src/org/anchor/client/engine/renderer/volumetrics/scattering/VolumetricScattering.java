package org.anchor.client.engine.renderer.volumetrics.scattering;

import java.util.List;

import org.anchor.client.engine.renderer.Graphics;
import org.anchor.client.engine.renderer.QuadRenderer;
import org.anchor.client.engine.renderer.blur.Blur;
import org.anchor.client.engine.renderer.gui.GUIRenderer;
import org.anchor.client.engine.renderer.shadows.Shadows;
import org.anchor.client.engine.renderer.ssao.SSAOBlurShader;
import org.anchor.client.engine.renderer.types.Framebuffer;
import org.anchor.client.engine.renderer.types.light.Light;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL40;
import org.lwjgl.util.vector.Matrix4f;

public class VolumetricScattering {

    protected Framebuffer calculationFBO, outputFBO;
    protected Blur blur;
    protected VolumetricScatteringShader shader;

    public VolumetricScattering() {
        calculationFBO = new Framebuffer(Display.getWidth() / 2, Display.getHeight() / 2, Framebuffer.NONE);
        outputFBO = new Framebuffer(Display.getWidth(), Display.getHeight(), Framebuffer.NONE);

        shader = VolumetricScatteringShader.getInstance();
        blur = new Blur(Display.getWidth() / 2, Display.getHeight() / 2);
        blur.setShader(SSAOBlurShader.getInstance());
    }

    public void perform(int scene, int depthMap, int exposure, int normal, List<Light> lights, Matrix4f viewMatrix, Shadows shadows, float gScattering) {
        GL11.glDisable(GL11.GL_CULL_FACE);
        calculationFBO.bindFramebuffer();
        shader.start();
        QuadRenderer.bind();

        GL11.glEnable(GL11.GL_BLEND);
        GL40.glBlendFunci(0, GL11.GL_ONE, GL11.GL_ONE);

        Graphics.bind2DTexture(depthMap, 0);
        Graphics.bind2DTexture(shadows.getShadowMap(0), 1);
        Graphics.bind2DTexture(exposure, 2);
        Graphics.bind2DTexture(normal, 3);

        GL11.glClearColor(0, 0, 0, 1);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
        for (Light light : lights) {
            if (light.getVolumetricStrength() <= 0)
                continue;

            shader.loadInformation(light, viewMatrix, shadows.getToShadowMapSpaceMatrix(0), gScattering);
            QuadRenderer.render();
        }
        GL11.glDisable(GL11.GL_BLEND);

        QuadRenderer.unbind();
        shader.stop();
        calculationFBO.unbindFramebuffer();

        blur.perform(calculationFBO.getColourTexture());

        outputFBO.bindFramebuffer();
        QuadRenderer.bind();

        GUIRenderer.perform(scene);

        GL11.glEnable(GL11.GL_BLEND);
        GL40.glBlendFunci(0, GL11.GL_ONE, GL11.GL_ONE);
        GUIRenderer.perform(blur.getOutputFBO().getColourTexture());
        GL11.glDisable(GL11.GL_BLEND);

        QuadRenderer.unbind();
        outputFBO.unbindFramebuffer();
        GL11.glEnable(GL11.GL_CULL_FACE);
    }

    public void shutdown() {
        outputFBO.shutdown();
        calculationFBO.shutdown();
    }

    public Framebuffer getOutputFBO() {
        return outputFBO;
    }

}
