package org.anchor.engine.shared.utils;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.anchor.engine.common.TextureType;
import org.anchor.engine.common.utils.FileHelper;
import org.anchor.engine.common.vfs.VirtualFileSystem;
import org.anchor.engine.shared.terrain.Terrain;

public class TerrainUtils {

    public static String PARTS = ((char) 2) + "";

    public static float[][] loadHeightsFromHeightmap(String heightmap) {
        BufferedImage image = null;
        try {
            image = ImageIO.read(VirtualFileSystem.find(FileHelper.newGameFile("res", TextureType.HEIGHTMAP, heightmap + ".png")).openInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        int VERTEX_COUNT = image.getHeight();
        float[][] heights = new float[VERTEX_COUNT][VERTEX_COUNT];

        for (int i = 0; i < VERTEX_COUNT; i++)
            for (int j = 0; j < VERTEX_COUNT; j++)
                heights[j][i] = getHeightFromRGB(image.getRGB(j, i));

        return heights;
    }

    public static float[][] loadHeightsFromData(String path) {
        String contents = FileHelper.read(VirtualFileSystem.find(path));
        String[] parts = contents.split(PARTS);

        int VERTEX_COUNT = Integer.parseInt(parts[0]);
        float[][] heights = new float[VERTEX_COUNT][VERTEX_COUNT];

        for (int i = 0; i < VERTEX_COUNT; i++)
            for (int j = 0; j < VERTEX_COUNT; j++)
                heights[j][i] = Float.parseFloat(parts[j * VERTEX_COUNT + i + 1]);

        return heights;
    }

    public static float getHeightFromRGB(int rgb) {
        float height = rgb;

        height += Terrain.MAX_PIXEL_COLOUR / 2f;
        height /= Terrain.MAX_PIXEL_COLOUR / 2f;
        height *= Terrain.MAX_HEIGHT;

        return height;
    }

}
