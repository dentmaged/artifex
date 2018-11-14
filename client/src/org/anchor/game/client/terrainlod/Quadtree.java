package org.anchor.game.client.terrainlod;

import java.util.ArrayList;
import java.util.List;

import org.anchor.client.engine.renderer.Graphics;
import org.anchor.client.engine.renderer.Loader;
import org.anchor.client.engine.renderer.types.mesh.Mesh;
import org.anchor.engine.shared.terrain.Terrain;
import org.anchor.game.client.GameClient;
import org.anchor.game.client.shaders.QuadtreeShader;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

public class Quadtree {

    private int lod;
    private float size;
    private Vector3f position, center, location, world;
    private Vector2f relative;
    private Quadtree parent;
    private List<Quadtree> children = new ArrayList<Quadtree>();

    private static Mesh mesh;
    private static int texture;
    private static int heightmap;

    public Quadtree(int lod, float size, Vector3f position, Vector2f relative, Vector3f world, Quadtree parent) {
        this.lod = lod;
        this.size = size;
        this.position = position;
        this.center = Vector3f.add(position, new Vector3f(size / 2, 0, size / 2), null);
        this.location = new Vector3f(world.x, world.z, Terrain.DEFAULT_SIZE);
        this.world = world;
        this.relative = relative;
        this.parent = parent;
    }

    public void addChildren() {
        if (lod == 0 || children.size() > 0)
            return;

        float s = size / 2;
        float rs = size / (Terrain.DEFAULT_SIZE * 2);

        for (int x = 0; x < 2; x++)
            for (int y = 0; y < 2; y++)
                children.add(new Quadtree(lod - 1, s, new Vector3f(position.x + s * x, position.y, position.z + s * y), new Vector2f(relative.x + rs * x, relative.y + rs * y), world, this));
    }

    public void update() {
        float distance = Vector3f.sub(GameClient.getPlayer().getPosition(), center, null).length();

        if (distance / lod > size || (lod == 1) && distance < 32)
            children.clear();
        else
            addChildren();

        for (Quadtree child : children)
            child.update();
    }

    public void render() {
        if (children.size() == 0) {
            if (mesh == null) {
                texture = Loader.getInstance().loadTexture("res/terrain/grass");
                heightmap = Loader.getInstance().loadTexture("res/heightmap/heightmap");

                int vertexPointer = 0;
                int v = 32;
                int count = v * v;

                float[] vertices = new float[count * 3];
                float[] normals = new float[count * 3];
                float[] textureCoords = new float[count * 2];

                float vf = 32;
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

            Graphics.bind2DTexture(0, 0);
            Graphics.bind2DTexture(texture, 1);
            Graphics.bind2DTexture(0, 2);
            Graphics.bind2DTexture(0, 3);
            Graphics.bind2DTexture(0, 4);
            Graphics.bind2DTexture(heightmap, 5);

            GL11.glEnable(GL11.GL_CULL_FACE);
            GL11.glCullFace(GL11.GL_BACK);
            shader.loadTerrainInformation(this);

            GL11.glDrawElements(GL11.GL_TRIANGLES, mesh.getVertexCount(), GL11.GL_UNSIGNED_INT, 0);

            GL20.glDisableVertexAttribArray(0);
            GL20.glDisableVertexAttribArray(1);
            GL20.glDisableVertexAttribArray(2);
            GL30.glBindVertexArray(0);
            shader.stop();
        } else {
            for (Quadtree child : children)
                child.render();
        }
    }

    public Vector3f getPosition() {
        return position;
    }

    public Vector3f getLocation() {
        return location;
    }

    public Quadtree getParent() {
        return parent;
    }

    public float getSize() {
        return size;
    }

    public static Quadtree create(Vector3f position) {
        return new Quadtree(getMaxLOD((int) Terrain.DEFAULT_SIZE), Terrain.DEFAULT_SIZE, position, new Vector2f(), position, null);
    }

    private static int getMaxLOD(int size) {
        int count = 1;
        while (size >= 4) {
            size /= 4;
            count++;
        }

        return count;
    }

}
