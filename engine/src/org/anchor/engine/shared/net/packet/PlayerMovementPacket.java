package org.anchor.engine.shared.net.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import org.anchor.engine.common.net.packet.IPacket;
import org.anchor.engine.shared.net.CorePacketManager;

public class PlayerMovementPacket implements IPacket {

    public boolean forwards, left, backwards, right, jump, walk;
    public float pitch, yaw;

    public PlayerMovementPacket() {

    }

    public PlayerMovementPacket(boolean forwards, boolean left, boolean backwards, boolean right, boolean jump, boolean walk, float pitch, float yaw) {
        this.forwards = forwards;
        this.left = left;
        this.backwards = backwards;
        this.right = right;
        this.jump = jump;
        this.walk = walk;

        this.pitch = pitch;
        this.yaw = yaw;
    }

    public int getId() {
        return CorePacketManager.PLAYER_MOVEMENT_PACKET;
    }

    @Override
    public void write(DataOutputStream stream) throws Exception {
        stream.writeBoolean(forwards);
        stream.writeBoolean(left);
        stream.writeBoolean(backwards);
        stream.writeBoolean(right);
        stream.writeBoolean(jump);
        stream.writeBoolean(walk);

        stream.writeFloat(pitch);
        stream.writeFloat(yaw);
    }

    @Override
    public void read(DataInputStream stream) throws Exception {
        forwards = stream.readBoolean();
        left = stream.readBoolean();
        backwards = stream.readBoolean();
        right = stream.readBoolean();
        jump = stream.readBoolean();
        walk = stream.readBoolean();

        pitch = stream.readFloat();
        yaw = stream.readFloat();
    }

}
