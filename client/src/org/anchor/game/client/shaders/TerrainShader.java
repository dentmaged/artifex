package org.anchor.game.client.shaders;

import org.anchor.client.engine.renderer.Renderer;
import org.anchor.engine.common.utils.CoreMaths;
import org.anchor.engine.shared.components.LivingComponent;
import org.anchor.game.client.GameClient;
import org.anchor.game.client.types.ClientTerrain;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

public class TerrainShader extends ModelShader {

    private static TerrainShader instance = new TerrainShader();

    protected TerrainShader() {
        super("terrain");
        texture = "blendmap";
    }

    @Override
    public void onBind() {
        super.onBind();

        loadInt("backgroundTexture", 1);
        loadInt("rTexture", 2);
        loadInt("gTexture", 3);
        loadInt("bTexture", 4);
    }

    public void loadTerrainInformation(ClientTerrain terrain) {
        loadMatrix("projectionViewTransformationMatrix", Matrix4f.mul(Matrix4f.mul(Renderer.getProjectionMatrix(), GameClient.getPlayer().getComponent(LivingComponent.class).getViewMatrix(), null), CoreMaths.createTransformationMatrix(new Vector3f(terrain.getX(), 0, terrain.getZ()), new Vector3f(), new Vector3f(terrain.getSize(), 1, terrain.getSize())), null));
        loadMatrix("normalMatrix", GameClient.getPlayer().getComponent(LivingComponent.class).getNormalMatrix(new Vector3f(terrain.getX(), 0, terrain.getZ()), new Vector3f()));

        loadVector("colour", terrain.getColour());
    }

    public static TerrainShader getInstance() {
        return instance;
    }

}
