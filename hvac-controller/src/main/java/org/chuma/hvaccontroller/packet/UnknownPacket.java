package org.chuma.hvaccontroller.packet;

public class UnknownPacket extends AbstractPacket {
    public UnknownPacket(PacketData packetData) {
        super(packetData);
    }

    @Override
    public int[] getUnderstandMask() {
        return new int[]{0, 0, 0, 0, 0, 0, 0, 0};
    }

    @Override
    public String toString() {
        return String.format("%s: cmd: 0x%02X", super.toString(), packetData.command);
    }
}
