package org.anchor.client.engine.renderer.font;

import org.lwjgl.util.vector.Vector4f;

public class Character {

    private int id;
    private Vector4f uv;

    private float xOffset;
    private float yOffset;

    private float sizeX;
    private float sizeY;

    private float xAdvance;

    protected Character(int id, float xTextureCoord, float yTextureCoord, float xTexSize, float yTexSize, float xOffset, float yOffset, float sizeX, float sizeY, float xAdvance) {
        this.id = id;

        this.uv = new Vector4f(xTextureCoord, yTextureCoord, xTexSize, yTexSize);
        this.xAdvance = xAdvance;

        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
    }

    protected int getId() {
        return id;
    }

    protected Vector4f getUV() {
        return uv;
    }

    protected float getXOffset() {
        return xOffset;
    }

    protected float getYOffset() {
        return yOffset;
    }

    protected float getSizeX() {
        return sizeX;
    }

    protected float getSizeY() {
        return sizeY;
    }

    protected float getXAdvance() {
        return xAdvance;
    }

}
