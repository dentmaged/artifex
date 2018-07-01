package org.anchor.engine.shared.net;

import org.anchor.engine.common.net.packet.PacketManager;
import org.anchor.engine.shared.net.packet.EntitySpawnPacket;
import org.anchor.engine.shared.net.packet.LevelChangePacket;
import org.anchor.engine.shared.net.packet.PlayerMovementPacket;
import org.anchor.engine.shared.net.packet.PlayerPositionPacket;

public class CorePacketManager {

    public static int LEVEL_CHANGE_PACKET = 1;
    public static int PLAYER_MOVEMENT_PACKET = 2;
    public static int PLAYER_POSITION_PACKET = 3;
    public static int ENTITY_SPAWN_PACKET = 4;

    public static void register() {
        PacketManager.clearPackets();
        PacketManager.registerPacket(LEVEL_CHANGE_PACKET, LevelChangePacket.class);
        PacketManager.registerPacket(PLAYER_MOVEMENT_PACKET, PlayerMovementPacket.class);
        PacketManager.registerPacket(PLAYER_POSITION_PACKET, PlayerPositionPacket.class);
        PacketManager.registerPacket(ENTITY_SPAWN_PACKET, EntitySpawnPacket.class);
    }

}
