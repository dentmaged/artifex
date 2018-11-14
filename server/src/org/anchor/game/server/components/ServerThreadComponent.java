package org.anchor.game.server.components;

import org.anchor.engine.common.net.server.ServerThread;
import org.anchor.engine.shared.entity.Entity;
import org.anchor.engine.shared.entity.IComponent;
import org.anchor.engine.shared.net.IUser;

public class ServerThreadComponent implements IComponent {

    public ServerThread net;
    public IUser user;

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
