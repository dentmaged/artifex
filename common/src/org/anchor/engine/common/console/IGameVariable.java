package org.anchor.engine.common.console;

public interface IGameVariable {

    public String getName();

    public String getDefaultValue();

    public String getDescription();

    public GameVariableType getType();

    public String getValueAsString();

    public int getValueAsInt();

    public float getValueAsFloat();

    public boolean getValueAsBool();

    public void setValue(String value);

    public void setValue(int value);

    public void setValue(float value);

    public void setValue(boolean value);

}
