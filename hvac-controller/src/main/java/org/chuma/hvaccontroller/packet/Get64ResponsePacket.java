package org.chuma.hvaccontroller.packet;

public class Get64ResponsePacket extends AbstractPacket {
    public static final int MASK_TEMP_HI_BYTE = 0xFF;
    public static final int MASK_TEMP_LOW_BYTE = 0xFF;

    public Get64ResponsePacket(PacketData packetData) {
        super(packetData);
    }

    @Override
    public int[] getUnderstandMask() {
        return new int[]{
                0,
                0,
                0,
                0,
                MASK_TEMP_HI_BYTE,
                MASK_TEMP_LOW_BYTE,
                0,
                0
        };
    }

    public double getUnitTemperature() {
        return (packetData.data[4] * 256 + packetData.data[5] - 553) / 10f;
    }

    @Override
    public String valuesToString() {
        return String.format(";;unitTemp:%.2f", getUnitTemperature());
    }
}
