package org.anchor.game.client.shaders;

import org.anchor.client.engine.renderer.Renderer;
import org.anchor.engine.shared.entity.Entity;
import org.lwjgl.util.vector.Matrix4f;

public class ViewmodelShader extends StaticShader {

    private static ViewmodelShader instance = new ViewmodelShader();

    private Matrix4f identity;

    protected ViewmodelShader() {
        super();
    }

    @Override
    public void onBind() {
        super.onBind();

        if (identity == null)
            identity = new Matrix4f();

        loadMatrix("viewMatrix", identity);
    }

    @Override
    public void loadEntitySpecificInformation(Entity entity) {
        super.loadEntitySpecificInformation(entity);

        loadMatrix("projectionViewTransformationMatrix", Matrix4f.mul(Renderer.getProjectionMatrix(), entity.getTransformationMatrix(), null));
    }

    public static ViewmodelShader getInstance() {
        return instance;
    }

}
