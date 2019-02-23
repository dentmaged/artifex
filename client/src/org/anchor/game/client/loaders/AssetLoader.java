package org.anchor.game.client.loaders;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.anchor.client.engine.renderer.Loader;
import org.anchor.client.engine.renderer.types.Material;
import org.anchor.client.engine.renderer.types.Model;
import org.anchor.engine.common.utils.FileHelper;
import org.anchor.game.client.async.Requester;
import org.anchor.game.client.particles.ParticleTexture;

public class AssetLoader {

    private static Map<String, Model> models = new HashMap<String, Model>();
    private static Map<String, Material> materials = new HashMap<String, Material>();
    private static Map<String, ParticleTexture> particles = new HashMap<String, ParticleTexture>();

    public static Model loadModel(String name) {
        return loadModel(name, false);
    }

    public static Model loadModel(String name, boolean singleThreaded) {
        if (!models.containsKey(name.toLowerCase()))
            models.put(name.toLowerCase(), new Model(Requester.requestMesh(name)));

        return models.get(name.toLowerCase());
    }

    public static void removeModel(Model model) {
        models.remove(model.getName());
    }

    public static void reloadModels() {
        for (Entry<String, Model> entry : models.entrySet())
            entry.getValue().setMesh(Requester.requestMesh(entry.getKey()));
    }

    public static Material loadMaterial(String name) {
        if (name == null)
            return loadAEM("default");

        if (!materials.containsKey(name.toLowerCase()))
            materials.put(name.toLowerCase(), loadAEM(name));

        return materials.get(name.toLowerCase());
    }

    private static Material loadAEM(String name) {
        Material material = new Material();
        Map<String, String> values = loadValues(FileHelper.newGameFile(Loader.RES_LOC, name + ".aem"));

        if (values.containsKey("albedo") && FileHelper.newGameFile("res", values.get("albedo") + ".png").exists())
            material.setAlbedo(Requester.requestTexture(values.get("albedo")));

        if (values.containsKey("normal") && FileHelper.newGameFile("res", values.get("normal") + ".png").exists())
            material.setNormalMap(Requester.requestTexture(values.get("normal")));

        if (values.containsKey("specular") && FileHelper.newGameFile("res", values.get("specular") + ".png").exists())
            material.setSpecularMap(Requester.requestTexture(values.get("specular")));

        if (values.containsKey("metallic") && FileHelper.newGameFile("res", values.get("metallic") + ".png").exists())
            material.setMetallicMap(Requester.requestTexture(values.get("metallic")));

        if (values.containsKey("roughness") && FileHelper.newGameFile("res", values.get("roughness") + ".png").exists())
            material.setRoughnessMap(Requester.requestTexture(values.get("roughness")));

        if (values.containsKey("ao") && FileHelper.newGameFile("res", values.get("ao") + ".png").exists())
            material.setAmbientOcclusionMap(Requester.requestTexture(values.get("ao")));

        if (values.containsKey("backface"))
            material.setCullingEnabled(Boolean.parseBoolean(values.get("backface")));

        if (values.containsKey("blending"))
            material.setCullingEnabled(Boolean.parseBoolean(values.get("blending")));

        return material;
    }

    public static void reloadMaterials() {
        for (Entry<String, Material> entry : materials.entrySet())
            materials.put(entry.getKey(), loadAEM(entry.getKey()));
    }

    public static ParticleTexture loadParticle(String name) {
        if (!particles.containsKey(name.toLowerCase()))
            particles.put(name.toLowerCase(), loadAEP(name));

        return particles.get(name.toLowerCase());
    }

    private static ParticleTexture loadAEP(String name) {
        Map<String, String> values = loadValues(FileHelper.newGameFile(Loader.RES_LOC, name + ".aep"));

        int rows = 1;
        if (values.containsKey("rows"))
            rows = Integer.parseInt(values.get("rows"));

        boolean additive = false;
        if (values.containsKey("additive"))
            additive = Boolean.parseBoolean(values.get("additive"));

        return new ParticleTexture(Requester.requestTexture(values.get("texture")), rows, additive);
    }

    public static void reloadParticles() {
        for (Entry<String, ParticleTexture> entry : particles.entrySet())
            particles.put(entry.getKey(), loadAEP(entry.getKey()));
    }

    private static Map<String, String> loadValues(File file) {
        Map<String, String> values = new HashMap<String, String>();
        String contents = FileHelper.read(file);
        String[] lines = contents.split("\n");

        for (int i = 0; i < lines.length; i++) {
            if (lines[i].indexOf('=') == -1)
                continue;

            String[] parts = lines[i].split("=");
            values.put(parts[0], parts[1]);
        }

        return values;
    }

}
