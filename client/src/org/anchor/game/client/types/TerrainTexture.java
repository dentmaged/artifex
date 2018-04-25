package org.anchor.game.client.types;

import org.anchor.client.engine.renderer.types.TextureRequest;
import org.anchor.engine.common.TextureType;
import org.anchor.game.client.async.Requester;

public class TerrainTexture {

    protected TextureRequest blendmap, background, red, green, blue;

    public TerrainTexture(String blendmap, String background, String red, String green, String blue) {
        this(Requester.requestTexture(TextureType.TERRAIN, blendmap), Requester.requestTexture(TextureType.TERRAIN, background), Requester.requestTexture(TextureType.TERRAIN, red), Requester.requestTexture(TextureType.TERRAIN, green), Requester.requestTexture(TextureType.TERRAIN, blue));
    }

    public TerrainTexture(TextureRequest blendmap, TextureRequest background, TextureRequest red, TextureRequest green, TextureRequest blue) {
        this.blendmap = blendmap;
        this.background = background;
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    public int getBlendmap() {
        return blendmap.getTexture();
    }

    public String getBlendmapName() {
        return TextureType.TERRAIN.extract(blendmap.getName());
    }

    public int getBackgroundTexture() {
        return background.getTexture();
    }

    public String getBackgroundName() {
        return TextureType.TERRAIN.extract(background.getName());
    }

    public int getRedTexture() {
        return red.getTexture();
    }

    public String getRedName() {
        return TextureType.TERRAIN.extract(red.getName());
    }

    public int getGreenTexture() {
        return green.getTexture();
    }

    public String getGreenName() {
        return TextureType.TERRAIN.extract(green.getName());
    }

    public int getBlueTexture() {
        return blue.getTexture();
    }

    public String getBlueName() {
        return TextureType.TERRAIN.extract(blue.getName());
    }

    public boolean isLoaded() {
        return blendmap.isLoaded() && background.isLoaded() && red.isLoaded() && green.isLoaded() && blue.isLoaded();
    }

}
