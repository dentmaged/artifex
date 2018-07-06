package org.anchor.client.engine.renderer.autoexposure;

import java.util.ArrayList;
import java.util.List;

import org.anchor.client.engine.renderer.Graphics;
import org.anchor.client.engine.renderer.QuadRenderer;
import org.anchor.client.engine.renderer.Settings;
import org.anchor.client.engine.renderer.gui.GUIShader;
import org.anchor.client.engine.renderer.types.Framebuffer;
import org.anchor.engine.common.utils.CoreMaths;
import org.lwjgl.util.vector.Vector2f;

public class AutoExposure {

    protected List<Framebuffer> framebuffers;
    protected Framebuffer previous;
    protected Framebuffer exposure;

    protected GUIShader pingpongShader;
    protected AutoExposureShader autoExposureShader;

    public AutoExposure() {
        int width = Settings.width;
        int height = Settings.height;
        int maxSize = Math.max(Settings.width, Settings.height);

        pingpongShader = GUIShader.getInstance();
        autoExposureShader = AutoExposureShader.getInstance();

        previous = new Framebuffer(1, 1, Framebuffer.NONE);
        exposure = new Framebuffer(1, 1, Framebuffer.NONE);
        framebuffers = new ArrayList<Framebuffer>();
        while (maxSize > 1) {
            width = Math.max(width / 2, 1);
            height = Math.max(height / 2, 1);

            framebuffers.add(new Framebuffer(width, height, Framebuffer.NONE));
            maxSize = Math.max(width, height);
        }
    }

    public void perform(int scene) {
        pingpongShader.start();
        QuadRenderer.bind();
        pingpongShader.loadInformation(CoreMaths.createTransformationMatrix(new Vector2f(), new Vector2f(1, 1), 0, 0));

        previous.bindFramebuffer();
        Graphics.bind2DTexture(framebuffers.get(framebuffers.size() - 1).getColourTexture(), 0);
        QuadRenderer.render();

        for (int i = 0; i < framebuffers.size(); i++) {
            framebuffers.get(i).bindFramebuffer();

            Graphics.bind2DTexture(i == 0 ? scene : framebuffers.get(i - 1).getColourTexture(), 0);
            QuadRenderer.render();
        }
        pingpongShader.stop();

        exposure.bindFramebuffer();
        autoExposureShader.start();
        autoExposureShader.loadInformation();

        Graphics.bindColourTexture(framebuffers.get(framebuffers.size() - 1), 0);
        Graphics.bindColourTexture(previous, 1);
        QuadRenderer.render();

        autoExposureShader.stop();
        exposure.unbindFramebuffer();
        QuadRenderer.unbind();
    }

    public int getExposureTexture() {
        return exposure.getColourTexture();
    }

    public void shutdown() {
        pingpongShader.shutdown();
        autoExposureShader.shutdown();

        previous.shutdown();
        exposure.shutdown();
        for (Framebuffer framebuffer : framebuffers)
            framebuffer.shutdown();
    }

}
