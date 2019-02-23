package org.anchor.game.editor.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.anchor.engine.common.utils.FileHelper;
import org.anchor.engine.shared.entity.Entity;
import org.anchor.engine.shared.entity.IComponent;
import org.anchor.engine.shared.terrain.Terrain;
import org.anchor.engine.shared.utils.Layer;
import org.anchor.engine.shared.utils.Property;
import org.anchor.engine.shared.utils.RawParser;
import org.anchor.engine.shared.utils.TerrainUtils;
import org.anchor.game.client.components.LightProbeComponent;
import org.anchor.game.client.components.ReflectionProbeComponent;
import org.anchor.game.client.storage.GameMap;
import org.anchor.game.client.types.ClientScene;
import org.anchor.game.client.types.ClientTerrain;

public class MapWriter {

    public static void write(File file, ClientScene scene) {
        String text = "";
        try {
            text += GameMap.INFO + GameMap.MAP_VERSION + GameMap.PARTS;
            for (Layer layer : scene.getLayers()) {
                if (layer == scene.getDefaultLayer())
                    continue;

                text += layer.getName() + "@" + layer.getColour().getRed() + "@" + layer.getColour().getGreen() + "@" + layer.getColour().getBlue() + "@" + layer.isPickable() + "@" + layer.isVisible() + "@" + GameMap.SUBPARTS;
            }
            text += "\n";

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
                text += GameMap.PARTS;

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

        FileHelper.write(file, text);
        if (scene.getTerrains().size() > 0 || scene.getComponents(ReflectionProbeComponent.class).size() > 0 || scene.getComponents(LightProbeComponent.class).size() > 0) {
            try {
                File storage = new File(file.getAbsolutePath().replace(".asg", ".ads"));
                ZipOutputStream stream = new ZipOutputStream(new FileOutputStream(storage));

                for (Terrain shared : scene.getTerrains()) {
                    stream.putNextEntry(new ZipEntry("terraindata/" + shared.getGX() + "/" + shared.getGZ() + "/height"));

                    OutputStreamWriter writer = new OutputStreamWriter(stream);
                    writer.write(shared.getVerticesPerSide() + TerrainUtils.PARTS);

                    for (int i = 0; i < shared.getVerticesPerSide(); i++)
                        for (int j = 0; j < shared.getVerticesPerSide(); j++)
                            writer.write(shared.getHeightAt(i, j) + TerrainUtils.PARTS);
                    writer.flush();

                    stream.closeEntry();
                }

                stream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static int getIndex(List<Entity> entities, Entity target) {
        for (int i = 0; i < entities.size(); i++) {
            if (entities.get(i) == target)
                return i;
        }

        return -1;
    }

    protected static int getColourFromHeightmap(float height) {
        height /= Terrain.MAX_HEIGHT;
        height *= Terrain.MAX_PIXEL_COLOUR / 2f;
        height -= Terrain.MAX_PIXEL_COLOUR / 2f;

        return (int) height;
    }

}
