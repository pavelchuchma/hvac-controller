package org.chuma.hvaccontroller;

import java.io.IOException;

import org.chuma.hvaccontroller.packet.PacketData;

public interface IPacketSource {
    PacketData getPacket();

    void startRead() throws IOException;

    void stopRead();

    void sendData(PacketData data);
}
