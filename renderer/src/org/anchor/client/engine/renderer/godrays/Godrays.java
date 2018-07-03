package org.anchor.client.engine.renderer.godrays;

import org.anchor.client.engine.renderer.Engine;
import org.anchor.client.engine.renderer.QuadRenderer;
import org.anchor.client.engine.renderer.Renderer;
import org.anchor.client.engine.renderer.types.Framebuffer;
import org.anchor.client.engine.renderer.types.light.Light;
import org.anchor.client.engine.renderer.types.light.LightType;
import org.anchor.engine.common.utils.Projection;
import org.anchor.engine.common.utils.VectorUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

public class Godrays {

    protected Framebuffer outputFBO;
    protected GodraysShader shader;
    protected Projection projection;

    public Godrays() {
        outputFBO = new Framebuffer(Display.getWidth(), Display.getHeight(), Framebuffer.NONE);
        shader = GodraysShader.getInstance();
        projection = new Projection(Renderer.getProjectionMatrix());
    }

    public void perform(int scene, int godraysTexture, int exposureTexture, Matrix4f viewMatrix, Light light) {
        outputFBO.bindFramebuffer();
        shader.start();
        QuadRenderer.bind();

        Engine.bind2DTexture(scene, 0);
        Engine.bind2DTexture(godraysTexture, 1);
        Engine.bind2DTexture(exposureTexture, 2);

        if (light != null) {
            Vector3f position = light.getPosition();
            if (light.getLightType() == LightType.DIRECTIONAL)
                position = VectorUtils.mul(light.getDirection(), 20000);

            shader.loadInformation(100, projection.update(position, viewMatrix));
        } else {
            shader.loadInformation(0, new Vector2f(0, 0));
        }

        QuadRenderer.render();
        QuadRenderer.unbind();
        shader.stop();
        outputFBO.unbindFramebuffer();
    }

    public void shutdown() {
        shader.shutdown();
        outputFBO.shutdown();
    }

    public Framebuffer getOutputFBO() {
        return outputFBO;
    }

}
