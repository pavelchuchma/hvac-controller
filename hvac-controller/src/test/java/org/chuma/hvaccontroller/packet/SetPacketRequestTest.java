package org.chuma.hvaccontroller.packet;

import org.chuma.hvaccontroller.IPacketSource;
import org.chuma.hvaccontroller.debug.PacketFileSource;
import org.chuma.hvaccontroller.device.FanSpeed;
import org.chuma.hvaccontroller.device.OperatingMode;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class SetPacketRequestTest {
    @Test
    public void testCreate01() {
        SetPacketRequest packet;
        int[] expData;
        //SetPacketRequest: sleep:0 temp:24 fan:AUTO mode:AUTO on:1 quite:0
        packet = new SetPacketRequest(10, 20, true, OperatingMode.AUTO, FanSpeed.AUTO, 24, false, false);
        expData = new int[]{0b00011111, 0b00011000, 0b00011000, 0b00000000, 0b11110100, 0b00000000, 0b00000000, 0b00000000};
        assertArrayEquals(expData, packet.getData().data);
        assertEquals(10, packet.getFrom());
        assertEquals(20, packet.getTo());

        // mode Cool
        // A0 00011111 00011000 00011000 00000001 11110100 00000000 00000000 00000000
        // SetPacketRequest: sleep:0 temp:24 fan:AUTO mode:COOL on:1 quite:0
        packet = new SetPacketRequest(10, 20, true, OperatingMode.COOL, FanSpeed.AUTO, 24, false, false);
        expData = new int[]{0b00011111, 0b00011000, 0b00011000, 0b00000001, 0b11110100, 0b00000000, 0b00000000, 0b00000000};
        assertArrayEquals(expData, packet.getData().data);

        // mode fan, fan 1
        // A0 00011111 00011000 01011000 00000011 11110100 00000000 00000000 00000000
        // SetPacketRequest: sleep:0 temp:24 fan:SPEED_1 mode:FAN on:1 quite:0
        packet = new SetPacketRequest(10, 20, true, OperatingMode.FAN, FanSpeed.SPEED_1, 24, false, false);
        expData = new int[]{0b00011111, 0b00011000, 0b01011000, 0b00000011, 0b11110100, 0b00000000, 0b00000000, 0b00000000};
        assertArrayEquals(expData, packet.getData().data);

        // heat 23, fan 3
        // A0 00011111 00011000 10110111 00000100 11110100 00000000 00000000 00000000
        // SetPacketRequest: sleep:0 temp:23 fan:SPEED_3 mode:HEAT on:1 quite:0
        packet = new SetPacketRequest(10, 20, true, OperatingMode.HEAT, FanSpeed.SPEED_3, 23, false, false);
        expData = new int[]{0b00011111, 0b00011000, 0b10110111, 0b00000100, 0b11110100, 0b00000000, 0b00000000, 0b00000000};
        assertArrayEquals(expData, packet.getData().data);

        // mode: dry
        // A0 00011111 00011000 00011000 00000010 11110100 00000000 00000000 00000000
        // SetPacketRequest: sleep:0 temp:24 fan:AUTO mode:DRY on:1 quite:0
        packet = new SetPacketRequest(10, 20, true, OperatingMode.DRY, FanSpeed.AUTO, 24, false, false);
        expData = new int[]{0b00011111, 0b00011000, 0b00011000, 0b00000010, 0b11110100, 0b00000000, 0b00000000, 0b00000000};
        assertArrayEquals(expData, packet.getData().data);

        // on: false
        // A0 00011111 00011000 01010111 00000100 11000100 00000000 00000000 00000000
        // SetPacketRequest: sleep:0 temp:23 fan:SPEED_1 mode:HEAT on:0 quite:0
        packet = new SetPacketRequest(10, 20, false, OperatingMode.HEAT, FanSpeed.SPEED_1, 23, false, false);
        expData = new int[]{0b00011111, 0b00011000, 0b01010111, 0b00000100, 0b11000100, 0b00000000, 0b00000000, 0b00000000};
        assertArrayEquals(expData, packet.getData().data);
    }

    @Test
    public void testFromFile() throws Exception {
        ClassLoader classLoader = getClass().getClassLoader();
        //noinspection ConstantConditions
        File file = new File(classLoader.getResource("A0.messages").getFile());
        IPacketSource packetSource = new PacketFileSource(file.getAbsolutePath());
        packetSource.startRead();
        PacketData pd;
        while ((pd = packetSource.getPacket()) != null) {
            SetPacketRequest fp = (SetPacketRequest) PacketFactory.Deserialize(pd);
            System.out.println(fp);
            SetPacketRequest cp = new SetPacketRequest(fp.getFrom(), fp.getTo(), fp.isOn(), fp.getMode(), fp.getFanSpeed(), fp.getTargetTemperature(), fp.isSleep(), fp.isQuite());

            assertEquals(fp.getFrom(), cp.getFrom());
            assertEquals(fp.getTo(), cp.getTo());
            assertEquals(fp.isOn(), cp.isOn());
            assertEquals(fp.isQuite(), cp.isQuite());
            assertEquals(fp.isSleep(), cp.isSleep());
            assertEquals(fp.getFanSpeed(), cp.getFanSpeed());
            assertEquals(fp.getTargetTemperature(), cp.getTargetTemperature());
            assertEquals(fp.getMode(), cp.getMode());
            assertArrayEquals(fp.getData().rawData, cp.getData().rawData);
        }
    }
}