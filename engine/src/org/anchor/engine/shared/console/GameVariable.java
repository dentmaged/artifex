package org.anchor.engine.shared.console;

import org.anchor.engine.common.console.CoreGameVariableManager;
import org.anchor.engine.common.console.GameVariableType;
import org.anchor.engine.common.console.IGameVariable;
import org.anchor.engine.shared.Engine;
import org.anchor.engine.shared.events.GameVariableUpdateEvent;

public class GameVariable implements IGameVariable {

    private String name, value, defaultValue, description;
    private GameVariableType type;

    public GameVariable(String name, String defaultValue, String description, GameVariableType type) {
        this.name = name;
        this.value = defaultValue;
        this.defaultValue = defaultValue;
        this.description = description;
        this.type = type;

        CoreGameVariableManager.register(this);
    }

    @Override
    public String getValueAsString() {
        return value;
    }

    @Override
    public int getValueAsInt() {
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {

        }

        return Integer.valueOf(defaultValue);
    }

    @Override
    public float getValueAsFloat() {
        try {
            return Float.valueOf(value);
        } catch (NumberFormatException e) {

        }

        return Float.valueOf(defaultValue);
    }

    @Override
    public boolean getValueAsBool() {
        return getValueAsInt() != 0 || getValueAsString().equals("true");
    }

    @Override
    public void setValue(String value) {
        if (type == GameVariableType.CHEAT && !EngineGameVariables.sv_cheats.getValueAsBool())
            return;

        this.value = value;
        Engine.bus.fireEvent(new GameVariableUpdateEvent(this));
    }

    @Override
    public void setValue(int value) {
        setValue(value + "");
    }

    @Override
    public void setValue(float value) {
        setValue(value + "");
    }

    @Override
    public void setValue(boolean value) {
        setValue(value ? "1" : "0");
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDefaultValue() {
        return defaultValue;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public GameVariableType getType() {
        return type;
    }

}
