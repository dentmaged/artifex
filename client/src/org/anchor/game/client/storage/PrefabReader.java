package org.anchor.game.client.storage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.anchor.engine.common.utils.FileHelper;
import org.anchor.engine.shared.Engine;
import org.anchor.engine.shared.entity.Entity;
import org.anchor.game.client.types.ClientScene;

public class PrefabReader {

    public static List<Entity> read(File file) {
        List<Entity> entities = new ArrayList<Entity>();

        String contents = FileHelper.read(file);
        String[] lines = contents.split("\n");
        for (int i = 0; i < lines.length; i++)
            entities.add(GameMap.parse((ClientScene) Engine.getScene(), lines[i]));

        GameMap.setParents(entities);

        return entities;
    }

}
