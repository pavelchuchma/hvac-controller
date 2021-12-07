package org.chuma.hvaccontroller.packet;

import org.chuma.hvaccontroller.device.FanSpeed;
import org.chuma.hvaccontroller.device.OperatingMode;

public abstract class AbstractSetPacket extends AbstractPacket {

    public static final int MASK_SLEEP = 0x20;
    public static final int MASK_QUITE = 0x20;
    public static final int MASK_MODE = 0x07;
    public static final int MASK_FAN_SPEED = 0xE0;
    public static final int MASK_TARGET_TEMP = 0x1F;
    public static final int MASK_ON = 0x30;

    public AbstractSetPacket(PacketData data) {
        super(data);
    }

    public AbstractSetPacket(int from, int to, int command, boolean on, OperatingMode mode, FanSpeed fanSpeed, int temp, boolean sleep, boolean quite) {
        super(createData(from, to, command, on, mode, fanSpeed, temp, sleep, quite));
    }

    private static PacketData createData(int from, int to, int command, boolean on, OperatingMode mode, FanSpeed fanSpeed, int temp, boolean sleep, boolean quite) {
        int[] data = new int[8];
        data[0] = 0b00011111 + ((sleep) ? MASK_SLEEP : 0);
        data[1] = 0b00011000;
        data[2] = temp + (serializeFanSpeed(fanSpeed) << 5);
        data[3] = serializeOperatingMode(mode);
        data[4] = 0b11000100 + ((on) ? MASK_ON : 0);
        data[5] = 0;
        data[6] = ((quite) ? MASK_QUITE : 0);
        data[7] = 0;
        return new PacketData(from, to, command, data);
    }

    private static int serializeOperatingMode(OperatingMode mode) {
        switch (mode) {
            case AUTO:
                return 0x00;
            case COOL:
                return 0x01;
            case FAN:
                return 0x03;
            case DRY:
                return 0x02;
            case HEAT:
                return 0x04;
            default:
                throw new IllegalStateException("Mode " + mode + " cannot be serialized");
        }
    }

    private static int serializeFanSpeed(FanSpeed speed) {
        switch (speed) {
            case AUTO:
                return 0x00;
            case SPEED_1:
                return 0x02;
            case SPEED_2:
                return 0x04;
            case SPEED_3:
                return 0x05;
            default:
                throw new IllegalStateException("Speed " + speed + " cannot be serialized");
        }
    }

    public static FanSpeed getFanSpeedImpl(int val) {
        switch (val) {
            case 0x00:
                return FanSpeed.AUTO;
            case 0x02:
                return FanSpeed.SPEED_1;
            case 0x04:
                return FanSpeed.SPEED_2;
            case 0x05:
                return FanSpeed.SPEED_3;
            default:
                throw new IllegalArgumentException("Unexpected mode number: " + val);
        }
    }

    public static OperatingMode getOperatingModeImpl(int val) {
        switch (val) {
            case 0x00:
                return OperatingMode.AUTO;
            case 0x01:
                return OperatingMode.COOL;
            case 0x02:
                return OperatingMode.DRY;
            case 0x03:
                return OperatingMode.FAN;
            case 0x04:
                return OperatingMode.HEAT;
            default:
                throw new IllegalArgumentException("Unexpected mode number: " + val);
        }
    }

    public FanSpeed getFanSpeed() {
        int val = (packetData.data[2] & MASK_FAN_SPEED) >> 5;
        return getFanSpeedImpl(val);
    }

    public boolean isSleep() {
        return (packetData.data[0] & MASK_SLEEP) != 0;
    }

    public boolean isQuite() {
        return (packetData.data[6] & MASK_QUITE) != 0;
    }

    public boolean isOn() {
        return (packetData.data[4] & MASK_ON) != 0;
    }

    public OperatingMode getMode() {
        int val = packetData.data[3] & MASK_MODE;
        return getOperatingModeImpl(val);
    }

    public int getTargetTemperature() {
        return (packetData.data[2] & MASK_TARGET_TEMP);
    }

    @Override
    public String toString() {
        return String.format("sleep:%d;;temp:%d fan:%s;mode:%s;on:%d;;quite:%d;", boolAsInt(isSleep()), getTargetTemperature(), getFanSpeed(), getMode(), boolAsInt(isOn()), boolAsInt(isQuite()));
    }

    @Override
    public int[] getUnderstandMask() {
        return new int[]{
                MASK_SLEEP,
                0,
                MASK_TARGET_TEMP | MASK_FAN_SPEED,
                MASK_MODE,
                MASK_ON,
                0,
                MASK_QUITE,
                0};
    }
}
