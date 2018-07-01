package org.anchor.game.server.components;

import org.anchor.engine.common.net.server.ServerThread;
import org.anchor.engine.shared.components.IComponent;
import org.anchor.engine.shared.entity.Entity;

public class ServerThreadComponent implements IComponent {

    public ServerThread net;

    @Override
    public void spawn(Entity entity) {

    }

    @Override
    public void setValue(String key, String value) {

    }

    @Override
    public IComponent copy() {
        return null;
    }

}
