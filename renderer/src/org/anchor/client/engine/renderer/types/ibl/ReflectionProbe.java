package org.anchor.client.engine.renderer.types.ibl;

public interface ReflectionProbe extends Probe {

    public int getPrefilteredCubemap();

    @Override
    public default boolean isBaked() {
        return getPrefilteredCubemap() > 0;
    }

}
