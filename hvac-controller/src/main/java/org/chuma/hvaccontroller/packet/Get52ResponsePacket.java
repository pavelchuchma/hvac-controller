package org.chuma.hvaccontroller.packet;

import org.chuma.hvaccontroller.device.FanSpeed;
import org.chuma.hvaccontroller.device.OperatingMode;

public class Get52ResponsePacket extends AbstractPacket {
    public static final int MASK_TEMPERATURE = 0x7F;
    public static final int MASK_FAN_SPEED = 0x07;
    public static final int MASK_ON = 0x80;
    public static final int MASK_MODE_AUTO = 0x20;
    public static final int MASK_DEFROST = 0x10;
    public static final int MASK_MODE = 0x0F;

    public Get52ResponsePacket(PacketData packetData) {
        super(packetData);
    }

    @Override
    public int[] getUnderstandMask() {
        return new int[]{
                MASK_TEMPERATURE,
                MASK_TEMPERATURE,
                MASK_TEMPERATURE,
                MASK_FAN_SPEED,
                MASK_ON | MASK_MODE_AUTO | MASK_DEFROST | MASK_MODE,
                0,
                0,
                MASK_TEMPERATURE
        };
    }

    public FanSpeed getFanSpeed() {
        int val = packetData.data[3] & MASK_FAN_SPEED;
        return AbstractSetPacket.getFanSpeedImpl(val);
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
        return decodeTemperature(0);
    }

    public int getRoomTemperature() {
        return decodeTemperature(1);
    }

    public int getAirTemperature() {
        return decodeTemperature(2);
    }

    public int getAir2Temperature() {
        return decodeTemperature(7);
    }

    private int decodeTemperature(int byteIndex) {
        return (packetData.data[byteIndex] & MASK_TEMPERATURE) - 55;
    }

    public boolean isOn() {
        return (packetData.data[4] & MASK_ON) != 0;
    }

    public boolean isDefrost() {
        return (packetData.data[4] & MASK_DEFROST) != 0;
    }

    @Override
    public String valuesToString() {
        return String.format("tgtTemp:%d;roomTemp:%d;airTemp:%d;fan:%s;on:%d auto:%d defrost:%d mode:%s;air2Temp:%d",
                getTargetTemperature(), getRoomTemperature(), getAirTemperature(), getFanSpeed(),
                boolAsInt(isOn()), boolAsInt(isModeAuto()), boolAsInt(isDefrost()), getMode(), getAir2Temperature());
    }
}