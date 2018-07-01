package org.anchor.game.client.shaders;

import org.anchor.engine.common.utils.CoreMaths;
import org.anchor.engine.shared.components.LivingComponent;
import org.anchor.game.client.GameClient;
import org.anchor.game.client.terrainlod.Quadtree;
import org.anchor.game.client.terrainlod.Tile;
import org.lwjgl.util.vector.Vector3f;

public class QuadtreeShader extends ModelShader {

    private static QuadtreeShader instance = new QuadtreeShader();

    protected QuadtreeShader() {
        super("quadtree");
        texture = "blendmap";
    }

    @Override
    public void onBind() {
        super.onBind();

        loadInt("backgroundTexture", 1);
        loadInt("rTexture", 2);
        loadInt("gTexture", 3);
        loadInt("bTexture", 4);
        loadInt("heightmap", 5);
    }

    public void loadTerrainInformation(Quadtree quadtree) {
        loadFloat("size", quadtree.getSize());
        loadVector("location", quadtree.getLocation());
        loadMatrix("transformationMatrix", CoreMaths.createTransformationMatrix(quadtree.getPosition(), new Vector3f(), new Vector3f(quadtree.getSize(), 1, quadtree.getSize())));
        loadMatrix("normalMatrix", GameClient.getPlayer().getComponent(LivingComponent.class).getNormalMatrix(quadtree.getPosition(), new Vector3f()));
    }

    public void loadTerrainInformation(Tile tile) {
        loadFloat("size", tile.getSize());
        loadFloat("morph", tile.getMorph());
        loadVector("location", tile.getLocation());
        loadMatrix("transformationMatrix", CoreMaths.createTransformationMatrix(tile.getPosition(), new Vector3f(), new Vector3f(tile.getSize(), 1, tile.getSize())));
        loadMatrix("normalMatrix", GameClient.getPlayer().getComponent(LivingComponent.class).getNormalMatrix(tile.getPosition(), new Vector3f()));
    }

    public static QuadtreeShader getInstance() {
        return instance;
    }

}
