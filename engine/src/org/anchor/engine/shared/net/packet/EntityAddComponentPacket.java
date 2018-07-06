package org.anchor.engine.shared.net.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.lang.reflect.Field;

import org.anchor.engine.common.net.packet.IPacket;
import org.anchor.engine.shared.components.IComponent;
import org.anchor.engine.shared.net.CorePacketManager;
import org.anchor.engine.shared.utils.Property;
import org.anchor.engine.shared.utils.RawParser;

public class EntityAddComponentPacket implements IPacket {

    public int id;
    public IComponent component;

    public EntityAddComponentPacket() {

    }

    public EntityAddComponentPacket(int id, IComponent component) {
        this.id = id;
        this.component = component;
    }

    public int getId() {
        return CorePacketManager.ENTITY_ADD_COMPONENT_PACKET;
    }

    @Override
    public void write(DataOutputStream stream) throws Exception {
        stream.writeInt(id);

        Class<? extends IComponent> clazz = component.getClass();
        stream.writeUTF(clazz.getCanonicalName());

        for (Field field : clazz.getFields()) {
            Property property = field.getAnnotation(Property.class);
            if (property != null)
                RawParser.getInstance().write(stream, field.get(component));
        }
    }

    @Override
    public void read(DataInputStream stream) throws Exception {
        id = stream.readInt();

        Class<? extends IComponent> clazz = (Class<? extends IComponent>) Class.forName(stream.readUTF());
        component = clazz.newInstance();

        for (Field field : clazz.getFields()) {
            Property property = field.getAnnotation(Property.class);
            if (property != null)
                field.set(component, RawParser.getInstance().read(stream, field.getType()));
        }
    }

}
