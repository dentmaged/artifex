package org.anchor.game.editor.utils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map.Entry;

import org.anchor.engine.shared.components.IComponent;
import org.anchor.engine.shared.entity.Entity;
import org.anchor.engine.shared.terrain.Terrain;
import org.anchor.engine.shared.utils.Property;
import org.anchor.engine.shared.utils.RawParser;
import org.anchor.game.client.storage.GameMap;
import org.anchor.game.client.types.ClientScene;
import org.anchor.game.client.types.ClientTerrain;

public class MapWriter {

    public static String write(ClientScene scene) {
        String text = "";
        try {
            for (Entity entity : scene.getEntities()) {
                for (Entry<String, String> entry : entity.entrySet())
                    text += entry.getKey() + ":" + entry.getValue() + GameMap.SUBPARTS;
                if (entity.getParent() != null)
                    text += "parent:" + getIndex(scene.getEntities(), entity.getParent()) + GameMap.SUBPARTS;

                if (entity.entrySet().size() > 0 || entity.getParent() != null)
                    text = text.substring(0, text.length() - 1);
                text += GameMap.PARTS;

                for (IComponent component : entity.getComponents())
                    text += component.getClass().getName() + GameMap.SUBPARTS;
                if (entity.getComponents().size() > 0)
                    text = text.substring(0, text.length() - 1);
                text += GameMap.PARTS;

                for (IComponent component : entity.getComponents()) {
                    boolean x = false;
                    for (Field field : component.getClass().getFields()) {
                        Property property = field.getAnnotation(Property.class);
                        if (property != null) {
                            x = true;
                            text += property.value() + ":" + RawParser.getInstance().encode(field.get(component)) + GameMap.SUBPARTS;
                        }
                    }

                    if (x)
                        text = text.substring(0, text.length() - 1);
                    text += GameMap.PARTS;
                }
                text = text.substring(0, text.length() - 1) + "\n";
            }
            text += GameMap.ENTITY_END + "\n";

            for (Terrain shared : scene.getTerrains()) {
                ClientTerrain terrain = (ClientTerrain) shared;

                text += terrain.getGX() + GameMap.SUBPARTS + terrain.getGZ() + GameMap.PARTS;
                text += terrain.getHeightmap() + GameMap.PARTS;

                text += terrain.getTextures().getBlendmapName() + GameMap.SUBPARTS;
                text += terrain.getTextures().getBackgroundName() + GameMap.SUBPARTS;
                text += terrain.getTextures().getRedName() + GameMap.SUBPARTS;
                text += terrain.getTextures().getGreenName() + GameMap.SUBPARTS;
                text += terrain.getTextures().getBlueName() + "\n";
            }

            text += GameMap.TERRAIN_END + "\n";
        } catch (Exception e) {
            e.printStackTrace();
        }

        return text;
    }

    public static int getIndex(List<Entity> entities, Entity target) {
        for (int i = 0; i < entities.size(); i++) {
            if (entities.get(i) == target)
                return i;
        }

        return -1;
    }

}
