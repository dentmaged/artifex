package org.anchor.game.client.settings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

import org.anchor.client.engine.renderer.Settings;
import org.anchor.engine.common.utils.FileHelper;

public class SettingsManager {

    private Properties properties;
    private File file;

    private static SettingsManager instance;

    private SettingsManager() {
        instance = this;

        properties = new Properties();
        file = new File(FileHelper.getAppData("anchor"), "settings.ini");
        FileHelper.createIfNotExists(file);

        try {
            properties.load(new FileInputStream(file));
        } catch (Exception e) {
            e.printStackTrace();
        }

        createDefaults();
        setup();
    }

    private void createDefaults() {
        if (!exists("display.resolution"))
            set("resolution", "1280x720");

        if (!exists("display.fullscreen"))
            set("fullscreen", "false");
    }

    public void setup() {
        String[] resolution = get("display.resolution").split("x");
        if (resolution.length < 2)
            resolution = new String[] { "1280", "720" };

        Settings.width = Integer.parseInt(resolution[0]);
        Settings.height = Integer.parseInt(resolution[1]);
        Settings.fullscreen = getBoolean("display.fullscreen");
    }

    public void set(String key, String value) {
        properties.setProperty(key, value);
    }

    public void setInt(String key, int value) {
        properties.setProperty(key, value + "");
    }

    public void setBoolean(String key, boolean value) {
        properties.setProperty(key, value + "");
    }

    public void setFloat(String key, float value) {
        properties.setProperty(key, value + "");
    }

    public boolean exists(String key) {
        return properties.containsKey(key);
    }

    public String get(String key) {
        return properties.getProperty(key);
    }

    public int getInt(String key) {
        return Integer.parseInt(properties.getProperty(key));
    }

    public boolean getBoolean(String key) {
        return Boolean.parseBoolean(properties.getProperty(key));
    }

    public float getFloat(String key) {
        return Float.parseFloat(properties.getProperty(key));
    }

    public void cleanUp() {
        try {
            properties.store(new FileOutputStream(file), "Anchor Game Engine");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static SettingsManager getInstance() {
        if (instance == null)
            new SettingsManager();

        return instance;
    }

}