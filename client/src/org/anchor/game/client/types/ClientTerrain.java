package org.anchor.game.client.types;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import org.anchor.client.engine.renderer.Loader;
import org.anchor.client.engine.renderer.types.mesh.Mesh;
import org.anchor.engine.shared.terrain.Terrain;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

public class ClientTerrain extends Terrain {

    protected Mesh mesh;
    protected TerrainTexture textures;
    protected Vector4f colour = new Vector4f();

    protected List<Integer> offsets = new ArrayList<Integer>();

    public ClientTerrain(int gridX, int gridZ, String heightmap, TerrainTexture textures) {
        this(Terrain.DEFAULT_SIZE, gridX, gridZ, heightmap, textures);
    }

    public ClientTerrain(float size, int gridX, int gridZ, String heightmap, TerrainTexture textures) {
        super(gridX, gridZ, heightmap);

        this.textures = textures;
    }

    public float getHeightAtPoint(Vector3f point) {
        return getHeightAtPoint(point.x, point.z);
    }

    public float getHeightAtPoint(float x, float z) {
        float localX = x - this.x;
        float localZ = z - this.z;

        int arrayX = (int) (localX * getIncrement());
        int arrayZ = (int) (localZ * getIncrement());

        if (arrayX < 0 || arrayX >= heights.length || arrayZ < 0 || arrayZ >= heights.length)
            return 0;

        return heights[arrayX][arrayZ];
    }

    public void setHeightAtPoint(Vector3f point, float height) {
        setHeightAtPoint(point.x, point.z, height);
    }

    public void setHeightAtPoint(float x, float z, float height) {
        float localX = x - this.x;
        float localZ = z - this.z;

        int arrayX = (int) (localX * getIncrement());
        int arrayZ = (int) (localZ * getIncrement());

        if (arrayX < 0 || arrayX >= heights.length || arrayZ < 0 || arrayZ >= heights.length)
            return;

        heights[arrayX][arrayZ] = height;
    }

    public void increaseHeightAtPoint(Vector3f point, float amount) {
        increaseHeightAtPoint(point.x, point.z, amount);
    }

    public void increaseHeightAtPoint(float x, float z, float amount) {
        float localX = x - this.x;
        float localZ = z - this.z;

        int arrayX = (int) (localX * getIncrement());
        int arrayZ = (int) (localZ * getIncrement());

        if (arrayX < 0 || arrayX >= heights.length || arrayZ < 0 || arrayZ >= heights.length)
            return;

        heights[arrayX][arrayZ] += amount;
    }

    public Mesh getMesh() {
        if (refresh)
            loadHeights();

        return mesh;
    }

    public TerrainTexture getTextures() {
        return textures;
    }

    public Vector4f getColour() {
        return colour;
    }

    public void reloadHeights() {
        if (mesh == null) {
            loadHeights();

            return;
        }

        int vertexPointer = 0;
        int v = heights.length;
        int count = v * v;

        float[] vertices = new float[count * 3];
        float[] normals = new float[count * 3];

        float vf = heights.length;
        for (int i = 0; i < v; i++) {
            for (int j = 0; j < v; j++) {
                vertices[vertexPointer * 3] = (float) j / (vf - 1);
                vertices[vertexPointer * 3 + 1] = heights[j][i];
                vertices[vertexPointer * 3 + 2] = (float) i / (vf - 1);

                Vector3f normal = calculateNormal(j, i);
                normals[vertexPointer * 3] = normal.x;
                normals[vertexPointer * 3 + 1] = normal.y;
                normals[vertexPointer * 3 + 2] = normal.z;

                vertexPointer++;
            }
        }

        FloatBuffer buffer = BufferUtils.createFloatBuffer(vertices.length);
        Loader.getInstance().updateVBO(mesh.getFirstVBO(), vertices, buffer);

        buffer = BufferUtils.createFloatBuffer(normals.length);
        Loader.getInstance().updateVBO(mesh.getFirstVBO() + 2, normals, buffer);
    }

    public void loadHeights() {
        refresh = false;
        if (mesh != null) {
            GL30.glDeleteVertexArrays(mesh.getVAO());
            GL15.glDeleteBuffers(mesh.getFirstVBO());
            GL15.glDeleteBuffers(mesh.getFirstVBO() + 1);
            GL15.glDeleteBuffers(mesh.getFirstVBO() + 2);

            mesh = null;
        }

        int vertexPointer = 0;
        int v = heights.length;
        int count = v * v;

        float[] vertices = new float[count * 3];
        float[] normals = new float[count * 3];
        float[] textureCoords = new float[count * 2];

        float vf = heights.length;
        for (int i = 0; i < v; i++) {
            for (int j = 0; j < v; j++) {
                vertices[vertexPointer * 3] = (float) j / (vf - 1);
                vertices[vertexPointer * 3 + 1] = heights[j][i];
                vertices[vertexPointer * 3 + 2] = (float) i / (vf - 1);

                Vector3f normal = calculateNormal(j, i);
                normals[vertexPointer * 3] = normal.x;
                normals[vertexPointer * 3 + 1] = normal.y;
                normals[vertexPointer * 3 + 2] = normal.z;

                textureCoords[vertexPointer * 2] = (float) j / (vf - 1);
                textureCoords[vertexPointer * 2 + 1] = (float) i / (vf - 1);

                vertexPointer++;
            }
        }

        List<Integer> generatedIndices = new ArrayList<Integer>();
        offsets.add(generatedIndices.size());

        generatedIndices.addAll(generateIndices(v, 1));
        offsets.add(generatedIndices.size());

        int[] indices = new int[generatedIndices.size()];
        for (int i = 0; i < indices.length; i++)
            indices[i] = generatedIndices.get(i);

        mesh = Loader.getInstance().loadToVAO(vertices, textureCoords, normals, indices);
    }

    @Override
    public void unload() {
        super.unload();

        GL30.glDeleteVertexArrays(mesh.getVAO());
        GL15.glDeleteBuffers(mesh.getFirstVBO());
        GL15.glDeleteBuffers(mesh.getFirstVBO() + 1);
        GL15.glDeleteBuffers(mesh.getFirstVBO() + 2);

        mesh = null;
    }

    private Vector3f calculateNormal(int x, int z) {
        float heightL = getHeight(x - 1, z);
        float heightR = getHeight(x + 1, z);
        float heightD = getHeight(x, z - 1);
        float heightU = getHeight(x, z + 1);
        Vector3f normal = new Vector3f(heightL - heightR, 2f, heightD - heightU);
        normal.normalise();

        return normal;
    }

    private float getHeight(int x, int z) {
        if (x < 0 || x >= heights.length || z < 0 || z >= heights.length)
            return 0;

        return heights[x][z];
    }

    protected List<Integer> generateIndices(int vertices, int step) {
        List<Integer> indices = new ArrayList<Integer>();

        for (int gz = 0; gz < vertices; gz += step) {
            for (int gx = 0; gx < vertices; gx += step) {
                int topLeft = (gz * vertices) + gx;
                int topRight = topLeft + step;
                int bottomLeft = ((gz + step) * vertices) + gx;
                int bottomRight = bottomLeft + step;

                if (topRight % vertices == 0 || bottomLeft > vertices * vertices)
                    continue;

                indices.add(topLeft);
                indices.add(bottomLeft);
                indices.add(topRight);
                indices.add(topRight);
                indices.add(bottomLeft);
                indices.add(bottomRight);
            }
        }

        for (int i = 0; i < 6; i++)
            indices.remove(indices.size() - 1);

        return indices;
    }

    public boolean isLoaded() {
        return textures.isLoaded();
    }

}
