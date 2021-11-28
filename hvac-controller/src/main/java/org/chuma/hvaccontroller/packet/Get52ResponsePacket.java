package org.chuma.hvaccontroller.packet;

import org.chuma.hvaccontroller.device.FanSpeed;
import org.chuma.hvaccontroller.device.OperatingMode;

public class Get52ResponsePacket extends AbstractPacket {
    public static final int MASK_TARGET_TEMP = 0x3F;
    public static final int MASK_AIR_TEMP = 0x3F;
    public static final int MASK_FAN_SPEED = 0x07;
    public static final int MASK_MODE = 0x0F;
    public static final int MASK_MODE_AUTO = 0x20;
    public static final int MASK_ON = 0x80;
    public static final int MASK_X = 0x02;
    public static final int MASK_Y = 0x01;

    public Get52ResponsePacket(PacketData packetData) {
        super(packetData);
    }

    @Override
    public int[] getUnderstandMask() {
        return new int[]{
                MASK_TARGET_TEMP,
                MASK_X | MASK_Y,
                MASK_AIR_TEMP,
                MASK_FAN_SPEED,
                MASK_ON | MASK_MODE | MASK_MODE_AUTO,
                0,
                0,
                MASK_AIR_TEMP // 8th byte is the same as 3th
        };
    }

    public FanSpeed getFanSpeed() {
        int val = packetData.data[3] & MASK_FAN_SPEED;
        return AbstractSetPacket.getFanSpeedImpl(val);
    }

    public boolean isX() {
        return (packetData.data[1] & MASK_X) != 0;
    }

    public boolean isY() {
        return (packetData.data[1] & MASK_Y) != 0;
    }

    public OperatingMode getMode() {
        int value = packetData.data[4] & MASK_MODE;

        if ((value & 0x01) != 0) {
            return OperatingMode.HEAT;
        }
        if ((value & 0x02) != 0) {
            return OperatingMode.COOL;
        }
        if ((value & 0x04) != 0) {
            return OperatingMode.DRY;
        }
        if ((value & 0x08) != 0) {
            return OperatingMode.FAN;
        }
        throw new IllegalArgumentException("Unknown mode: " + packetData.data[3]);
    }

    public boolean isModeAuto() {
        return (packetData.data[4] & MASK_MODE_AUTO) != 0;
    }

    public int getTargetTemperature() {
        return (packetData.data[0] & MASK_TARGET_TEMP) + 9;
    }

    public int getAirTemperature() {
        return (packetData.data[2] & MASK_AIR_TEMP) + 9;
    }

    public boolean isOn() {
        return (packetData.data[4] & MASK_ON) != 0;
    }

    @Override
    public String toString() {
        return String.format("temp:%d;x:%d y:%d;airTemp:%d;fan:%s;on:%d auto:%d mode:%s;", getTargetTemperature(), boolAsInt(isX()), boolAsInt(isY()), getAirTemperature(), getFanSpeed(), boolAsInt(isOn()), boolAsInt(isModeAuto()), getMode());
    }
}