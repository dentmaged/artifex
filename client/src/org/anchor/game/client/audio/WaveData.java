package org.anchor.game.client.audio;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.anchor.engine.common.Log;
import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL10;

public class WaveData {

    private AudioInputStream audioStream;
    private int format;
    private int sampleRate;
    private int totalBytes;
    private int bytesPerFrame;
    private ByteBuffer data;
    private byte[] dataArray;

    private WaveData(AudioInputStream stream) {
        this.audioStream = stream;

        AudioFormat audioFormat = stream.getFormat();
        format = getOpenAlFormat(audioFormat.getChannels(), audioFormat.getSampleSizeInBits());
        this.sampleRate = (int) audioFormat.getSampleRate();
        this.bytesPerFrame = audioFormat.getFrameSize();
        this.totalBytes = (int) (stream.getFrameLength() * bytesPerFrame);
        this.data = BufferUtils.createByteBuffer(totalBytes);
        this.dataArray = new byte[totalBytes];

        loadData();
    }

    protected void dispose() {
        try {
            audioStream.close();
            data.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ByteBuffer loadData() {
        try {
            int bytesRead = audioStream.read(dataArray, 0, totalBytes);
            data.clear();
            data.put(dataArray, 0, bytesRead);
            data.flip();
        } catch (IOException e) {
            e.printStackTrace();
            Log.warning("Couldn't read bytes from audio stream!");
        }

        return data;
    }

    public static WaveData create(File file) {
        InputStream stream = null;
        try {
            stream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        InputStream bufferedInput = new BufferedInputStream(stream);
        AudioInputStream audioStream = null;
        try {
            audioStream = AudioSystem.getAudioInputStream(bufferedInput);
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new WaveData(audioStream);
    }

    private static int getOpenAlFormat(int channels, int bitsPerSample) {
        if (channels == 1)
            return bitsPerSample == 8 ? AL10.AL_FORMAT_MONO8 : AL10.AL_FORMAT_MONO16;

        return bitsPerSample == 8 ? AL10.AL_FORMAT_STEREO8 : AL10.AL_FORMAT_STEREO16;
    }

    public AudioInputStream getAudioStream() {
        return audioStream;
    }

    public byte[] getDataArray() {
        return dataArray;
    }

    public int getFormat() {
        return format;
    }

    public int getSampleRate() {
        return sampleRate;
    }

    public int getTotalBytes() {
        return totalBytes;
    }

    public int getBytesPerFrame() {
        return bytesPerFrame;
    }

    public ByteBuffer getData() {
        return data;
    }

}
