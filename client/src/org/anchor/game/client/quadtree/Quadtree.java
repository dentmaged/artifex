package org.anchor.game.client.quadtree;

import java.util.List;

import org.anchor.engine.shared.terrain.Terrain;
import org.anchor.game.client.GameClient;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

public class Quadtree {

    private float size;
    private Vector3f position;
    private Vector2f relative;
    private Quadtree parent;
    private List<Quadtree> children;

    public Quadtree(float size, Vector3f position, Vector2f relative, Quadtree parent) {
        this.size = size;
        this.position = position;
        this.relative = relative;
        this.parent = parent;
    }

    public void addChildren() {
        if (size == 16 || children.size() > 0)
            return;

        float s = size / 2;
        float rs = size / (Terrain.SIZE * 2);

        for (int x = 0; x < 2; x++)
            for (int y = 0; y < 2; y++)
                children.add(new Quadtree(s, new Vector3f(position.x + s * x, position.y, position.z + s * y), new Vector2f(relative.x + rs * x, relative.y + rs * y), this));
    }

    public void update() {
        float distance = Vector3f.sub(GameClient.getPlayer().getPosition(), position, null).lengthSquared();

        if (distance > size * size)
            children.clear();
        else
            addChildren();

        for (Quadtree child : children)
            child.update();
    }

    public void render() {
        if (children.size() == 0) {

        } else {
            for (Quadtree child : children)
                child.render();
        }
    }

}
