package org.anchor.game.client.components;

import org.anchor.engine.shared.components.IComponent;
import org.anchor.engine.shared.entity.Entity;
import org.anchor.engine.shared.utils.Property;
import org.anchor.game.client.audio.Audio;
import org.anchor.game.client.audio.Source;
import org.lwjgl.util.vector.Vector3f;

public class SoundComponent implements IComponent {

    public Source source;
    public int sound = -1;

    @Property("Relative Position")
    public Vector3f relativePosition = new Vector3f();

    private Entity entity;

    @Override
    public void spawn(Entity entity) {
        this.entity = entity;
        source = new Source();

        if (sound == -1 && entity.containsKey("sound"))
            sound = Audio.load(entity.getValue("sound"));
    }

    @Override
    public void setValue(String key, String value) {
        if (key.equals("loop"))
            source.setLoop(Boolean.parseBoolean(value));

        if (key.equals("pitch"))
            source.setPitch(Float.parseFloat(value));

        if (key.equals("volume"))
            source.setVolume(Float.parseFloat(value));
    }

    public void update() {
        source.setPosition(Vector3f.add(entity.getPosition(), relativePosition, null));
        source.setVelocity(entity.getVelocity());
    }

    @Override
    public IComponent copy() {
        SoundComponent copy = new SoundComponent();
        copy.sound = sound;

        return copy;
    }

}
