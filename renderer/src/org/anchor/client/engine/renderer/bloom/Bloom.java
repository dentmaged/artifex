package org.anchor.client.engine.renderer.bloom;

import org.anchor.client.engine.renderer.Graphics;
import org.anchor.client.engine.renderer.QuadRenderer;
import org.anchor.client.engine.renderer.autoexposure.AutoExposure;
import org.anchor.client.engine.renderer.blur.Blur;
import org.anchor.client.engine.renderer.types.Framebuffer;
import org.lwjgl.opengl.Display;

public class Bloom {

    private Framebuffer outputFBO;
    protected BloomShader bloomShader;
    protected CombineShader combineShader;

    protected AutoExposure autoExposure;
    protected Blur blurOne, blurTwo, blurThree;

    public Bloom() {
        outputFBO = new Framebuffer(Display.getWidth(), Display.getHeight(), Framebuffer.NONE);
        bloomShader = BloomShader.getInstance();
        combineShader = CombineShader.getInstance();

        autoExposure = new AutoExposure();
        blurOne = new Blur(Display.getWidth() / 2, Display.getHeight() / 2);
        blurTwo = new Blur(Display.getWidth() / 4, Display.getHeight() / 4);
        blurThree = new Blur(Display.getWidth() / 8, Display.getHeight() / 8);
    }

    public void perform(int scene, int bloomTexture) {
        bloomShader.start();
        QuadRenderer.bind();
        Graphics.bind2DTexture(bloomTexture, 0);

        outputFBO.bindFramebuffer();
        QuadRenderer.render();
        outputFBO.unbindFramebuffer();

        QuadRenderer.unbind();
        bloomShader.stop();

        blurOne.perform(outputFBO.getColourTexture());
        blurTwo.perform(outputFBO.getColourTexture());
        blurThree.perform(outputFBO.getColourTexture());

        autoExposure.perform(scene);

        combineShader.start();
        QuadRenderer.bind();

        Graphics.bind2DTexture(scene, 0);
        Graphics.bindColourTexture(blurOne.getOutputFBO(), 1);
        Graphics.bindColourTexture(blurTwo.getOutputFBO(), 2);
        Graphics.bindColourTexture(blurThree.getOutputFBO(), 3);
        Graphics.bind2DTexture(autoExposure.getExposureTexture(), 4);

        outputFBO.bindFramebuffer();
        QuadRenderer.render();
        outputFBO.unbindFramebuffer();

        QuadRenderer.unbind();
        combineShader.stop();
    }

    public void shutdown() {
        outputFBO.shutdown();
        blurOne.shutdown();
        blurTwo.shutdown();
        blurThree.shutdown();
        bloomShader.shutdown();
        combineShader.shutdown();
    }

    public Framebuffer getOutputFBO() {
        return outputFBO;
    }

    public int getExposureTexture() {
        return autoExposure.getExposureTexture();
    }

}
