package org.anchor.engine.common.net.packet;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class PacketManager {

    private static Map<Integer, Class<? extends IPacket>> packets = new HashMap<Integer, Class<? extends IPacket>>();

    public static <T extends IPacket> T getPacketByID(int id) {
        for (Entry<Integer, Class<? extends IPacket>> entry : packets.entrySet()) {
            if (entry.getKey() == id) {
                try {
                    return (T) entry.getValue().newInstance();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }

    public static void clearPackets() {
        packets.clear();
    }

    public static void registerPacket(int id, Class<? extends IPacket> packet) {
        packets.put(id, packet);
    }

}
