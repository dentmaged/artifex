package org.anchor.game.editor.plugins;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.swing.JComponent;
import javax.swing.JMenu;

import org.anchor.engine.common.Log;
import org.anchor.engine.common.utils.FileHelper;
import org.anchor.game.editor.ui.Window;
import org.ho.yaml.Yaml;

public class PluginManager {

    private List<BasePlugin> loadedPlugins = new ArrayList<BasePlugin>();
    private Map<JMenu, BasePlugin> menuOwners = new HashMap<JMenu, BasePlugin>();
    private Map<JComponent, BasePlugin> panelOwners = new HashMap<JComponent, BasePlugin>();

    public void registerMenu(JMenu menu, BasePlugin plugin) {
        if (!loadedPlugins.contains(plugin)) {
            Log.warning("Plugin " + plugin.getData().name + " is trying to register menu " + menu.getText() + ", but isn't loaded. This shouldn't happen! Aborting...");

            return;
        }

        if (menuOwners.containsKey(menu)) {
            Log.warning("Menu " + menu.getText() + " in plugin " + plugin.getData().name + " has already been registered! Aborting...");

            return;
        }

        Window.getInstance().registerMenu(menu);
        menuOwners.put(menu, plugin);
    }

    public void registerPanel(String title, BasePlugin plugin, JComponent component, int position) {
        if (!loadedPlugins.contains(plugin)) {
            Log.warning("Plugin " + plugin.getData().name + " is trying to register panel " + title + ", but isn't loaded. This shouldn't happen! Aborting...");

            return;
        }

        if (panelOwners.containsKey(component)) {
            Log.warning("Panel " + title + " in plugin " + plugin.getData().name + " has already been registered! Aborting...");

            return;
        }

        Window.getInstance().registerPanel(title, component, position);
        panelOwners.put(component, plugin);
    }

    public void loadPluginsFromDirectory(File dir) {
        for (File child : dir.listFiles()) {
            if (!child.getName().toLowerCase().endsWith(".jar"))
                continue;

            loadPlugin(child);
        }
    }

    public void loadPlugin(File jar) {
        try {
            PluginDataContainer data = parseYAML(jar);
            if (data == null) {
                Log.warning("Failed to load plugin.yml" + jar.getAbsolutePath());

                return;
            }

            URLClassLoader classLoader = new URLClassLoader(new URL[] { jar.toURI().toURL(), }, this.getClass().getClassLoader());
            Class<?> main = Class.forName(data.mainClass, true, classLoader);
            if (!BasePlugin.class.isAssignableFrom(main)) {
                Log.warning("Main class " + main.getName() + " in jar " + jar.getAbsolutePath() + " does not implement IPlugin! Aborting...");

                return;
            }
            Object instance = main.newInstance();

            Field dataField = main.getField("data");
            dataField.setAccessible(true);
            dataField.set(instance, data);
            dataField.setAccessible(false);

            loadedPlugins.add((BasePlugin) instance);
            main.getMethod("enable").invoke(instance);
        } catch (Exception e) {
            Log.warning("Failed to load plugin " + jar.getAbsolutePath());
            e.printStackTrace();
        }
    }

    public void loadInternalPlugin(PluginDataContainer data) {
        try {
            Class<?> main = Class.forName(data.mainClass);
            if (!BasePlugin.class.isAssignableFrom(main)) {
                Log.warning("Main class " + main.getName() + " (internal) does not implement IPlugin! Aborting...");

                return;
            }
            Object instance = main.newInstance();

            Field dataField = main.getField("data");
            dataField.setAccessible(true);
            dataField.set(instance, data);
            dataField.setAccessible(false);

            loadedPlugins.add((BasePlugin) instance);
            main.getMethod("enable").invoke(instance);
        } catch (Exception e) {
            Log.warning("Failed to load plugin " + data.name);
            e.printStackTrace();
        }
    }

    private String getYAML(File jar) {
        try {
            ZipFile zip = new ZipFile(jar);
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (!entry.getName().equalsIgnoreCase("plugin.yml"))
                    continue;

                zip.close();
                return FileHelper.read(zip.getInputStream(entry));
            }

            zip.close();
        } catch (Exception e) {
            Log.warning("Failed to load plugin yaml " + jar.getAbsolutePath());
            e.printStackTrace();
        }

        return null;
    }

    private PluginDataContainer parseYAML(File jar) {
        String contents = getYAML(jar);
        if (contents == null)
            return null;

        Object object = Yaml.load(contents);
        if (!(object instanceof Map))
            return null;

        PluginDataContainer container = new PluginDataContainer();
        Map<Object, Object> data = (Map<Object, Object>) object;

        container.name = value(data.get("name"));
        container.description = value(data.get("description"));
        container.version = value(data.get("version"));
        container.mainClass = value(data.get("main"));

        if (data.containsKey("authors") && data.get("authors") instanceof String[])
            container.authors = (String[]) data.get("authors");

        if (container.name == null || container.mainClass == null) {
            Log.warning("Missing name or main field in plugin.yml in plugin " + jar.getAbsolutePath());
            return null;
        }

        return container;
    }

    private String value(Object object) {
        if (object == null)
            return null;

        return object.toString();
    }

}
