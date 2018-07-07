package org.anchor.game.client.utils;

import org.anchor.game.client.audio.Audio;

public class Sound {

    private String name;
    private int sound;

    public Sound(String name) {
        this.name = name;
        this.sound = Audio.load(name);
    }

    public String getName() {
        return name;
    }

    public int getSound() {
        return sound;
    }

}
