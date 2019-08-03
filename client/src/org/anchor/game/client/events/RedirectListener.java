package org.anchor.game.client.events;

import org.anchor.engine.common.events.Listener;
import org.anchor.engine.common.events.handler.EventHandler;
import org.anchor.engine.shared.entity.Entity;
import org.anchor.engine.shared.entity.IComponent;
import org.anchor.engine.shared.events.component.AddComponentEvent;
import org.anchor.engine.shared.events.entity.EntityCreateEvent;
import org.anchor.engine.shared.net.Redirect;

public class RedirectListener implements Listener {

    @EventHandler
    public void onEntityCreate(EntityCreateEvent event) {
        Entity entity = event.getEntity();

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

    @EventHandler
    public void onComponentAdd(AddComponentEvent event) {
        Entity entity = event.getEntity();
        IComponent component = event.getComponent();

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
