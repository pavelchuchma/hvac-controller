package org.chuma.hvaccontroller.packet;

public interface Packet {
    PacketData getData();

    boolean isRequest();

    int getFrom();

    int getTo();

    int getCommand();

    int[] getUnderstandMask();

    String valuesToString();
}
