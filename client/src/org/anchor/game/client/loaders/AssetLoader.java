package org.anchor.game.client.loaders;

import java.util.HashMap;
import java.util.Map;

import org.anchor.client.engine.renderer.Loader;
import org.anchor.client.engine.renderer.types.Model;
import org.anchor.client.engine.renderer.types.mesh.MeshRequest;
import org.anchor.client.engine.renderer.types.texture.ModelTexture;
import org.anchor.engine.common.utils.FileHelper;
import org.anchor.game.client.async.Requester;
import org.anchor.game.client.loaders.obj.ModelData;
import org.anchor.game.client.loaders.obj.OBJFileLoader;

public class AssetLoader {

    private static Map<String, Model> models = new HashMap<String, Model>();
    private static Map<String, ModelTexture> textures = new HashMap<String, ModelTexture>();

    public static Model loadModel(String name) {
        return loadModel(name, false);
    }

    public static Model loadModel(String name, boolean singleThreaded) {
        if (!models.containsKey(name.toLowerCase())) {
            ModelTexture texture = new ModelTexture(Requester.requestTexture(name + "_diffuse"));
            MeshRequest mesh = null;

            if (FileHelper.newGameFile("res", name + "_normal.png").exists()) {
                if (singleThreaded) {
                    ModelData data = OBJFileLoader.loadOBJModel(name);

                    mesh = Requester.alreadyLoadedMesh(Loader.getInstance().loadToVAO(data.getVertices(), data.getTextureCoords(), data.getNormals(), data.getTangents(), data.getIndices()));
                    texture.setNormalMap(Requester.alreadyLoadedTexture(Loader.getInstance().loadTexture(name + "_normal")));
                } else {
                    mesh = Requester.requestNormalMesh(name);
                    texture.setNormalMap(Requester.requestTexture(name + "_normal"));
                }
            } else {
                if (singleThreaded) {
                    ModelData data = OBJFileLoader.loadOBJModel(name);
                    mesh = Requester.alreadyLoadedMesh(Loader.getInstance().loadToVAO(data.getVertices(), data.getTextureCoords(), data.getNormals(), data.getIndices()));
                } else {
                    mesh = Requester.requestMesh(name);
                }
            }

            if (FileHelper.newGameFile("res", name + "_specular.png").exists()) {
                if (singleThreaded)
                    texture.setSpecularMap(Requester.alreadyLoadedTexture(Loader.getInstance().loadTexture(name + "_specular")));
                else
                    texture.setSpecularMap(Requester.requestTexture(name + "_specular"));
            }

            if (FileHelper.newGameFile("res", name + "_metallic.png").exists()) {
                if (singleThreaded)
                    texture.setMetallicMap(Requester.alreadyLoadedTexture(Loader.getInstance().loadTexture(name + "_metallic")));
                else
                    texture.setMetallicMap(Requester.requestTexture(name + "_metallic"));
            }

            if (FileHelper.newGameFile("res", name + "_roughness.png").exists()) {
                if (singleThreaded)
                    texture.setRoughnessMap(Requester.alreadyLoadedTexture(Loader.getInstance().loadTexture(name + "_roughness")));
                else
                    texture.setRoughnessMap(Requester.requestTexture(name + "_roughness"));
            }

            if (FileHelper.newGameFile("res", name + "_ao.png").exists()) {
                if (singleThreaded)
                    texture.setAmbientOcclusionMap(Requester.alreadyLoadedTexture(Loader.getInstance().loadTexture(name + "_ao")));
                else
                    texture.setAmbientOcclusionMap(Requester.requestTexture(name + "_ao"));
            }

            models.put(name.toLowerCase(), new Model(mesh, texture));
        }

        return models.get(name.toLowerCase());
    }

    public static void removeModel(Model model) {
        models.remove(model.getName());
    }

    public static ModelTexture loadDecalTexture(String name) {
        return loadDecalTexture(name, false);
    }

    public static ModelTexture loadDecalTexture(String name, boolean singleThreaded) {
        if (!textures.containsKey(name.toLowerCase())) {
            ModelTexture texture = new ModelTexture(Requester.requestTexture(name));

            if (FileHelper.newGameFile("res", name + "_normal.png").exists()) {
                if (singleThreaded)
                    texture.setNormalMap(Requester.alreadyLoadedTexture(Loader.getInstance().loadTexture(name + "_normal")));
                else
                    texture.setNormalMap(Requester.requestTexture(name + "_normal"));
            }

            if (FileHelper.newGameFile("res", name + "_specular.png").exists()) {
                if (singleThreaded)
                    texture.setSpecularMap(Requester.alreadyLoadedTexture(Loader.getInstance().loadTexture(name + "_specular")));
                else
                    texture.setSpecularMap(Requester.requestTexture(name + "_specular"));
            }

            if (FileHelper.newGameFile("res", name + "_metallic.png").exists()) {
                if (singleThreaded)
                    texture.setMetallicMap(Requester.alreadyLoadedTexture(Loader.getInstance().loadTexture(name + "_metallic")));
                else
                    texture.setMetallicMap(Requester.requestTexture(name + "_metallic"));
            }

            if (FileHelper.newGameFile("res", name + "_roughness.png").exists()) {
                if (singleThreaded)
                    texture.setRoughnessMap(Requester.alreadyLoadedTexture(Loader.getInstance().loadTexture(name + "_roughness")));
                else
                    texture.setRoughnessMap(Requester.requestTexture(name + "_roughness"));
            }

            if (FileHelper.newGameFile("res", name + "_ao.png").exists()) {
                if (singleThreaded)
                    texture.setAmbientOcclusionMap(Requester.alreadyLoadedTexture(Loader.getInstance().loadTexture(name + "_ao")));
                else
                    texture.setAmbientOcclusionMap(Requester.requestTexture(name + "_ao"));
            }

            textures.put(name.toLowerCase(), texture);
        }

        return textures.get(name.toLowerCase());
    }

}
