package org.chuma.hvaccontroller;

import org.chuma.hvaccontroller.packet.PacketData;

import java.io.IOException;

public interface IPacketSource {
    PacketData getPacket();

    void startRead() throws IOException;

    void stopRead();

    void sendData(PacketData data);
}
