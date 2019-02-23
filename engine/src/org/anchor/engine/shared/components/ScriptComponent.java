package org.anchor.engine.shared.components;

import javax.script.ScriptEngine;

import org.anchor.engine.common.script.Script;
import org.anchor.engine.common.script.ScriptLoader;
import org.anchor.engine.shared.entity.Entity;
import org.anchor.engine.shared.entity.IComponent;
import org.anchor.engine.shared.utils.Property;

public class ScriptComponent implements IComponent, IInteractable {

    @Property("Script")
    public Script script;

    protected Entity entity;

    protected boolean precache, spawn, update, updateFixed, interact, setValue;

    @Override
    public void precache(Entity entity) {
        this.entity = entity;
        if (script == null && entity.containsKey("script"))
            script = load(this, entity.getValue("script"));

        if (spawn)
            script.invoke("spawn", entity, this);
    }

    @Override
    public void spawn() {

    }

    @Override
    public void update() {
        if (update)
            script.invoke("update", entity, this);
    }

    @Override
    public void updateFixed() {
        if (updateFixed)
            script.invoke("updateFixed", entity, this);
    }

    @Override
    public void setValue(String key, String value) {
        if (setValue)
            script.invoke("setValue", entity, this, key, value);
    }

    @Override
    public void interact() {
        if (interact)
            script.invoke("interact", entity, this);
    }

    @Override
    public IComponent copy() {
        ScriptComponent copy = new ScriptComponent();
        copy.script = script;

        copy.precache = precache;
        copy.spawn = spawn;
        copy.update = update;
        copy.updateFixed = updateFixed;
        copy.setValue = setValue;
        copy.interact = interact;

        return copy;
    }

    public static Script load(ScriptComponent component, String file) {
        Script script = ScriptLoader.loadScript(file);

        component.precache = script.functionExists("precache");
        component.spawn = script.functionExists("spawn");
        component.update = script.functionExists("update");
        component.updateFixed = script.functionExists("updateFixed");
        component.setValue = script.functionExists("setValue");
        component.interact = script.functionExists("interact");

        return script;
    }

    public static void registerProperty(ScriptEngine engine, String name, String variable, Class<?> type) {
        Script script = ScriptLoader.getScript(engine);
        script.addProperty(name, variable, type);
    }

}
