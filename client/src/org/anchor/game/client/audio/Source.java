package org.anchor.game.client.audio;

import org.lwjgl.openal.AL10;
import org.lwjgl.util.vector.Vector3f;

public class Source {

    protected int id;

    public Source() {
        id = AL10.alGenSources();

        setVolume(1);
        setPitch(1);
        setPosition(new Vector3f());
        setVelocity(new Vector3f());
    }

    public void play(int buffer) {
        stop();

        AL10.alSourcei(id, AL10.AL_BUFFER, buffer);
        AL10.alSourcePlay(id);
    }

    public void pause() {
        AL10.alSourcePause(id);
    }

    public void resume() {
        AL10.alSourcePlay(id);
    }

    public void stop() {
        AL10.alSourceStop(id);
    }

    public void delete() {
        stop();

        AL10.alDeleteSources(id);
    }

    public int getId() {
        return id;
    }

    public void setVolume(float volume) {
        AL10.alSourcef(id, AL10.AL_GAIN, volume);
    }

    public void setPitch(float pitch) {
        AL10.alSourcef(id, AL10.AL_PITCH, pitch);
    }

    public void setPosition(Vector3f position) {
        setPosition(position.x, position.y, position.z);
    }

    public void setPosition(float x, float y, float z) {
        AL10.alSource3f(id, AL10.AL_POSITION, x, y, z);
    }

    public void setVelocity(Vector3f velocity) {
        setVelocity(velocity.x, velocity.y, velocity.z);
    }

    public void setVelocity(float x, float y, float z) {
        AL10.alSource3f(id, AL10.AL_VELOCITY, x, y, z);
    }

    public void setLoop(boolean loop) {
        AL10.alSourcei(id, AL10.AL_LOOPING, loop ? AL10.AL_TRUE : AL10.AL_FALSE);
    }

    public boolean isPlaying() {
        return AL10.alGetSourcei(id, AL10.AL_SOURCE_STATE) == AL10.AL_PLAYING;
    }

}
