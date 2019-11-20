package org.anchor.engine.common.script;

import java.util.ArrayList;
import java.util.List;

import javax.script.Invocable;
import javax.script.ScriptEngine;

public class Script {

    private String file;
    private ScriptEngine engine;
    private Invocable invocable;

    private List<ScriptProperty> properties;

    public Script(String file, ScriptEngine engine) {
        this.file = file;
        this.engine = engine;
        this.invocable = (Invocable) engine;
        this.properties = new ArrayList<ScriptProperty>();
    }

    public Object invoke(String name, Object... args) {
        try {
            return invocable.invokeFunction(name, args);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean functionExists(String name) {
        try {
            return (boolean) engine.eval("typeof " + name + " === 'function'");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public Object get(String key) {
        return engine.get(key);
    }

    public void put(String key, Object value) {
        engine.put(key, value);
    }

    public String getFile() {
        return file;
    }

    public ScriptEngine getEngine() {
        return engine;
    }

    public void addProperty(String name, String variable, Class<?> type) {
        for (ScriptProperty property : properties)
            if (property.getName().equals(name) && property.getVariable().equals(variable) && property.getType() == type)
                return;

        properties.add(new ScriptProperty(this, name, variable, type));
    }

    public List<ScriptProperty> getProperties() {
        return properties;
    }

}
