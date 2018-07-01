package org.anchor.game.editor.terrain.shape;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.anchor.engine.common.utils.FileHelper;
import org.anchor.engine.common.utils.StringUtils;

public class Shape {

    private String name;
    private BufferedImage image;

    public Shape(String path) {
        this.name = StringUtils.upperFirst(path).replace("_smooth", " (Smooth)");

        try {
            this.image = ImageIO.read(FileHelper.newGameFile("brushes", path + ".png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public float getStrengthAtPixel(float currentX, float currentZ, float radius) {
        float max = radius * 2;
        int x = (int) ((currentX + radius) / max * image.getWidth());
        int z = (int) ((currentZ + radius) / max * image.getHeight());

        return ((image.getRGB(x, z) >> 16) & 0x000000FF) / 255f;
    }

    public String getName() {
        return name;
    }

}
