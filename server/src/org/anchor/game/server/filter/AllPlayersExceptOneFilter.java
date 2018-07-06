package org.anchor.game.server.filter;

import org.anchor.engine.shared.entity.Entity;

public class AllPlayersExceptOneFilter extends AllPlayersFilter {

    public AllPlayersExceptOneFilter(Entity player) {
        super();
        targets.remove(player);
    }

}
