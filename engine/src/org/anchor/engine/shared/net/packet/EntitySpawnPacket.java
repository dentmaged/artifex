package org.anchor.engine.shared.net.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.lang.reflect.Field;
import java.util.Map.Entry;

import org.anchor.engine.common.Log;
import org.anchor.engine.common.net.packet.IPacket;
import org.anchor.engine.shared.components.TransformComponent;
import org.anchor.engine.shared.entity.Entity;
import org.anchor.engine.shared.entity.IComponent;
import org.anchor.engine.shared.net.CorePacketManager;
import org.anchor.engine.shared.net.Redirect;
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

        int count = 0;
        for (IComponent component : entity.getComponents())
            if (!component.getClass().getName().startsWith("org.anchor.game.server"))
                count++;

        stream.writeInt(count);
        for (IComponent component : entity.getComponents()) {
            Class<? extends IComponent> clazz = component.getClass();
            if (clazz.getName().startsWith("org.anchor.game.server"))
                continue;

            Redirect redirect = clazz.getAnnotation(Redirect.class);
            if (redirect == null) {
                stream.writeUTF(clazz.getCanonicalName());
                stream.writeBoolean(true);

                for (Field field : clazz.getFields()) {
                    Property property = field.getAnnotation(Property.class);
                    if (property != null)
                        RawParser.getInstance().write(stream, field.get(component));
                }
            } else {
                stream.writeUTF(redirect.value());
                stream.writeBoolean(false);
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
            String className = stream.readUTF();
            if (className.length() == 0) {
                Log.warning("Invalid component name (zero length)!");
                continue;
            }

            Log.debug(className);
            Class<? extends IComponent> clazz = null;

            try {
                clazz = (Class<? extends IComponent>) Class.forName(className);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                Log.error("Component " + className + " not found!");
            }

            IComponent component = clazz.newInstance();
            entity.addComponent(component);

            if (stream.readBoolean()) {
                for (Field field : clazz.getFields()) {
                    Property property = field.getAnnotation(Property.class);
                    if (property != null)
                        field.set(component, RawParser.getInstance().read(stream, field.getType()));
                }
            }
        }
        entity.spawn();
    }

}
