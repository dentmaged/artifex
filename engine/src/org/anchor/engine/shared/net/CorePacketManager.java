package org.anchor.engine.shared.net;

import org.anchor.engine.common.net.packet.PacketManager;
import org.anchor.engine.shared.net.packet.AuthenticationPacket;
import org.anchor.engine.shared.net.packet.EntityAddComponentPacket;
import org.anchor.engine.shared.net.packet.EntityComponentVariableChangePacket;
import org.anchor.engine.shared.net.packet.EntityDestroyPacket;
import org.anchor.engine.shared.net.packet.EntityKeyValuePacket;
import org.anchor.engine.shared.net.packet.EntityLinkPacket;
import org.anchor.engine.shared.net.packet.EntityRemoveComponentPacket;
import org.anchor.engine.shared.net.packet.EntitySpawnPacket;
import org.anchor.engine.shared.net.packet.GameVariablePacket;
import org.anchor.engine.shared.net.packet.LevelChangePacket;
import org.anchor.engine.shared.net.packet.PlayerMovementPacket;
import org.anchor.engine.shared.net.packet.PlayerPositionPacket;
import org.anchor.engine.shared.net.packet.RunCommandPacket;
import org.anchor.engine.shared.net.packet.SendMessagePacket;

public class CorePacketManager {

    public static int AUTHENTICATION_PACKET = 0; // To Server
    public static int LEVEL_CHANGE_PACKET = 1; // To Client

    public static int PLAYER_MOVEMENT_PACKET = 2; // To Server
    public static int PLAYER_POSITION_PACKET = 3; // To Client

    public static int ENTITY_SPAWN_PACKET = 4; // To Client
    public static int ENTITY_ADD_COMPONENT_PACKET = 5; // To Client
    public static int ENTITY_COMPONENT_VARIABLE_CHANGE_PACKET = 6; // To Client
    public static int ENTITY_REMOVE_COMPONENT_PACKET = 7; // To Client
    public static int ENTITY_KEY_VALUE_PACKET = 8; // To Client
    public static int ENTITY_DESTROY_PACKET = 9; // To Client
    public static int ENTITY_LINK_PACKET = 10; // To Client

    public static int GAME_VARIABLE_PACKET = 11; // Both
    public static int RUN_COMMAND_PACKET = 12; // Both
    public static int SEND_MESSAGE_PACKET = 13; // To Client

    public static void register() {
        PacketManager.clearPackets();
        PacketManager.registerPacket(AUTHENTICATION_PACKET, AuthenticationPacket.class);
        PacketManager.registerPacket(LEVEL_CHANGE_PACKET, LevelChangePacket.class);

        PacketManager.registerPacket(PLAYER_MOVEMENT_PACKET, PlayerMovementPacket.class);
        PacketManager.registerPacket(PLAYER_POSITION_PACKET, PlayerPositionPacket.class);

        PacketManager.registerPacket(ENTITY_SPAWN_PACKET, EntitySpawnPacket.class);
        PacketManager.registerPacket(ENTITY_ADD_COMPONENT_PACKET, EntityAddComponentPacket.class);
        PacketManager.registerPacket(ENTITY_COMPONENT_VARIABLE_CHANGE_PACKET, EntityComponentVariableChangePacket.class);
        PacketManager.registerPacket(ENTITY_REMOVE_COMPONENT_PACKET, EntityRemoveComponentPacket.class);
        PacketManager.registerPacket(ENTITY_KEY_VALUE_PACKET, EntityKeyValuePacket.class);
        PacketManager.registerPacket(ENTITY_DESTROY_PACKET, EntityDestroyPacket.class);
        PacketManager.registerPacket(ENTITY_LINK_PACKET, EntityLinkPacket.class);

        PacketManager.registerPacket(GAME_VARIABLE_PACKET, GameVariablePacket.class);
        PacketManager.registerPacket(RUN_COMMAND_PACKET, RunCommandPacket.class);
        PacketManager.registerPacket(SEND_MESSAGE_PACKET, SendMessagePacket.class);
    }

}
