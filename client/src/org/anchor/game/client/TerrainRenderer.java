package org.anchor.game.client;

import org.anchor.client.engine.renderer.Graphics;
import org.anchor.client.engine.renderer.types.mesh.Mesh;
import org.anchor.engine.shared.scene.Scene;
import org.anchor.engine.shared.terrain.Terrain;
import org.anchor.game.client.shaders.TerrainShader;
import org.anchor.game.client.types.ClientTerrain;
import org.anchor.game.client.utils.FrustumCull;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

public class TerrainRenderer {

    private static TerrainShader shader = TerrainShader.getInstance();

    public static void render(Scene scene) {
        shader.start();

        for (Terrain shared : scene.getTerrains()) {
            if (!FrustumCull.isVisible(shared))
                continue;

            ClientTerrain terrain = (ClientTerrain) shared;
            if (!terrain.isLoaded())
                continue;

            Mesh mesh = terrain.getMesh();
            GL30.glBindVertexArray(mesh.getVAO());

            GL20.glEnableVertexAttribArray(0);
            GL20.glEnableVertexAttribArray(1);
            GL20.glEnableVertexAttribArray(2);

            Graphics.bind2DTexture(terrain.getTextures().getBlendmap(), 0);
            Graphics.bind2DTexture(terrain.getTextures().getBackgroundTexture(), 1);
            Graphics.bind2DTexture(terrain.getTextures().getRedTexture(), 2);
            Graphics.bind2DTexture(terrain.getTextures().getGreenTexture(), 3);
            Graphics.bind2DTexture(terrain.getTextures().getBlueTexture(), 4);

            GL11.glEnable(GL11.GL_CULL_FACE);
            GL11.glCullFace(GL11.GL_BACK);
            shader.loadTerrainInformation(terrain);

            GL11.glDrawElements(GL11.GL_TRIANGLES, terrain.getMesh().getVertexCount(), GL11.GL_UNSIGNED_INT, 0);

            GL20.glDisableVertexAttribArray(0);
            GL20.glDisableVertexAttribArray(1);
            GL20.glDisableVertexAttribArray(2);
            GL30.glBindVertexArray(0);
        }

        shader.stop();
    }

}
