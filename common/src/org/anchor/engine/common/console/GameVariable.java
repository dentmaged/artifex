package org.anchor.engine.common.console;

public class GameVariable {

    private String name, value, defaultValue, description;
    private GameVariableType type;

    public GameVariable(String name, String defaultValue, String description, GameVariableType type) {
        this.name = name;
        this.value = defaultValue;
        this.defaultValue = defaultValue;
        this.description = description;
        this.type = type;

        GameVariableManager.register(this);
    }

    public String getValueAsString() {
        return value;
    }

    public int getValueAsInt() {
        return Integer.valueOf(value);
    }

    public float getValueAsFloat() {
        return Float.valueOf(value);
    }

    public boolean getValueAsBool() {
        return getValueAsInt() != 0;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setValue(int value) {
        this.value = value + "";
    }

    public void setValue(float value) {
        this.value = value + "";
    }

    public void setValue(boolean value) {
        this.value = value ? "1" : "0";
    }

    public String getName() {
        return name;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public String getDescription() {
        return description;
    }

    public GameVariableType getType() {
        return type;
    }

}
