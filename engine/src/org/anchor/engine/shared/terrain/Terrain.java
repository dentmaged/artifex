package org.anchor.engine.shared.terrain;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.anchor.engine.common.Log;
import org.anchor.engine.common.TextureType;
import org.anchor.engine.common.utils.CoreMaths;
import org.anchor.engine.common.utils.FileHelper;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

public class Terrain {

    public static final float DEFAULT_SIZE = 1024;
    public static final float MAX_HEIGHT = 16;
    public static final float MAX_PIXEL_COLOUR = 256 * 256 * 256;

    protected float x, z, size;
    protected int gridX, gridZ;
    protected Vector3f center;

    protected String heightmap;
    protected float[][] heights;
    protected boolean refresh;

    public Terrain(int gridX, int gridZ, String heightmap) {
        this(DEFAULT_SIZE, gridX, gridZ, heightmap);
    }

    public Terrain(float size, int gridX, int gridZ, String heightmap) {
        this.size = size;
        setGridX(gridX);
        setGridZ(gridZ);

        if (heightmap == null || heightmap == "") {
            Log.warning("Heightmap is null or blank! Using template heightmap!");
            heightmap = "heightmap";
        }

        loadHeightsFromHeightmap(heightmap);
    }

    public void setGridX(int gridX) {
        this.gridX = gridX;
        this.x = gridX * size;

        this.center = new Vector3f(x + (size / 2), 0, z + (size / 2));
    }

    public void setGridZ(int gridZ) {
        this.gridZ = gridZ;
        this.z = gridZ * size;

        this.center = new Vector3f(x + (size / 2), 0, z + (size / 2));
    }

    public void setSize(float size) {
        this.size = size;
        this.x = gridX * size;
        this.z = gridZ * size;

        this.center = new Vector3f(x + (size / 2), 0, z + (size / 2));
    }

    public float getX() {
        return x;
    }

    public float getZ() {
        return z;
    }

    public float getSize() {
        return size;
    }

    public float getGridX() {
        return gridX;
    }

    public float getGridZ() {
        return gridZ;
    }

    public int getGX() {
        return gridX;
    }

    public int getGZ() {
        return gridZ;
    }

    public Vector3f getCenter() {
        return center;
    }

    public String getHeightmap() {
        return heightmap;
    }

    public void update() {

    }

    // COLLISIONS \\

    public float getHeightOfTerrain(float worldX, float worldZ) {
        float terrainX = worldX - this.x;
        float terrainZ = worldZ - this.z;
        float gridSquareSize = size / ((float) heights.length - 1);
        int gridX = (int) Math.floor(terrainX / gridSquareSize);
        int gridZ = (int) Math.floor(terrainZ / gridSquareSize);

        if (gridX >= heights.length - 1 || gridZ >= heights.length - 1 || gridX < 0 || gridZ < 0)
            return -MAX_HEIGHT;

        float xCoord = (terrainX % gridSquareSize) / gridSquareSize;
        float zCoord = (terrainZ % gridSquareSize) / gridSquareSize;

        if (xCoord <= (1 - zCoord))
            return CoreMaths.barryCentric(new Vector3f(0, heights[gridX][gridZ], 0), new Vector3f(1, heights[gridX + 1][gridZ], 0), new Vector3f(0, heights[gridX][gridZ + 1], 1), new Vector2f(xCoord, zCoord));
        else
            return CoreMaths.barryCentric(new Vector3f(1, heights[gridX + 1][gridZ], 0), new Vector3f(1, heights[gridX + 1][gridZ + 1], 1), new Vector3f(0, heights[gridX][gridZ + 1], 1), new Vector2f(xCoord, zCoord));
    }

    // LOADING \\

    public void loadHeightsFromHeightmap(String heightmap) {
        this.heightmap = heightmap;

        BufferedImage image = null;
        try {
            image = ImageIO.read(FileHelper.newGameFile("res", TextureType.HEIGHTMAP, heightmap + ".png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        int VERTEX_COUNT = image.getHeight();
        heights = new float[VERTEX_COUNT][VERTEX_COUNT];

        for (int i = 0; i < VERTEX_COUNT; i++)
            for (int j = 0; j < VERTEX_COUNT; j++)
                heights[j][i] = getHeightHeightmap(j, i, image);

        refresh = true;
    }

    public void unload() {
        heights = null;
    }

    protected float getHeightHeightmap(int x, int z, BufferedImage image) {
        if (x < 0 || x >= image.getHeight() || z < 0 || z >= image.getHeight()) {
            return 0;
        }

        float height = image.getRGB(x, z);
        height += MAX_PIXEL_COLOUR / 2f;
        height /= MAX_PIXEL_COLOUR / 2f;
        height *= MAX_HEIGHT;

        return height;
    }

    public float getIncrement() {
        return (float) heights.length / (float) size;
    }

}
