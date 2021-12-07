package org.chuma.hvaccontroller.packet;

import org.chuma.hvaccontroller.device.OperatingMode;

public class Get53ResponsePacket extends AbstractPacket {
    public static final int MASK_SLEEP = 0x20;
    public static final int MASK_MODE = 0x07;

    public Get53ResponsePacket(PacketData packetData) {
        super(packetData);
    }

    @Override
    public int[] getUnderstandMask() {
        return new int[]{
                0,
                0,
                0,
                0,
                MASK_SLEEP,
                0,
                0,
                MASK_MODE
        };
    }

    public boolean isSleepMode() {
        return (packetData.data[4] & MASK_SLEEP) != 0;
    }

    public OperatingMode getMode() {
        int val = packetData.data[7] & MASK_MODE;
        return AbstractSetPacket.getOperatingModeImpl(val);
    }

    @Override
    public String toString() {
        return String.format("%s:;;;;sleep:%d;;;mode:%s;", super.toString(), boolAsInt(isSleepMode()), getMode());
    }
}
