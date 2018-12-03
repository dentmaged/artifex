package org.anchor.client.engine.renderer.ssao;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.anchor.client.engine.renderer.Graphics;
import org.anchor.client.engine.renderer.QuadRenderer;
import org.anchor.client.engine.renderer.Settings;
import org.anchor.client.engine.renderer.blur.Blur;
import org.anchor.client.engine.renderer.types.Framebuffer;
import org.anchor.client.engine.renderer.types.ImageFormat;
import org.anchor.engine.common.console.CoreGameVariableManager;
import org.anchor.engine.common.console.IGameVariable;
import org.anchor.engine.common.utils.Mathf;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

public class SSAO {

    protected int noise;
    protected List<Vector3f> samples;

    protected Framebuffer outputFBO;
    protected Blur blur;
    protected SSAOShader shader;

    private IGameVariable r_performSSAO;

    public SSAO() {
        Random random = new Random();
        float data[] = new float[16 * 3];
        for (int i = 0; i < 16; i++) {
            float r = random.nextFloat() * 2 - 1;
            float g = random.nextFloat() * 2 - 1;

            data[i * 3] = r;
            data[i * 3 + 1] = g;
            data[i * 3 + 2] = 0;
        }

        noise = GL11.glGenTextures();

        FloatBuffer buffer = ByteBuffer.allocateDirect(data.length << 2).order(ByteOrder.nativeOrder()).asFloatBuffer();
        buffer.put(data).flip();

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, noise);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL30.GL_RGB32F, 4, 4, 0, GL11.GL_RGB, GL11.GL_FLOAT, buffer);

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);

        samples = new ArrayList<Vector3f>();
        for (int i = 0; i < 64; i++) {
            Vector3f sample = new Vector3f(random.nextFloat() * 2 - 1, random.nextFloat() * 2 - 1, random.nextFloat());
            sample.normalise();
            sample.scale(random.nextFloat());

            float scale = (float) i / 64f;
            scale = Mathf.lerp(0.1f, 1, scale * scale);
            sample.scale(scale);

            samples.add(sample);
        }

        outputFBO = new Framebuffer(Display.getWidth() / 4, Display.getHeight() / 4, Framebuffer.NONE, ImageFormat.R);
        blur = new Blur(ImageFormat.R);
        blur.setShader(SSAOBlurShader.getInstance());
        shader = SSAOShader.getInstance();

        shader.start();
        shader.loadInformation(new Vector2f(Display.getWidth() / 4, Display.getHeight() / 4), samples);
        shader.stop();

        r_performSSAO = CoreGameVariableManager.getByName("r_performSSAO");
    }

    public void perform(Matrix4f inverseViewMatrix, int depthMap, int normal) {
        outputFBO.bindFramebuffer();

        if (r_performSSAO.getValueAsBool()) {
            shader.start();
            shader.loadInverseViewMatrix(inverseViewMatrix);
            QuadRenderer.bind();

            Graphics.bind2DTexture(depthMap, 0);
            Graphics.bind2DTexture(normal, 1);
            Graphics.bind2DTexture(noise, 2);

            QuadRenderer.render();
            QuadRenderer.unbind();
            shader.stop();
        } else {
            GL11.glClearColor(1, 1, 1, 1);
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
            GL11.glClearColor(Settings.clearR, Settings.clearG, Settings.clearB, 1);
        }

        outputFBO.unbindFramebuffer();
        if (r_performSSAO.getValueAsBool())
            blur.perform(outputFBO.getColourTexture());
    }

    public int getAmbientOcclusionTexture() {
        return blur.getOutputFBO().getColourTexture();
    }

    public void shutdown() {
        outputFBO.shutdown();
        blur.shutdown();
    }

}
