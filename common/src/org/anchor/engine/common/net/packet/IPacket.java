package org.anchor.engine.common.net.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public interface IPacket {

    public int getId();

    public void write(DataOutputStream stream) throws Exception;

    public void read(DataInputStream stream) throws Exception;

}
