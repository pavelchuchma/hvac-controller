package org.chuma.hvaccontroller;

import org.chuma.hvaccontroller.packet.Packet;

import java.io.IOException;

public interface IPacketProcessor {
    void start() throws IOException;

    void stop() throws IOException;

    void process(Packet packetData) throws IOException;
}
