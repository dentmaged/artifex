package org.anchor.game.client;

import org.anchor.engine.common.net.packet.IPacket;
import org.anchor.engine.shared.Engine;
import org.anchor.engine.shared.components.IComponent;
import org.anchor.engine.shared.entity.Entity;
import org.anchor.engine.shared.net.Redirect;

public class ClientEngine extends Engine {

    @Override
    public void broadcast(IPacket packet) {
        GameClient.getClient().sendPacket(packet);
    }

    @Override
    public void onEntityCreate(Entity entity) {
        try {
            for (IComponent component : entity.getComponents()) {
                Redirect redirect = component.getClass().getAnnotation(Redirect.class);
                if (redirect != null) {
                    entity.removeComponent(component);
                    entity.addComponent(((Class<? extends IComponent>) Class.forName(redirect.value())).newInstance());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onComponentAdd(Entity entity, IComponent component) {
        try {
            Redirect redirect = component.getClass().getAnnotation(Redirect.class);
            if (redirect != null) {
                entity.removeComponent(component);
                entity.addComponent(((Class<? extends IComponent>) Class.forName(redirect.value())).newInstance());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
