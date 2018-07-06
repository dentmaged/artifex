package org.anchor.game.client.shaders;

import org.anchor.client.engine.renderer.Renderer;
import org.anchor.engine.shared.components.LivingComponent;
import org.anchor.engine.shared.entity.Entity;
import org.anchor.game.client.GameClient;
import org.anchor.game.client.components.MeshComponent;
import org.anchor.game.client.types.ClientShader;
import org.lwjgl.util.vector.Matrix4f;

public class ModelShader extends ClientShader {

    protected static String texture = "albedo";

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
        loadMatrix("projectionViewTransformationMatrix", Matrix4f.mul(Matrix4f.mul(Renderer.getProjectionMatrix(), GameClient.getPlayer().getComponent(LivingComponent.class).getViewMatrix(), null), entity.getTransformationMatrix(), null));
        loadMatrix("normalMatrix", GameClient.getPlayer().getComponent(LivingComponent.class).getNormalMatrix(entity));

        MeshComponent render = entity.getComponent(MeshComponent.class);
        if (render == null)
            return;

        loadFloat("numberOfRows", render.model.getTexture().getNumberOfRows());
        loadVector("textureOffset", render.getTextureOffset());
        loadVector("colour", render.colour);
    }

    @Override
    public void onUnbind() {

    }

    @Override
    protected void bindAttributes() {
        bindFragOutput(0, "out_diffuse");
        bindFragOutput(1, "out_other");
        bindFragOutput(2, "out_normal");
        bindFragOutput(3, "out_bloom");
        bindFragOutput(4, "out_godrays");

        bindAttribute(0, "position");
        bindAttribute(1, "textureCoordinates");
        bindAttribute(2, "normal");
    }

}
