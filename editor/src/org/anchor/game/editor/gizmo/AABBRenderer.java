package org.anchor.game.editor.gizmo;

import org.anchor.client.engine.renderer.Loader;
import org.anchor.client.engine.renderer.debug.DebugShader;
import org.anchor.client.engine.renderer.types.mesh.Mesh;
import org.anchor.engine.common.utils.AABB;
import org.anchor.engine.common.utils.CoreMaths;
import org.anchor.engine.shared.components.LivingComponent;
import org.anchor.engine.shared.entity.Entity;
import org.anchor.game.client.GameClient;
import org.anchor.game.client.components.MeshComponent;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Vector3f;

public class AABBRenderer {

    private static Vector3f[] rotations = new Vector3f[] { new Vector3f(0, 270, 0), new Vector3f(0, 180, 0), new Vector3f(0, 90, 0), new Vector3f(), new Vector3f(0, 0, 180), new Vector3f(0, 270, 180), new Vector3f(0, 180, 180), new Vector3f(0, 90, 180) };

    private static float[] vertices = new float[] { 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1 };

    private Vector3f scale;

    protected Mesh mesh;
    protected DebugShader shader;

    public AABBRenderer() {
        this.scale = new Vector3f();

        this.mesh = Loader.getInstance().loadToVAO(vertices, 3);
        this.shader = DebugShader.getInstance();
    }

    public void render(Entity entity) {
        MeshComponent render = entity.getComponent(MeshComponent.class);
        if (render == null)
            return;

        AABB aabb = render.getAABB();
        if (aabb == null)
            return;

        Vector3f position = entity.getPosition();
        float distance = Vector3f.sub(GameClient.getPlayer().getPosition(), position, null).length();
        distance *= aabb.getFurthest() * 0.0005f;
        scale.set(distance, distance, distance);

        GL11.glDisable(GL11.GL_DEPTH_TEST);

        GL30.glBindVertexArray(mesh.getVAO());
        GL20.glEnableVertexAttribArray(0);

        shader.start();

        int i = 0;
        for (Vector3f corner : aabb.getCorners()) {
            shader.loadInformation(GameClient.getPlayer().getComponent(LivingComponent.class).getViewMatrix(), CoreMaths.createTransformationMatrix(corner, rotations[i], scale), new Vector3f(1, 1, 0), 1);
            GL11.glDrawArrays(GL11.GL_LINES, 0, mesh.getVertexCount());

            i++;
        }

        shader.stop();

        GL20.glDisableVertexAttribArray(0);
        GL30.glBindVertexArray(0);

        GL11.glEnable(GL11.GL_DEPTH_TEST);
    }

}
