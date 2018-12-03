package org.anchor.engine.common.script;

public class ScriptProperty {

    private Script script;
    private String name, variable;
    private Class<?> type;

    public ScriptProperty(Script script, String name, String variable, Class<?> type) {
        this.script = script;
        this.name = name;
        this.variable = variable;
        this.type = type;
    }

    public Script getScript() {
        return script;
    }

    public String getName() {
        return name;
    }

    public String getVariable() {
        return variable;
    }

    public Class<?> getType() {
        return type;
    }

}
