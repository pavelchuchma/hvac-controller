package org.chuma.hvaccontroller.packet;

public class Get54ResponsePacket extends AbstractPacket {
    public static final int MASK_BLADE_POSITION = 0x0F;
    public static final int MASK_QUITE = 0x20;

    public Get54ResponsePacket(PacketData packetData) {
        super(packetData);
    }

    @Override
    public int[] getUnderstandMask() {
        return new int[]{
                0,
                MASK_BLADE_POSITION | MASK_QUITE,
                0,
                0,
                0,
                0,
                0,
                0
        };
    }

    public boolean isQuite() {
        return (packetData.data[1] & MASK_QUITE) != 0;
    }

    public int getBladePosition() {
        return packetData.data[1] & MASK_BLADE_POSITION;
    }

    @Override
    public String valuesToString() {
        return String.format(";quite:%d bladePos:%d", boolAsInt(isQuite()), getBladePosition());
    }
}
