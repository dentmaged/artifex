package org.anchor.game.client.shaders;

import org.anchor.engine.shared.components.LivingComponent;
import org.anchor.engine.shared.entity.Entity;
import org.anchor.game.client.GameClient;
import org.anchor.game.client.components.MeshComponent;
import org.anchor.game.client.types.ClientShader;
import org.lwjgl.util.vector.Matrix4f;

public class ModelShader extends ClientShader {

    protected static String texture = "modelTexture";

    protected ModelShader(String program) {
        super(program);
    }

    @Override
    public void onBind() {
        loadMatrix("viewMatrix", GameClient.getPlayer().getComponent(LivingComponent.class).getViewMatrix());

        loadInt(texture, 0);
    }

    @Override
    public void loadEntitySpecificInformation(Entity entity) {
        Matrix4f transformationMatrix = entity.getTransformationMatrix();
        loadMatrix("transformationMatrix", transformationMatrix);
        loadMatrix("normalMatrix", GameClient.getPlayer().getComponent(LivingComponent.class).getNormalMatrix(transformationMatrix));

        MeshComponent render = entity.getComponent(MeshComponent.class);
        loadFloat("numberOfRows", render.model.getTexture().getNumberOfRows());
        loadVector("textureOffset", render.getTextureOffset());
        loadVector("colour", render.colour);
    }

    @Override
    public void onUnbind() {

    }

    @Override
    protected void bindAttributes() {
        super.bindFragOutput(0, "out_diffuse");
        super.bindFragOutput(1, "out_position");
        super.bindFragOutput(2, "out_normal");
        super.bindFragOutput(3, "out_bloom");
        super.bindFragOutput(4, "out_godrays");

        super.bindAttribute(0, "position");
        super.bindAttribute(1, "textureCoordinates");
        super.bindAttribute(2, "normal");
    }

}
