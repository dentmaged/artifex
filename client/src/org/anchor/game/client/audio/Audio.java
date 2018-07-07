package org.anchor.game.client.audio;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.anchor.engine.common.utils.FileHelper;
import org.lwjgl.LWJGLException;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.util.vector.Vector3f;

public class Audio {

    private static String RES_LOC = "res";
    private static List<Integer> buffers = new ArrayList<Integer>();

    public static void init() {
        try {
            AL.create();
        } catch (LWJGLException e) {
            e.printStackTrace();
        }
    }

    public static int load(String path) {
        return load(FileHelper.newGameFile(RES_LOC, path + ".wav"));
    }

    public static int load(File file) {
        int buffer = AL10.alGenBuffers();
        buffers.add(buffer);

        WaveData data = WaveData.create(file);
        AL10.alBufferData(buffer, data.getFormat(), data.getData(), data.getSampleRate());
        data.dispose();

        return buffer;
    }

    public static void setListenerData(Vector3f position, Vector3f velocity) {
        AL10.alListener3f(AL10.AL_POSITION, position.x, position.y, position.z);
        AL10.alListener3f(AL10.AL_VELOCITY, velocity.x, velocity.y, velocity.z);
    }

    public static void shutdown() {
        for (int buffer : buffers)
            AL10.alDeleteBuffers(buffer);

        AL.destroy();
    }

}
