package org.chuma.hvaccontroller.device;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import org.junit.Assert;
import org.junit.Test;

import org.chuma.hvaccontroller.packet.Get52ResponsePacket;
import org.chuma.hvaccontroller.packet.Get53ResponsePacket;
import org.chuma.hvaccontroller.packet.Get54ResponsePacket;
import org.chuma.hvaccontroller.packet.Get64ResponsePacket;
import org.chuma.hvaccontroller.packet.Packet;
import org.chuma.hvaccontroller.packet.PacketData;
import org.chuma.hvaccontroller.packet.PacketFactory;
import org.chuma.hvaccontroller.packet.SetPacketResponse;

public class PacketReaderTest {
    /**
     * Puts 11 ms sleep before each 0x32 byte because start packet detection expects at least 10 ms silence before each packet.
     */
    static class SlowInputStream extends InputStream {
        InputStream is;

        public SlowInputStream(InputStream is) {
            this.is = is;
        }

        @Override
        public int read() throws IOException {
            final int c = is.read();
            if (c == 0x32) {
                try {
                    Thread.sleep(11);
                } catch (InterruptedException e) {
                    throw new IOException(e);
                }
            }
            return c;
        }

        @Override
        public void close() throws IOException {
            is.close();
        }

        @Override
        public int available() throws IOException {
            return is.available();
        }
    }

    public static InputStream getSlowTestResourceStream() throws IOException {
        ClassLoader classLoader = PacketReaderTest.class.getClassLoader();
        //noinspection ConstantConditions
        File file = new File(classLoader.getResource("ttyUSB0.dump").getFile());
        return new SlowInputStream(Files.newInputStream(file.toPath()));
    }

    @Test
    public void parsePackets() throws Exception {
        try (InputStream inputStream = getSlowTestResourceStream()) {

            PacketReader pr = new PacketReader();
            Packet[] packets = new Packet[580];
            for (int i = 0; i < packets.length; i++) {
                packets[i] = PacketFactory.Deserialize(pr.readNext(inputStream));
            }
            {
                // 3 = {Get52ResponsePacket@2108} "0x20->0x84 tgtTemp:23;roomTemp:25;airTemp:25;fan:SPEED_1;on:0 auto:0 defrost:1 mode:HEAT;air2Temp:25"
                Get52ResponsePacket p = (Get52ResponsePacket) packets[3];
                Assert.assertEquals(23, p.getTargetTemperature());
                Assert.assertEquals(25, p.getRoomTemperature());
                Assert.assertEquals(25, p.getAirTemperature());
                Assert.assertEquals(25, p.getAir2Temperature());
                Assert.assertEquals(FanSpeed.SPEED_1, p.getFanSpeed());
                Assert.assertFalse(p.isOn());
                Assert.assertFalse(p.isModeAuto());
                Assert.assertTrue(p.isDefrost());
                Assert.assertEquals(OperatingMode.HEAT, p.getMode());
            }
            {
                // 180 = {Get52ResponsePacket@2400} "0x20->0x84 tgtTemp:24;roomTemp:25;airTemp:25;fan:AUTO;on:1 auto:1 defrost:0 mode:COOL;air2Temp:25"
                Get52ResponsePacket p = (Get52ResponsePacket) packets[180];
                Assert.assertEquals(24, p.getTargetTemperature());
                Assert.assertEquals(25, p.getRoomTemperature());
                Assert.assertEquals(25, p.getAirTemperature());
                Assert.assertEquals(25, p.getAir2Temperature());
                Assert.assertEquals(FanSpeed.AUTO, p.getFanSpeed());
                Assert.assertTrue(p.isOn());
                Assert.assertTrue(p.isModeAuto());
                Assert.assertFalse(p.isDefrost());
                Assert.assertEquals(OperatingMode.COOL, p.getMode());
            }
            {
                // 270 = {Get52ResponsePacket@2092} "0x20->0x84 tgtTemp:24;roomTemp:24;airTemp:24;fan:SPEED_3;on:1 auto:0 defrost:0 mode:FAN;air2Temp:24"
                Get52ResponsePacket p = (Get52ResponsePacket) packets[270];
                Assert.assertEquals(24, p.getAirTemperature());
                Assert.assertEquals(24, p.getTargetTemperature());
                Assert.assertEquals(FanSpeed.SPEED_3, p.getFanSpeed());
                Assert.assertTrue(p.isOn());
                Assert.assertFalse(p.isModeAuto());
                Assert.assertFalse(p.isDefrost());
                Assert.assertEquals(OperatingMode.FAN, p.getMode());
            }
            {
                // 333 = {Get52ResponsePacket@2093} "0x20->0x84 tgtTemp:25;roomTemp:23;airTemp:24;fan:SPEED_1;on:1 auto:0 defrost:0 mode:HEAT;air2Temp:24"
                Get52ResponsePacket p = (Get52ResponsePacket) packets[333];
                Assert.assertEquals(25, p.getTargetTemperature());
                Assert.assertEquals(24, p.getAirTemperature());
                Assert.assertEquals(24, p.getAir2Temperature());
                Assert.assertEquals(23, p.getRoomTemperature());
                Assert.assertEquals(FanSpeed.SPEED_1, p.getFanSpeed());
                Assert.assertTrue(p.isOn());
                Assert.assertFalse(p.isModeAuto());
                Assert.assertEquals(OperatingMode.HEAT, p.getMode());
            }
            {
                // 513 = {Get52ResponsePacket@2094} "0x20->0x84 tgtTemp:25;roomTemp:22;airTemp:22;fan:SPEED_1;on:0 auto:0 defrost:0 mode:HEAT;air2Temp:22"
                Get52ResponsePacket p = (Get52ResponsePacket) packets[513];
                Assert.assertEquals(22, p.getAirTemperature());
                Assert.assertEquals(25, p.getTargetTemperature());
                Assert.assertEquals(FanSpeed.SPEED_1, p.getFanSpeed());
                Assert.assertFalse(p.isOn());
                Assert.assertFalse(p.isModeAuto());
                Assert.assertEquals(OperatingMode.HEAT, p.getMode());
            }
            {
                // 321 = {Get53ResponsePacket@2095} "0x20->0x84 ;;;;sleep:0;;;mode:HEAT;"
                Get53ResponsePacket p = (Get53ResponsePacket) packets[321];
                Assert.assertEquals(OperatingMode.HEAT, p.getMode());
                Assert.assertFalse(p.isSleepMode());
            }
            {
                // 390 = {Get53ResponsePacket@2096} "0x20->0x84 ;;;;sleep:1;;;mode:HEAT;"
                Get53ResponsePacket p = (Get53ResponsePacket) packets[390];
                Assert.assertEquals(OperatingMode.HEAT, p.getMode());
                Assert.assertTrue(p.isSleepMode());
            }
            {
                // 393 = {Get54ResponsePacket@2097} "0x20->0x84 ;quite:0 bladePos:1"
                Get54ResponsePacket p = (Get54ResponsePacket) packets[393];
                Assert.assertFalse(p.isQuite());
                Assert.assertEquals(1, p.getBladePosition());
            }
            {
                // 357 = {Get54ResponsePacket@2098} "0x20->0x84 ;quite:1 bladePos:1"
                Get54ResponsePacket p = (Get54ResponsePacket) packets[357];
                Assert.assertTrue(p.isQuite());
                Assert.assertEquals(1, p.getBladePosition());
            }
            {
                // 324 = {Get54ResponsePacket@2742} "0x20->0x84 ;quite:0 bladePos:2"
                Get54ResponsePacket p = (Get54ResponsePacket) packets[324];
                Assert.assertFalse(p.isQuite());
                Assert.assertEquals(2, p.getBladePosition());
            }
            {
                // 348 = {SetPacketResponse@2099} "0x20->0x84 sleep:0;;temp:25 fan:SPEED_1;mode:HEAT;on:1;;quite:1;"
                SetPacketResponse p = (SetPacketResponse) packets[348];
                Assert.assertFalse(p.isSleep());
                Assert.assertEquals(25, p.getTargetTemperature());
                Assert.assertEquals(FanSpeed.SPEED_1, p.getFanSpeed());
                Assert.assertEquals(OperatingMode.HEAT, p.getMode());
                Assert.assertTrue(p.isOn());
                Assert.assertTrue(p.isQuite());
            }
            {
                // 387 = {SetPacketResponse@2100} "0x20->0x84 sleep:1;;temp:25 fan:SPEED_1;mode:HEAT;on:1;;quite:0;"
                SetPacketResponse p = (SetPacketResponse) packets[387];
                Assert.assertTrue(p.isSleep());
                Assert.assertFalse(p.isQuite());
            }
            {
                // 378 = {Get64ResponsePacket@2793} "0x20->0x84 ;;unitTemp:22.80"
                Get64ResponsePacket p = (Get64ResponsePacket) packets[378];
                Assert.assertEquals(22.80, p.getUnitTemperature(), 0.001);
            }
            {
                // 27 = {Get64ResponsePacket@2132} "0x20->0x84 ;;unitTemp:25.60"
                Get64ResponsePacket p = (Get64ResponsePacket) packets[27];
                Assert.assertEquals(25.60, p.getUnitTemperature(), 0.001);
            }
            pr.stop();
        }
    }

    @Test
    public void parseLowTemperaturePacket() throws Exception {
        {
            Get52ResponsePacket p = (Get52ResponsePacket) PacketFactory.Deserialize(new PacketData(new int[]{50, 32, 132, 82, 78, 80, 64, 253, 130, 12, 0, 64, 155, 52}, 100));
            Assert.assertEquals("0x20->0x84 tgtTemp:23;roomTemp:25;airTemp:9;fan:SPEED_3;on:1 auto:0 defrost:0 mode:COOL;air2Temp:9", p.toString());
        }
        {
            Get52ResponsePacket p = (Get52ResponsePacket) PacketFactory.Deserialize(new PacketData(new int[]{50, 32, 132, 82, 78, 79, 63, 253, 130, 12, 0, 63, 132, 52}, 100));
            Assert.assertEquals("0x20->0x84 tgtTemp:23;roomTemp:24;airTemp:8;fan:SPEED_3;on:1 auto:0 defrost:0 mode:COOL;air2Temp:8", p.toString());
        }
    }
}