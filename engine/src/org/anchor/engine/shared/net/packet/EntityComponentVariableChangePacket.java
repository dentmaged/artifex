package org.anchor.engine.shared.net.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.lang.reflect.Field;

import org.anchor.engine.common.net.packet.IPacket;
import org.anchor.engine.shared.components.IComponent;
import org.anchor.engine.shared.entity.Entity;
import org.anchor.engine.shared.net.CorePacketManager;
import org.anchor.engine.shared.utils.Property;
import org.anchor.engine.shared.utils.RawParser;

public class EntityComponentVariableChangePacket implements IPacket {

    private IComponent component;

    public int id;
    public Class<? extends IComponent> clazz;
    public Field field;
    public Object value;

    public EntityComponentVariableChangePacket() {

    }

    public EntityComponentVariableChangePacket(Entity entity, IComponent component, Field field) {
        this.id = entity.getId();
        this.component = component;
        this.field = field;
    }

    public int getId() {
        return CorePacketManager.ENTITY_COMPONENT_VARIABLE_CHANGE_PACKET;
    }

    @Override
    public void write(DataOutputStream stream) throws Exception {
        stream.writeInt(id);
        stream.writeUTF(component.getClass().getCanonicalName());
        stream.writeUTF(field.getAnnotation(Property.class).value());

        RawParser.getInstance().write(stream, field.get(component));
    }

    @Override
    public void read(DataInputStream stream) throws Exception {
        id = stream.readInt();
        clazz = (Class<? extends IComponent>) Class.forName(stream.readUTF());

        String name = stream.readUTF();
        for (Field field : clazz.getFields()) {
            Property property = field.getAnnotation(Property.class);
            if (property != null && property.value().equals(name)) {
                this.field = field;
                break;
            }
        }

        value = RawParser.getInstance().read(stream, field.getType());
    }

}
