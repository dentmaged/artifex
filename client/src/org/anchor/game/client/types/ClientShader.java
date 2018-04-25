package org.anchor.game.client.types;

import org.anchor.client.engine.renderer.Shader;
import org.anchor.engine.shared.entity.Entity;

public abstract class ClientShader extends Shader {

    public ClientShader(String program) {
        super(program);
    }

    @Override
    public void start() {
        super.start();
        onBind();
    }

    @Override
    public void stop() {
        super.stop();
        onUnbind();
    }

    public abstract void onBind();

    public abstract void loadEntitySpecificInformation(Entity entity);

    public abstract void onUnbind();

}
