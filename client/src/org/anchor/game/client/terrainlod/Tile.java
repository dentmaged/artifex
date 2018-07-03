package org.anchor.game.client.terrainlod;

import java.util.ArrayList;
import java.util.List;

import org.anchor.client.engine.renderer.Engine;
import org.anchor.client.engine.renderer.Loader;
import org.anchor.client.engine.renderer.types.mesh.Mesh;
import org.anchor.engine.common.utils.Mathf;
import org.anchor.engine.shared.terrain.Terrain;
import org.anchor.game.client.GameClient;
import org.anchor.game.client.shaders.QuadtreeShader;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

public class Tile {

    private float size;
    private Vector3f position, location;
    private Vector2f relative;
    private int morph;

    private static Mesh mesh;
    private static int texture;
    private static int heightmap;

    public static int MORPH_NONE = 0;
    public static int MORPH_TOP = 1;
    public static int MORPH_LEFT = 2;
    public static int MORPH_BOTTOM = 4;
    public static int MORPH_RIGHT = 8;

    public Tile(float size, Vector3f world, Vector2f relative, int morph) {
        this.size = size;
        this.position = new Vector3f();
        this.location = new Vector3f(world.x, world.z, Terrain.DEFAULT_SIZE);
        this.relative = relative;
        this.morph = morph;
    }

    public void render() {
        position.set(GameClient.getPlayer().getPosition().x + relative.x, 0, GameClient.getPlayer().getPosition().z + relative.y);

        if (mesh == null) {
            texture = Loader.getInstance().loadTexture("res/terrain/grass");
            heightmap = Loader.getInstance().loadTexture("res/heightmap/heightmap-massive");

            int vertexPointer = 0;
            int v = 32;
            int count = v * v;

            float[] vertices = new float[count * 3];
            float[] normals = new float[count * 3];
            float[] textureCoords = new float[count * 2];

            float vf = v;
            for (int i = 0; i < v; i++) {
                for (int j = 0; j < v; j++) {
                    vertices[vertexPointer * 3] = (float) j / (vf - 1);
                    vertices[vertexPointer * 3 + 1] = 0;
                    vertices[vertexPointer * 3 + 2] = (float) i / (vf - 1);

                    textureCoords[vertexPointer * 2] = (float) j / (vf - 1);
                    textureCoords[vertexPointer * 2 + 1] = (float) i / (vf - 1);

                    vertexPointer++;
                }
            }

            int pointer = 0;
            int[] indices = new int[6 * (v - 1) * (v - 1)];
            for (int gz = 0; gz < v - 1; gz++) {
                for (int gx = 0; gx < v - 1; gx++) {
                    int topLeft = (gz * v) + gx;
                    int topRight = topLeft + 1;
                    int bottomLeft = ((gz + 1) * v) + gx;
                    int bottomRight = bottomLeft + 1;

                    indices[pointer++] = topLeft;
                    indices[pointer++] = bottomLeft;
                    indices[pointer++] = topRight;
                    indices[pointer++] = topRight;
                    indices[pointer++] = bottomLeft;
                    indices[pointer++] = bottomRight;
                }
            }

            mesh = Loader.getInstance().loadToVAO(vertices, textureCoords, normals, indices);
        }

        QuadtreeShader shader = QuadtreeShader.getInstance();
        shader.start();
        GL30.glBindVertexArray(mesh.getVAO());

        GL20.glEnableVertexAttribArray(0);
        GL20.glEnableVertexAttribArray(1);
        GL20.glEnableVertexAttribArray(2);

        Engine.bind2DTexture(0, 0);
        Engine.bind2DTexture(texture, 1);
        Engine.bind2DTexture(0, 2);
        Engine.bind2DTexture(0, 3);
        Engine.bind2DTexture(0, 4);
        Engine.bind2DTexture(heightmap, 5);

        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glCullFace(GL11.GL_BACK);
        shader.loadTerrainInformation(this);

        GL11.glDrawElements(GL11.GL_TRIANGLES, mesh.getVertexCount(), GL11.GL_UNSIGNED_INT, 0);

        GL20.glDisableVertexAttribArray(0);
        GL20.glDisableVertexAttribArray(1);
        GL20.glDisableVertexAttribArray(2);
        GL30.glBindVertexArray(0);
        shader.stop();
    }

    public Vector3f getPosition() {
        return position;
    }

    public Vector3f getLocation() {
        return location;
    }

    public float getSize() {
        return size;
    }

    public int getMorph() {
        return morph;
    }

    public static List<Tile> create(Vector3f position) {
        List<Tile> tiles = new ArrayList<Tile>();

        float size = Terrain.DEFAULT_SIZE / Mathf.pow(2, 6);
        tiles.add(new Tile(size, position, new Vector2f(-size, -size), MORPH_NONE));
        tiles.add(new Tile(size, position, new Vector2f(-size, 0), MORPH_NONE));
        tiles.add(new Tile(size, position, new Vector2f(0, 0), MORPH_NONE));
        tiles.add(new Tile(size, position, new Vector2f(0, -size), MORPH_NONE));

        for (; size < Terrain.DEFAULT_SIZE; size *= 2) {
            tiles.add(new Tile(size, position, new Vector2f(-2 * size, -2 * size), MORPH_BOTTOM | MORPH_LEFT));
            tiles.add(new Tile(size, position, new Vector2f(-2 * size, -size), MORPH_LEFT));
            tiles.add(new Tile(size, position, new Vector2f(-2 * size, 0), MORPH_LEFT));
            tiles.add(new Tile(size, position, new Vector2f(-2 * size, size), MORPH_TOP | MORPH_LEFT));

            tiles.add(new Tile(size, position, new Vector2f(-size, -2 * size), MORPH_BOTTOM));
            tiles.add(new Tile(size, position, new Vector2f(-size, size), MORPH_TOP));

            tiles.add(new Tile(size, position, new Vector2f(0, -2 * size), MORPH_BOTTOM));
            tiles.add(new Tile(size, position, new Vector2f(0, size), MORPH_TOP));

            tiles.add(new Tile(size, position, new Vector2f(size, -2 * size), MORPH_BOTTOM | MORPH_RIGHT));
            tiles.add(new Tile(size, position, new Vector2f(size, -size), MORPH_RIGHT));
            tiles.add(new Tile(size, position, new Vector2f(size, 0), MORPH_RIGHT));
            tiles.add(new Tile(size, position, new Vector2f(size, size), MORPH_TOP | MORPH_RIGHT));
        }

        return tiles;
    }

}
