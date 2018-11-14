package org.anchor.client.engine.renderer.types.cubemap;

import java.util.HashMap;
import java.util.Map;

import org.anchor.client.engine.renderer.Graphics;
import org.anchor.client.engine.renderer.Renderer;
import org.anchor.client.engine.renderer.Settings;
import org.anchor.engine.common.utils.CoreMaths;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

public class BakedCubemap {

    private CubemapFramebuffer cubemap;
    private BakedShader shader;
    private int mipmaps, size;

    private static Map<String, BakedShader> shaders = new HashMap<String, BakedShader>();

    public BakedCubemap(int size, String shader, int mipmaps) {
        this(size, getShader(shader), mipmaps);
    }

    public BakedCubemap(int size, BakedShader shader, int mipmaps) {
        this.cubemap = new CubemapFramebuffer(size, mipmaps);
        this.shader = shader;
        this.size = size;
        this.mipmaps = mipmaps;
    }

    public void perform(int skybox) {
        float fov = Settings.fov;
        Settings.fov = 90;
        Settings.width = size;
        Settings.height = size;
        Renderer.refreshProjectionMatrix();

        cubemap.bindFramebuffer();
        shader.start();

        Renderer.bind(Renderer.getCubeModel());
        GL11.glDisable(GL11.GL_CULL_FACE);
        Graphics.bindCubemap(skybox, 1);

        for (int mip = 0; mip < mipmaps; mip++) {
            cubemap.startMipmapRender(mip);

            for (int i = 0; i < 6; i++) {
                cubemap.startFaceRender(i, mip);
                shader.loadInformation(CoreMaths.createViewMatrix(new Vector3f(), cubemap.getPitch(i), cubemap.getYaw(i), 180), new Matrix4f(), mipmaps > 1 ? (float) mip / (float) (mipmaps - 1) : 0, i);

                Renderer.render(Renderer.getCubeModel());
            }
        }

        Renderer.unbind(Renderer.getCubeModel());
        shader.stop();
        cubemap.unbindFramebuffer();

        Settings.fov = fov;
        Settings.width = Display.getWidth();
        Settings.height = Display.getHeight();
        Renderer.refreshProjectionMatrix();
    }

    public int getTexture() {
        return cubemap.getTexture();
    }

    public static BakedShader getShader(String shader) {
        if (!shaders.containsKey(shader))
            shaders.put(shader, new BakedShader(shader));

        return shaders.get(shader);
    }

    public void shutdown() {
        cubemap.shutdown();
    }

}
