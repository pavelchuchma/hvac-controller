package org.chuma.hvaccontroller.packet;

public class UnknownPacket extends AbstractPacket {
    public UnknownPacket(PacketData packetData) {
        super(packetData);
    }

    @Override
    public int[] getUnderstandMask() {
        return new int[]{0, 0, 0, 0, 0, 0, 0, 0};
    }
}
