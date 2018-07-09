package org.anchor.engine.shared.net.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import org.anchor.engine.common.net.packet.IPacket;
import org.anchor.engine.shared.net.CorePacketManager;

public class PlayerMovementPacket implements IPacket {

    public boolean forwards, left, backwards, right, jump, walk, interact, fire, reload;
    public int selectedWeapon;
    public float pitch, yaw;

    public PlayerMovementPacket() {

    }

    public PlayerMovementPacket(boolean forwards, boolean left, boolean backwards, boolean right, boolean jump, boolean walk, boolean interact, boolean fire, boolean reload, int selectedWeapon, float pitch, float yaw) {
        this.forwards = forwards;
        this.left = left;
        this.backwards = backwards;
        this.right = right;
        this.jump = jump;
        this.walk = walk;
        this.interact = interact;

        this.fire = fire;
        this.reload = reload;
        this.selectedWeapon = selectedWeapon;

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
        stream.writeBoolean(interact);

        stream.writeBoolean(fire);
        stream.writeBoolean(reload);
        stream.writeInt(selectedWeapon);

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
        interact = stream.readBoolean();

        fire = stream.readBoolean();
        reload = stream.readBoolean();
        selectedWeapon = stream.readInt();

        pitch = stream.readFloat();
        yaw = stream.readFloat();
    }

}
