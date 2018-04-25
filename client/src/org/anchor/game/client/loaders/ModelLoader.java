package org.anchor.game.client.loaders;

import java.util.HashMap;
import java.util.Map;

import org.anchor.client.engine.renderer.types.MeshRequest;
import org.anchor.client.engine.renderer.types.Model;
import org.anchor.client.engine.renderer.types.ModelTexture;
import org.anchor.engine.common.utils.FileHelper;
import org.anchor.game.client.async.Requester;

public class ModelLoader {

    private static Map<String, Model> models = new HashMap<String, Model>();

    public static Model loadModel(String name) {
        if (!models.containsKey(name.toLowerCase())) {
            ModelTexture texture = new ModelTexture(Requester.requestTexture(name + "_diffuse"));
            MeshRequest mesh = null;

            if (FileHelper.newGameFile("res", name + "_normal.png").exists()) {
                mesh = Requester.requestNormalMesh(name);
                texture.setNormalMap(Requester.requestTexture(name + "_normal"));
            } else {
                mesh = Requester.requestMesh(name);
            }

            if (FileHelper.newGameFile("res", name + "_specular.png").exists())
                texture.setSpecularMap(Requester.requestTexture(name + "_specular"));

            models.put(name, new Model(mesh, texture));
        }

        return models.get(name.toLowerCase());
    }

}
