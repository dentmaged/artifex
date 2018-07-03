package org.anchor.game.client.async.types;

import java.awt.image.BufferedImage;

import org.anchor.client.engine.renderer.types.texture.Texture;
import org.anchor.client.engine.renderer.types.texture.TextureRequest;

public class CompletedTexture extends CompletedRequest {

    private TextureRequest request;
    private BufferedImage data;

    public CompletedTexture(TextureRequest request, BufferedImage data) {
        this.request = request;
        this.data = data;
    }

    @Override
    public void load() {
        request.setTexture(new Texture(data).getTextureId());
    }

}
