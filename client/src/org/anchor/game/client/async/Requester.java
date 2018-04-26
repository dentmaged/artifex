package org.anchor.game.client.async;

import java.io.File;
import java.io.FileInputStream;
import java.util.concurrent.LinkedBlockingQueue;

import javax.imageio.ImageIO;

import org.anchor.client.engine.renderer.Loader;
import org.anchor.client.engine.renderer.types.Mesh;
import org.anchor.client.engine.renderer.types.MeshRequest;
import org.anchor.client.engine.renderer.types.MeshType;
import org.anchor.client.engine.renderer.types.TextureRequest;
import org.anchor.engine.common.TextureType;
import org.anchor.engine.common.utils.FileHelper;
import org.anchor.game.client.async.types.CompletedAnimatedMesh;
import org.anchor.game.client.async.types.CompletedNormalMesh;
import org.anchor.game.client.async.types.CompletedPlainMesh;
import org.anchor.game.client.async.types.CompletedRequest;
import org.anchor.game.client.async.types.CompletedTexture;
import org.anchor.game.client.loaders.dae.AnimatedModelLoader;
import org.anchor.game.client.loaders.obj.ModelData;
import org.anchor.game.client.loaders.obj.OBJFileLoader;
import org.anchor.game.client.loaders.obj.normals.NormalMappedOBJLoader;

public class Requester {

    public static int MAX_ITERATIONS = 2;

    private static LinkedBlockingQueue<MeshRequest> meshRequests = new LinkedBlockingQueue<MeshRequest>();
    private static LinkedBlockingQueue<TextureRequest> textureRequests = new LinkedBlockingQueue<TextureRequest>();

    private static LinkedBlockingQueue<CompletedRequest> toOpenGL = new LinkedBlockingQueue<CompletedRequest>();

    private static volatile boolean running;

    public static void start() {
        running = true;
        new Thread(new Runnable() {

            @Override
            public void run() {
                while (running) {
                    MeshRequest meshRequest = meshRequests.poll();
                    TextureRequest textureRequest = textureRequests.poll();

                    if (meshRequest != null) {
                        if (meshRequest.getType() == MeshType.PLAIN)
                            toOpenGL.offer(new CompletedPlainMesh(meshRequest, OBJFileLoader.loadOBJModel(meshRequest.getName())));
                        else if (meshRequest.getType() == MeshType.NORMAL)
                            toOpenGL.offer(new CompletedNormalMesh(meshRequest, OBJFileLoader.loadOBJModel(meshRequest.getName())));
                        else
                            toOpenGL.offer(new CompletedAnimatedMesh(meshRequest, AnimatedModelLoader.loadMesh(meshRequest.getName())));
                    }

                    if (textureRequest != null) {
                        try {
                            File file = FileHelper.newGameFile(Loader.RES_LOC, textureRequest.getName().replace(Loader.RES_LOC + "/", "").replace(Loader.RES_LOC, "") + ".png");
                            if (!file.exists()) {
                                System.err.println(file.getName() + " does not exist! Falling back on default texture.");
                                file = FileHelper.newGameFile(Loader.RES_LOC, "missing_texture.png");
                            }

                            toOpenGL.offer(new CompletedTexture(textureRequest, ImageIO.read(new FileInputStream(file))));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

        }).start();
    }

    public static MeshRequest forceLoadPlainMesh(String name) {
        MeshRequest request = new MeshRequest(name);
        request.setMesh(OBJFileLoader.loadOBJ(name));

        return request;
    }

    public static MeshRequest forceLoadNormalMesh(String name) {
        MeshRequest request = new MeshRequest(name);
        request.setMesh(NormalMappedOBJLoader.loadOBJ(name));

        return request;
    }

    public static TextureRequest forceLoadTexture(TextureType type, String name) {
        return forceLoadTexture(type.withFile(name));
    }

    public static TextureRequest forceLoadTexture(String name) {
        TextureRequest request = new TextureRequest(name);
        request.setTexture(Loader.getInstance().loadTexture(name));

        return request;
    }

    public static void perform() {
        if (!running)
            start();

        CompletedRequest load;
        for (int i = 0; i < MAX_ITERATIONS && (load = toOpenGL.poll()) != null; i++)
            load.load();
    }

    public static TextureRequest alreadyLoadedTexture(int texture) {
        TextureRequest request = new TextureRequest("");
        request.setTexture(texture);

        return request;
    }

    public static MeshRequest alreadyLoadedMesh(Mesh mesh) {
        MeshRequest request = new MeshRequest("");
        request.setMesh(mesh);

        return request;
    }

    public static MeshRequest alreadyLoadedMesh(ModelData data) {
        MeshRequest request = new MeshRequest("");
        toOpenGL.offer(new CompletedPlainMesh(request, data));

        return request;
    }

    public static MeshRequest requestMesh(String mesh) {
        MeshRequest request = new MeshRequest(mesh, MeshType.PLAIN);
        meshRequests.offer(request);

        return request;
    }

    public static MeshRequest requestNormalMesh(String mesh) {
        MeshRequest request = new MeshRequest(mesh, MeshType.NORMAL);
        meshRequests.offer(request);

        return request;
    }

    public static MeshRequest requestAnimatedMesh(String mesh) {
        MeshRequest request = new MeshRequest(mesh, MeshType.ANIMATION);
        meshRequests.offer(request);

        return request;
    }

    public static TextureRequest requestTexture(TextureType type, String texture) {
        return requestTexture(type.withFile(texture));
    }

    public static TextureRequest requestTexture(String texture) {
        TextureRequest request = new TextureRequest(texture);
        textureRequests.offer(request);

        return request;
    }

    public static void shutdown() {
        running = false;
    }

}