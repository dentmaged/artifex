package org.anchor.game.client.particles;

import org.anchor.client.engine.renderer.types.texture.TextureRequest;

public class ParticleTexture {

    private TextureRequest request;
    private int rows;
    private boolean additive;

    public ParticleTexture(TextureRequest request) {
        this(request, 1, false);
    }

    public ParticleTexture(TextureRequest request, int rows) {
        this(request, rows, false);
    }

    public ParticleTexture(TextureRequest request, int rows, boolean additive) {
        this.request = request;
        this.rows = rows;
        this.additive = additive;
    }

    public TextureRequest getTexture() {
        return request;
    }

    public int getRows() {
        return rows;
    }

    public boolean usesAdditiveBlending() {
        return additive;
    }

}
