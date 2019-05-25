package org.anchor.game.editor.shaders;

import org.anchor.client.engine.renderer.Renderer;
import org.anchor.engine.shared.components.LivingComponent;
import org.anchor.game.client.GameClient;
import org.anchor.game.client.shaders.ModelShader;
import org.anchor.game.editor.editableMesh.EditableMesh;
import org.lwjgl.util.vector.Matrix4f;

public class EditableMeshShader extends ModelShader {

    private static EditableMeshShader instance = new EditableMeshShader();

    protected EditableMeshShader() {
        super("static");
    }

    @Override
    public void onBind() {
        super.onBind();

        loadInt("specular", 2);
        loadInt("metallic", 3);
        loadInt("roughness", 4);
        loadInt("ao", 5);
    }

    public void loadMeshSpecificInformation(EditableMesh mesh) {
        loadMatrix("projectionViewTransformationMatrix", Matrix4f.mul(Renderer.getProjectionMatrix(), Matrix4f.mul(GameClient.getPlayer().getComponent(LivingComponent.class).getViewMatrix(), mesh.getTransformationMatrix(), null), null));
        loadMatrix("normalMatrix", GameClient.getPlayer().getComponent(LivingComponent.class).getNormalMatrix(mesh.getRotation()));

        loadFloat("numberOfRows", mesh.material.getNumberOfRows());
        loadVector("colour", mesh.colour);
        loadBoolean("useAOMap", mesh.material.hasAmbientOcclusionMap());
    }

    public static EditableMeshShader getInstance() {
        return instance;
    }

}
