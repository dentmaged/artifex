package org.anchor.client.engine.renderer.autoexposure;

import org.anchor.client.engine.renderer.Graphics;
import org.anchor.client.engine.renderer.QuadRenderer;
import org.anchor.client.engine.renderer.gui.GUIRenderer;
import org.anchor.client.engine.renderer.types.Framebuffer;
import org.anchor.engine.common.utils.Mathf;
import org.lwjgl.opengl.Display;

public class AutoExposure {

    protected Framebuffer previous;
    protected Framebuffer exposure;

    protected AutoExposureShader autoExposureShader;

    public AutoExposure() {
        autoExposureShader = AutoExposureShader.getInstance();

        previous = new Framebuffer(1, 1, Framebuffer.NONE);
        exposure = new Framebuffer(1, 1, Framebuffer.NONE);
    }

    public void perform(int scene) {
        float mip = Mathf.floor(Mathf.log(Display.getWidth()) / Mathf.LOG_2);

        exposure.bindFramebuffer();
        autoExposureShader.start();
        autoExposureShader.loadInformation(mip);
        QuadRenderer.bind();

        Graphics.bind2DTexture(scene, 0);
        Graphics.bindColourTexture(previous, 1);
        QuadRenderer.render();

        QuadRenderer.unbind();
        autoExposureShader.stop();
        exposure.unbindFramebuffer();

        previous.bindFramebuffer();
        GUIRenderer.perform(scene, mip);
        previous.unbindFramebuffer();
    }

    public int getExposureTexture() {
        return exposure.getColourTexture();
    }

    public void shutdown() {
        previous.shutdown();
        exposure.shutdown();
    }

}
