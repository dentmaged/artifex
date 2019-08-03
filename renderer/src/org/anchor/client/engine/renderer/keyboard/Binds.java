package org.anchor.client.engine.renderer.keyboard;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.lwjgl.input.Keyboard;

public class Binds {

    private Map<String, String> binds = new HashMap<String, String>();
    private RunCommandCallback callback;

    private static Binds instance;

    public static boolean inUI = false;

    public void init(RunCommandCallback callback) {
        this.callback = callback;
    }

    public void update() {
        if (inUI)
            return;

        for (int key : KeyboardUtils.getKeysPressed()) {
            String name = Keyboard.getKeyName(key).toLowerCase();
            String command = binds.get(name);

            if (command == null || command.startsWith("+"))
                continue;

            callback.runCommand(command);
        }
    }

    public void keyUse(int key, boolean action) {
        if (inUI)
            return;

        String name = Keyboard.getKeyName(key).toLowerCase();
        String command = binds.get(name);

        if (command == null || !command.startsWith("+"))
            return;

        Keys.setPressed(command.substring(1), action);
    }

    public String toConfigString() {
        StringBuilder output = new StringBuilder();
        for (Entry<String, String> entry : binds.entrySet())
            output.append("bind ").append(entry.getKey()).append(" ").append(entry.getValue()).append("\n");

        return output + "";
    }

    public void addBind(String key, String command) {
        binds.put(key.toLowerCase(), command);
    }

    public static Binds getInstance() {
        if (instance == null)
            instance = new Binds();

        return instance;
    }

}
