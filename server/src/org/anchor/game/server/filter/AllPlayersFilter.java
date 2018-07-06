package org.anchor.game.server.filter;

import org.anchor.game.server.GameServer;

public class AllPlayersFilter extends Filter {

    public AllPlayersFilter() {
        targets.addAll(GameServer.getServer().getPlayers());
    }

}
