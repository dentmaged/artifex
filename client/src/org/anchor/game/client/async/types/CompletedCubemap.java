package org.anchor.game.client.async.types;

import java.awt.image.BufferedImage;

import org.anchor.client.engine.renderer.Loader;
import org.anchor.client.engine.renderer.types.Texture;
import org.anchor.client.engine.renderer.types.cubemap.CubemapRequest;

public class CompletedCubemap extends CompletedRequest {

    private CubemapRequest request;
    private BufferedImage[] data;

    public CompletedCubemap(CubemapRequest request, BufferedImage[] data) {
        this.request = request;
        this.data = data;
    }

    @Override
    public void load() {
        Texture[] textures = new Texture[data.length];
        for (int i = 0; i < textures.length; i++)
            textures[i] = new Texture(data[i]);

        request.setTexture(Loader.getInstance().loadCubemap(textures));
    }

}
