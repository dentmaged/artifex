package org.anchor.client.engine.renderer.types;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

public enum ImageFormat {

    RGB(GL11.GL_RGB, GL11.GL_RGB), RGB16F(GL30.GL_RGB16F, GL11.GL_RGB), RGBA(GL11.GL_RGBA, GL11.GL_RGBA), RGBA16F(GL30.GL_RGBA16F, GL11.GL_RGBA), R(GL11.GL_RED, GL11.GL_RED), R16F(GL30.GL_R16F, GL11.GL_RED), R32F(GL30.GL_R32F, GL11.GL_RED), RG(GL30.GL_RG, GL30.GL_RG), RG16F(GL30.GL_RG16F, GL30.GL_RG), RG32F(GL30.GL_RG32F, GL30.GL_RG);

    protected int format, base;

    private ImageFormat(int format, int base) {
        this.format = format;
        this.base = base;
    }

    public int getType() { // like RGBA16F
        return format;
    }

    public int getBase() { // like RGBA
        return base;
    }

}
