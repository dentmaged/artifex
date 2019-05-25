package org.anchor.game.editor.editableMesh.renderer;

import java.util.List;

import org.anchor.client.engine.renderer.Loader;
import org.anchor.client.engine.renderer.Renderer;
import org.anchor.client.engine.renderer.debug.DebugShader;
import org.anchor.client.engine.renderer.types.mesh.Mesh;
import org.anchor.engine.common.utils.AABB;
import org.anchor.engine.shared.components.LivingComponent;
import org.anchor.engine.shared.editor.TransformableObject;
import org.anchor.engine.shared.profiler.Profiler;
import org.anchor.game.client.GameClient;
import org.anchor.game.editor.editableMesh.EditableMesh;
import org.anchor.game.editor.editableMesh.types.SelectionMode;
import org.anchor.game.editor.editableMesh.types.Vertex;
import org.anchor.game.editor.shaders.EditableMeshShader;
import org.anchor.game.editor.ui.LevelEditor;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Vector3f;

public class EditableMeshRenderer {

    public static Mesh point = Loader.getInstance().loadToVAO(new float[] { 0, 0, 0 }, 3);

    static {
        point.setAABB(AABB.generateAABB(new float[] { 0, 0, 0 }, 0.1f));
    }

    public static void render(List<EditableMesh> editableMeshes) {
        Profiler.start("Deferred (Editable)");

        {
            EditableMeshShader shader = EditableMeshShader.getInstance();
            shader.start();

            for (EditableMesh mesh : editableMeshes) {
                Renderer.bind(mesh.model);

                mesh.material.bind();
                shader.loadMeshSpecificInformation(mesh);

                Renderer.render(mesh.model);
                Renderer.unbind(mesh.model);
            }
            shader.stop();
        }

        {
            DebugShader shader = DebugShader.getInstance();
            GL30.glBindVertexArray(point.getVAO());
            GL20.glEnableVertexAttribArray(0);
            shader.start();

            for (TransformableObject object : LevelEditor.getInstance().getSelectedObjects()) {
                if (object instanceof EditableMesh || object instanceof Vertex) {
                    EditableMesh mesh = null;
                    if (object instanceof EditableMesh)
                        mesh = (EditableMesh) object;
                    else if (object instanceof Vertex)
                        mesh = ((Vertex) object).getMesh();

                    if (EditableMesh.editableMeshComponent.selectionMode == SelectionMode.VERTEX) {
                        for (Vertex vertex : mesh.vertices) {
                            Vector3f colour = new Vector3f(1, 0, 1);
                            if (vertex == object) {
                                GL11.glDisable(GL11.GL_DEPTH_TEST);
                                colour.set(1, 0, 0);
                            }
                            GL11.glPointSize(8);

                            shader.loadInformation(GameClient.getPlayer().getComponent(LivingComponent.class).getViewMatrix(), vertex.getTransformationMatrix(), new Vector3f(1, 0, 1), 1);
                            GL11.glDrawArrays(GL11.GL_POINTS, 0, point.getVertexCount());
                            if (vertex == object)
                                GL11.glEnable(GL11.GL_DEPTH_TEST);
                        }
                    }
                }
            }

            shader.stop();
            GL20.glDisableVertexAttribArray(0);
            GL30.glBindVertexArray(0);
        }

        Profiler.end("Deferred (Editable)");
    }

}
