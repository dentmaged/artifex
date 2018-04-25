package org.anchor.game.client.types;

import java.util.ArrayList;
import java.util.List;

import org.anchor.client.engine.renderer.Loader;
import org.anchor.client.engine.renderer.types.Mesh;
import org.anchor.engine.shared.terrain.Terrain;
import org.anchor.game.client.GameClient;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

public class ClientTerrain extends Terrain {

    protected Mesh mesh;
    protected TerrainTexture textures;
    protected Vector4f colour = new Vector4f();

    protected List<Integer> offsets = new ArrayList<Integer>();
    protected float[] vertices, normals, textureCoords;
    protected int[] indices;

    public ClientTerrain(int gridX, int gridZ, String heightmap, TerrainTexture textures) {
        super(gridX, gridZ, heightmap);

        this.textures = textures;
    }

    @Override
    public void update() {
        float distance = Vector3f.sub(GameClient.getPlayer().getPosition(), center, null).lengthSquared();
        if (distance < LODs[0])
            LOD = 0;
        else if (distance < LODs[1])
            LOD = 1;
        else if (distance < LODs[2])
            LOD = 2;
        else
            LOD = 3;
    }

    public int getOffset() {
        return offsets.get(LOD);
    }

    public int getEnd() {
        return offsets.get(LOD + 1) - getOffset();
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

        vertices = new float[count * 3];
        normals = new float[count * 3];
        textureCoords = new float[count * 2];

        float vf = heights.length;
        for (int i = 0; i < v; i++) {
            for (int j = 0; j < v; j++) {
                vertices[vertexPointer * 3] = (float) j / (vf - 1) * SIZE;
                vertices[vertexPointer * 3 + 1] = heights[j][i];
                vertices[vertexPointer * 3 + 2] = (float) i / (vf - 1) * SIZE;

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

        System.out.println(generatedIndices.size() + " " + count);

        generatedIndices.addAll(generateIndices(v, 2));
        offsets.add(generatedIndices.size());

        System.out.println(generatedIndices.size());

        generatedIndices.addAll(generateIndices(v, 4));
        offsets.add(generatedIndices.size());

        System.out.println(generatedIndices.size());

        indices = new int[generatedIndices.size()];
        for (int i = 0; i < indices.length; i++)
            indices[i] = generatedIndices.get(i);

        mesh = Loader.getInstance().loadToVAO(vertices, textureCoords, normals, indices);
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
