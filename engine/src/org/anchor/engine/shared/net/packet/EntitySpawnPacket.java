package org.anchor.engine.shared.net.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.lang.reflect.Field;
import java.util.Map.Entry;

import org.anchor.engine.common.net.packet.IPacket;
import org.anchor.engine.shared.components.IComponent;
import org.anchor.engine.shared.components.TransformComponent;
import org.anchor.engine.shared.entity.Entity;
import org.anchor.engine.shared.net.CorePacketManager;
import org.anchor.engine.shared.utils.Property;
import org.anchor.engine.shared.utils.RawParser;

public class EntitySpawnPacket implements IPacket {

    public Entity entity;

    public EntitySpawnPacket() {

    }

    public EntitySpawnPacket(Entity entity) {
        this.entity = entity;
    }

    public int getId() {
        return CorePacketManager.ENTITY_SPAWN_PACKET;
    }

    @Override
    public void write(DataOutputStream stream) throws Exception {
        stream.writeInt(entity.getId());

        stream.writeInt(entity.entrySet().size());
        for (Entry<String, String> entry : entity.entrySet()) {
            stream.writeUTF(entry.getKey());
            stream.writeUTF(entry.getValue());
        }

        stream.writeInt(entity.getComponents().size());
        for (IComponent component : entity.getComponents()) {
            Class<? extends IComponent> clazz = component.getClass();
            stream.writeUTF(clazz.getCanonicalName());

            for (Field field : clazz.getFields()) {
                Property property = field.getAnnotation(Property.class);
                if (property != null)
                    RawParser.getInstance().write(stream, field.get(component));
            }
        }
    }

    @Override
    public void read(DataInputStream stream) throws Exception {
        entity = new Entity();
        entity.removeComponent(TransformComponent.class);
        entity.setId(stream.readInt());

        int count = stream.readInt();
        for (int i = 0; i < count; i++)
            entity.setValue(stream.readUTF(), stream.readUTF());

        count = stream.readInt();
        for (int i = 0; i < count; i++) {
            Class<? extends IComponent> clazz = (Class<? extends IComponent>) Class.forName(stream.readUTF());
            IComponent component = clazz.newInstance();
            entity.addComponent(component);

            for (Field field : clazz.getFields()) {
                Property property = field.getAnnotation(Property.class);
                if (property != null)
                    field.set(component, RawParser.getInstance().read(stream, field.getType()));
            }
        }
    }

}
