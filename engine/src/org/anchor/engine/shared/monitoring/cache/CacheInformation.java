package org.anchor.engine.shared.monitoring.cache;

public class CacheInformation {

    private Object original;

    public CacheInformation(Object original) {
        this.original = original;
    }

    public Object getOriginal() {
        return original;
    }

}
