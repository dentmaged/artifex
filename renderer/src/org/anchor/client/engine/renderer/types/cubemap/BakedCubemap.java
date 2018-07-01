package org.anchor.client.engine.renderer.types.cubemap;

import org.anchor.client.engine.renderer.Engine;
import org.anchor.client.engine.renderer.Renderer;
import org.anchor.client.engine.renderer.Settings;
import org.anchor.client.engine.renderer.types.Model;
import org.anchor.engine.common.utils.CoreMaths;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

public class BakedCubemap {

    private CubemapFramebuffer cubemap;
    private BakedShader shader;
    private int mipmaps, size;

    public BakedCubemap(int size, String shader, int mipmaps) {
        this(size, new BakedShader(shader), mipmaps);
    }

    public BakedCubemap(int size, BakedShader shader, int mipmaps) {
        this.cubemap = new CubemapFramebuffer(size, mipmaps);
        this.shader = shader;
        this.size = size;
        this.mipmaps = mipmaps;
    }

    public void perform(Model model, int skybox) {
        float fov = Settings.fov;
        Settings.fov = 90;
        Settings.width = size;
        Settings.height = size;
        Renderer.refreshProjectionMatrix();

        cubemap.bindFramebuffer();
        shader.start();
        Renderer.bind(model);

        Engine.bindCubemap(skybox, 1);
        for (int mip = 0; mip < mipmaps; mip++) {
            cubemap.startMipmapRender(mip);

            for (int i = 0; i < 6; i++) {
                cubemap.startFaceRender(i, mip);
                shader.loadInformation(CoreMaths.createViewMatrix(new Vector3f(), cubemap.getPitch(i), cubemap.getYaw(i), 180), new Matrix4f(), mipmaps > 1 ? (float) mip / (float) (mipmaps - 1) : 0, i);

                Renderer.render(model);
            }
        }

        Renderer.unbind(model);
        shader.stop();
        cubemap.unbindFramebuffer();

        Settings.fov = fov;
        Settings.width = Display.getWidth();
        Settings.height = Display.getHeight();
        Renderer.refreshProjectionMatrix();
    }

    public int getCubemap() {
        return cubemap.getTexture();
    }

}
