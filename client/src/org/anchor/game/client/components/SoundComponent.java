package org.anchor.game.client.components;

import org.anchor.engine.shared.components.IComponent;
import org.anchor.engine.shared.entity.Entity;
import org.anchor.engine.shared.utils.Property;
import org.anchor.game.client.audio.Source;
import org.anchor.game.client.utils.Sound;
import org.lwjgl.util.vector.Vector3f;

public class SoundComponent implements IComponent {

    public Source source;

    @Property("Sound")
    public Sound sound;

    @Property("Relative Position")
    public Vector3f relativePosition = new Vector3f();

    @Property("Loop")
    public boolean loop;

    @Property("Pitch")
    public float pitch = 1;

    @Property("Volume")
    public float volume = 1;

    private Entity entity;

    @Override
    public void spawn(Entity entity) {
        this.entity = entity;
        source = new Source();

        if (sound == null && entity.containsKey("sound"))
            sound = new Sound(entity.getValue("sound"));
    }

    @Override
    public void setValue(String key, String value) {
        if (key.equals("loop"))
            loop = Boolean.parseBoolean(value);

        if (key.equals("pitch"))
            pitch = Float.parseFloat(value);

        if (key.equals("volume"))
            volume = Float.parseFloat(value);
    }

    @Override
    public void updateFixed() {
        source.setPosition(entity.getPosition().x + relativePosition.x, entity.getPosition().y + relativePosition.y, entity.getPosition().z + relativePosition.z);
        source.setVelocity(entity.getVelocity());

        source.setLoop(loop);
        source.setPitch(pitch);
        source.setVolume(volume);
    }

    @Property("Play Sound")
    public void play() {
        if (sound != null)
            source.play(sound.getSound());
    }

    @Override
    public IComponent copy() {
        SoundComponent copy = new SoundComponent();
        copy.sound = sound;
        copy.relativePosition = new Vector3f(relativePosition);

        return copy;
    }

}
