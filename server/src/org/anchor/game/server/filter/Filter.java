package org.anchor.game.server.filter;

import java.util.ArrayList;
import java.util.List;

import org.anchor.engine.common.net.packet.IPacket;
import org.anchor.engine.shared.entity.Entity;
import org.anchor.game.server.components.ServerThreadComponent;

public class Filter {

    protected List<Entity> targets = new ArrayList<Entity>();

    public void sendPacket(IPacket packet) {
        for (Entity player : targets)
            player.getComponent(ServerThreadComponent.class).net.sendPacket(packet);
    }

}
