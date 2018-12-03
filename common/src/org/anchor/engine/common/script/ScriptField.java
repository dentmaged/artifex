package org.anchor.engine.common.script;

import org.anchor.engine.common.utils.JavaField;

public class ScriptField implements JavaField {

    protected ScriptProperty property;

    public ScriptField(ScriptProperty property) {
        this.property = property;
    }

    @Override
    public Object get(Object instance) {
        return property.getScript().get(property.getVariable());
    }

    @Override
    public void set(Object instance, Object value) {
        property.getScript().put(property.getVariable(), value);
    }

    @Override
    public String getName() {
        return property.getVariable();
    }

    @Override
    public Class<?> getType() {
        return property.getType();
    }

}
