package org.anchor.game.server;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.anchor.engine.common.utils.FileHelper;
import org.anchor.engine.shared.components.IComponent;
import org.anchor.engine.shared.entity.Entity;
import org.anchor.engine.shared.scene.Scene;
import org.anchor.engine.shared.terrain.Terrain;
import org.anchor.engine.shared.utils.Property;
import org.anchor.engine.shared.utils.RawParser;

public class GameMap {

    protected File file;
    protected Scene scene;

    public static String PARTS = ((char) 2) + "";
    public static String ENTITY_END = ((char) 3) + "";
    public static String TERRAIN_END = ((char) 4) + "";
    public static String SUBPARTS = ((char) 5) + "";

    public static Map<Entity, Integer> parents = new HashMap<Entity, Integer>();

    public GameMap(File file) {
        this.file = file;

        load();
    }

    public void load() {
        ServerEngine.ENTITY_ID = 1;
        scene = new Scene();

        String contents = FileHelper.read(file);
        String[] lines = contents.split("\n");
        int i = 0;
        while (!lines[i].equals(ENTITY_END)) {
            Entity entity = parse(lines[i]);
            if (entity != null) {
                scene.getEntities().add(entity);
                entity.setLineIndex(i);
            }

            i++;
        }
        setParents(scene.getEntities());

        i++;
        while (!lines[i].equals(TERRAIN_END)) {
            String[] parts = lines[i].split(PARTS);
            String[] coordinates = parts[0].split(SUBPARTS);
            float size = Terrain.DEFAULT_SIZE;
            if (parts.length > 3)
                size = Float.parseFloat(parts[3]);

            scene.getTerrains().add(new Terrain(size, Integer.parseInt(coordinates[0]), Integer.parseInt(coordinates[1]), parts[1]));
            i++;
        }
    }

    public Scene getScene() {
        return scene;
    }

    private static Map<String, String> getValues(String input) {
        Map<String, String> map = new HashMap<String, String>();
        if (input.length() > 0) {
            for (String part : input.split(SUBPARTS)) {
                String[] parts = split(part, ":");

                map.put(parts[0], parts[1]);
            }
        }

        return map;
    }

    public static Field getField(Class<?> clazz, String name) {
        for (Field field : clazz.getFields()) {
            Property property = field.getAnnotation(Property.class);
            if (property != null && property.value().equals(name))
                return field;
            ;
        }

        return null;
    }

    public static String[] split(String input, String split) {
        String[] arr = input.split(split);
        if (input.lastIndexOf(split) == input.length() - 1) {
            String[] spl = new String[arr.length + 1];
            for (int i = 0; i < arr.length; i++)
                spl[i] = arr[i];
            spl[arr.length] = "";

            arr = spl;
        }

        return arr;
    }

    public static Entity parse(String line) {
        String[] parts = split(line, PARTS);
        Entity entity = new Entity();

        try {
            for (Entry<String, String> entry : getValues(parts[0]).entrySet()) {
                if (entry.getKey().equals("parent"))
                    parents.put(entity, Integer.parseInt(entry.getValue()));

                entity.setValue(entry.getKey(), entry.getValue());
            }

            List<Integer> skip = new ArrayList<Integer>();
            int i = 0;
            for (String component : parts[1].split(SUBPARTS)) {
                if (component.startsWith("org.anchor.game.client")) {
                    skip.add(i);
                    i++;

                    continue;
                }

                Class<IComponent> clazz = (Class<IComponent>) Class.forName(component);
                entity.addComponent(clazz.newInstance());
                i++;
            }

            if (skip.size() + 3 == parts.length)
                return null;

            i = 0;
            for (int j = 0; j < entity.getComponents().size() + skip.size(); j++) {
                if (skip.contains(j)) {
                    i++;
                    continue;
                }

                IComponent component = entity.getComponents().get(j - i);
                String comp = parts[j + 2];

                if (comp.length() > 0) {
                    for (String keyValue : comp.split(SUBPARTS)) {
                        String[] subparts = split(keyValue, ":");
                        String name = subparts[0];
                        String value = subparts[1];

                        Field field = getField(component.getClass(), name);
                        if (field != null)
                            field.set(component, RawParser.getInstance().decode(value, field.getType()));
                        else
                            System.err.println("[WARN] Map load: field " + name + " (in component " + component.getClass().getSimpleName() + ") does not exist!");
                    }
                }
            }
            entity.spawn();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return entity;
    }

    public static void setParents(List<Entity> entities) {
        for (Entry<Entity, Integer> entry : parents.entrySet())
            entry.getKey().setParent(entities.get(entry.getValue()));

        parents.clear();
    }

}