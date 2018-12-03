package org.anchor.client.engine.renderer.types.ibl;

public interface LightProbe extends Probe {

    public int getIrradianceCubemap();

    @Override
    public default boolean isBaked() {
        return getIrradianceCubemap() > 0;
    }

}
