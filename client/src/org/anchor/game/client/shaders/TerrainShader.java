package org.anchor.game.client.shaders;

import org.anchor.engine.shared.components.LivingComponent;
import org.anchor.engine.shared.utils.Maths;
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
        Matrix4f transformationMatrix = Maths.createTransformationMatrix(new Vector3f(terrain.getX(), 0, terrain.getZ()), new Vector3f(), new Vector3f(1, 1, 1));
        loadMatrix("transformationMatrix", transformationMatrix);
        loadMatrix("normalMatrix", GameClient.getPlayer().getComponent(LivingComponent.class).getNormalMatrix(transformationMatrix));

        loadVector("colour", terrain.getColour());
    }

    public static TerrainShader getInstance() {
        return instance;
    }

}
