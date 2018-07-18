package org.anchor.engine.shared.console;

import org.anchor.engine.shared.Engine;
import org.anchor.engine.shared.net.packet.GameVariablePacket;

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
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {

        }

        return 0;
    }

    public float getValueAsFloat() {
        try {
            return Float.valueOf(value);
        } catch (NumberFormatException e) {

        }

        return 0;
    }

    public boolean getValueAsBool() {
        return getValueAsInt() != 0;
    }

    public void setValue(String value) {
        if (type == GameVariableType.CHEAT && !GameVariableManager.getByName("sv_cheats").getValueAsBool())
            return;
        if (Engine.isClientSide() && type == GameVariableType.GAMEMODE) {
            Engine.getInstance().broadcast(new GameVariablePacket(name, value));

            return;
        }

        this.value = value;

        if (Engine.isServerSide())
            Engine.getInstance().broadcast(new GameVariablePacket(value, value));
    }

    public void setValue(int value) {
        setValue(value + "");
    }

    public void setValue(float value) {
        setValue(value + "");
    }

    public void setValue(boolean value) {
        setValue(value ? "1" : "0");
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
