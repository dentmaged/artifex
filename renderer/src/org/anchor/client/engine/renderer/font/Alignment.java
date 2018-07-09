package org.anchor.client.engine.renderer.font;

public enum Alignment {

    LEFT(0), CENTER(-0.5f), RIGHT(-1);

    protected float multiplier;

    private Alignment(float multiplier) {
        this.multiplier = multiplier;
    }

    public float getAlignment(float length) {
        return length * multiplier;
    }

}
