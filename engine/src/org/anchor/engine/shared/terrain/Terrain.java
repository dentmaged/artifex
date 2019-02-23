package org.anchor.engine.shared.terrain;

import org.anchor.engine.common.utils.CoreMaths;
import org.anchor.engine.shared.utils.TerrainUtils;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

public class Terrain {

    public static final float DEFAULT_SIZE = 1024;
    public static final float MAX_HEIGHT = 16;
    public static final float MAX_PIXEL_COLOUR = 256 * 256 * 256;

    protected float x, z, size;
    protected int gridX, gridZ;
    protected Vector3f center;

    protected float[][] heights;
    protected boolean refresh;

    public Terrain(int gridX, int gridZ) {
        this(DEFAULT_SIZE, gridX, gridZ);
    }

    public Terrain(int gridX, int gridZ, float[][] heights) {
        this(DEFAULT_SIZE, gridX, gridZ, heights);
    }

    public Terrain(float size, int gridX, int gridZ) {
        this(size, gridX, gridZ, TerrainUtils.loadHeightsFromData("terraindata/" + gridX + "/" + gridZ + "/height"));
    }

    public Terrain(float size, int gridX, int gridZ, float[][] heights) {
        this.size = size;

        setGridX(gridX);
        setGridZ(gridZ);
        setHeights(heights);
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

    public int getVerticesPerSide() {
        return heights.length;
    }

    public float[][] getHeights() {
        return heights;
    }

    public void setHeights(float[][] heights) {
        this.heights = heights;
        this.refresh = true;
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

    public float getHeightAt(int x, int z) {
        return heights[x][z];
    }

    // LOADING \\

    public void unload() {
        heights = null;
    }

    public float getIncrement() {
        return (float) heights.length / (float) size;
    }

}
