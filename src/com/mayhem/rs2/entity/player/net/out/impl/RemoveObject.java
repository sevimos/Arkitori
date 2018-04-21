package com.mayhem.rs2.entity.player.net.out.impl;

import com.mayhem.core.network.StreamBuffer;
import com.mayhem.rs2.entity.Location;
import com.mayhem.rs2.entity.player.net.Client;
import com.mayhem.rs2.entity.player.net.out.OutgoingPacket;

/**
 * @author Yasin
 */
public class RemoveObject extends OutgoingPacket {

    private Location location;

    private byte objectType;

    public RemoveObject(Location location, byte objectType) {
        this.location = location;
        this.objectType = objectType;
    }
    @Override
    public void execute(Client client) {
        StreamBuffer.OutBuffer out = StreamBuffer.newOutBuffer(3);
        out.writeHeader(client.getEncryptor(), 101);
        new SendCoordinates(location, client.getPlayer());
        out.writeByte(-objectType, StreamBuffer.ValueType.C);
        out.writeByte(location.getX() << 4 + location.getY(), StreamBuffer.ValueType.STANDARD);
        client.send(out.getBuffer());


    }

    @Override
    public int getOpcode() {
        return 101;
    }
}
