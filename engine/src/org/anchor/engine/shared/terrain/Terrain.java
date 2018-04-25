package org.anchor.engine.shared.terrain;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.anchor.engine.common.TextureType;
import org.anchor.engine.common.utils.FileHelper;
import org.anchor.engine.shared.utils.Maths;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

public class Terrain {

    public static final int SIZE = 100;
    public static final float MAX_HEIGHT = 16;
    public static final float MAX_PIXEL_COLOUR = 256 * 256 * 256;
    public static final float[] LODs = {
            SIZE * SIZE, SIZE * SIZE * 4, SIZE * SIZE * 16, SIZE * SIZE * 64,
    };

    protected float x, z;
    protected int gridX, gridZ, LOD;
    protected Vector3f center;

    protected String heightmap;
    protected float[][] heights;
    protected boolean refresh;

    public Terrain(int gridX, int gridZ, String heightmap) {
        setGridX(gridX);
        setGridZ(gridZ);

        if (heightmap == null || heightmap == "") {
            System.err.println("Heightmap is null or blank! Using template heightmap!");
            heightmap = "heightmap";
        }

        loadHeightsFromHeightmap(heightmap);
    }

    public void setGridX(int gridX) {
        this.gridX = gridX;
        this.x = gridX * SIZE;

        this.center = new Vector3f(x + (SIZE / 2), 0, z + (SIZE / 2));
    }

    public void setGridZ(int gridZ) {
        this.gridZ = gridZ;
        this.z = gridZ * SIZE;

        this.center = new Vector3f(x + (SIZE / 2), 0, z + (SIZE / 2));
    }

    public float getX() {
        return x;
    }

    public float getZ() {
        return z;
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

    public int getLOD() {
        return LOD;
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
        float gridSquareSize = SIZE / ((float) heights.length - 1);
        int gridX = (int) Math.floor(terrainX / gridSquareSize);
        int gridZ = (int) Math.floor(terrainZ / gridSquareSize);

        if (gridX >= heights.length - 1 || gridZ >= heights.length - 1 || gridX < 0 || gridZ < 0)
            return -MAX_HEIGHT;

        float xCoord = (terrainX % gridSquareSize) / gridSquareSize;
        float zCoord = (terrainZ % gridSquareSize) / gridSquareSize;

        if (xCoord <= (1 - zCoord))
            return Maths.barryCentric(new Vector3f(0, heights[gridX][gridZ], 0), new Vector3f(1, heights[gridX + 1][gridZ], 0), new Vector3f(0, heights[gridX][gridZ + 1], 1), new Vector2f(xCoord, zCoord));
        else
            return Maths.barryCentric(new Vector3f(1, heights[gridX + 1][gridZ], 0), new Vector3f(1, heights[gridX + 1][gridZ + 1], 1), new Vector3f(0, heights[gridX][gridZ + 1], 1), new Vector2f(xCoord, zCoord));
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

}
