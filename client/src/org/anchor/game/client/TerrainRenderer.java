package org.anchor.game.client;

import org.anchor.client.engine.renderer.types.Mesh;
import org.anchor.engine.shared.scene.Scene;
import org.anchor.engine.shared.terrain.Terrain;
import org.anchor.game.client.shaders.TerrainShader;
import org.anchor.game.client.types.ClientTerrain;
import org.anchor.game.client.utils.FrustumCull;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
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
            if (terrain.getLOD() == 3 || !terrain.isLoaded())
                continue;

            Mesh mesh = terrain.getMesh();
            GL30.glBindVertexArray(mesh.getVAO());

            GL20.glEnableVertexAttribArray(0);
            GL20.glEnableVertexAttribArray(1);
            GL20.glEnableVertexAttribArray(2);

            GL13.glActiveTexture(GL13.GL_TEXTURE0);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, terrain.getTextures().getBlendmap());
            GL13.glActiveTexture(GL13.GL_TEXTURE1);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, terrain.getTextures().getBackgroundTexture());
            GL13.glActiveTexture(GL13.GL_TEXTURE2);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, terrain.getTextures().getRedTexture());
            GL13.glActiveTexture(GL13.GL_TEXTURE3);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, terrain.getTextures().getGreenTexture());
            GL13.glActiveTexture(GL13.GL_TEXTURE4);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, terrain.getTextures().getBlueTexture());

            GL11.glEnable(GL11.GL_CULL_FACE);
            GL11.glCullFace(GL11.GL_BACK);
            shader.loadTerrainInformation(terrain);

            GL11.glDrawElements(GL11.GL_TRIANGLES, terrain.getEnd(), GL11.GL_UNSIGNED_INT, terrain.getOffset() * 4);

            GL20.glDisableVertexAttribArray(0);
            GL20.glDisableVertexAttribArray(1);
            GL20.glDisableVertexAttribArray(2);
            GL30.glBindVertexArray(0);
        }

        shader.stop();
    }

}
