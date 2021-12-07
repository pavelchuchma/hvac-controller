package org.chuma.hvaccontroller.device;

import org.chuma.hvaccontroller.debug.ByteLogger;
import org.chuma.hvaccontroller.packet.Get52ResponsePacket;
import org.chuma.hvaccontroller.packet.Get53ResponsePacket;
import org.chuma.hvaccontroller.packet.Get54ResponsePacket;
import org.chuma.hvaccontroller.packet.Packet;
import org.chuma.hvaccontroller.packet.PacketFactory;
import org.chuma.hvaccontroller.packet.SetPacketResponse;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class PacketReaderTest {
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
    }

    public static InputStream getSlowTestResourceStream() throws FileNotFoundException {
        ClassLoader classLoader = PacketReaderTest.class.getClassLoader();
        //noinspection ConstantConditions
        File file = new File(classLoader.getResource("ttyUSB0.dump").getFile());
        return new SlowInputStream(new FileInputStream(file));
    }

    @Test
    public void parsePackets() throws Exception {
        try(InputStream inputStream = getSlowTestResourceStream()) {

            PacketReader pr = new PacketReader(inputStream, new ByteLogger());
            Packet[] packets = new Packet[580];
            for (int i=0; i<packets.length;i++) {
                packets[i] = PacketFactory.Deserialize(pr.readNext());
            }
            {
                // 270 = {Get52ResponsePacket@2415} "0x20->0x84: temp:24;x:1 y:1;airTemp:24;fan:SPEED_3;on:1 auto:0 mode:FAN;"
                Get52ResponsePacket p = (Get52ResponsePacket) packets[270];
                Assert.assertEquals(24, p.getAirTemperature());
                Assert.assertEquals(24, p.getTargetTemperature());
                Assert.assertEquals(FanSpeed.SPEED_3, p.getFanSpeed());
                Assert.assertTrue(p.isOn());
                Assert.assertFalse(p.isModeAuto());
                Assert.assertEquals(OperatingMode.FAN, p.getMode());
            }
            {
                // 333 = {Get52ResponsePacket@2481} "0x20->0x84: temp:25;x:1 y:0;airTemp:24;fan:SPEED_1;on:1 auto:0 mode:HEAT;"
                Get52ResponsePacket p = (Get52ResponsePacket) packets[333];
                Assert.assertEquals(24, p.getAirTemperature());
                Assert.assertEquals(25, p.getTargetTemperature());
                Assert.assertEquals(FanSpeed.SPEED_1, p.getFanSpeed());
                Assert.assertTrue(p.isOn());
                Assert.assertFalse(p.isModeAuto());
                Assert.assertEquals(OperatingMode.HEAT, p.getMode());
            }
            {
                // 513 = {Get52ResponsePacket@2669} "0x20->0x84: temp:25;x:0 y:1;airTemp:22;fan:SPEED_1;on:0 auto:0 mode:HEAT;"
                Get52ResponsePacket p = (Get52ResponsePacket) packets[513];
                Assert.assertEquals(22, p.getAirTemperature());
                Assert.assertEquals(25, p.getTargetTemperature());
                Assert.assertEquals(FanSpeed.SPEED_1, p.getFanSpeed());
                Assert.assertFalse(p.isOn());
                Assert.assertFalse(p.isModeAuto());
                Assert.assertEquals(OperatingMode.HEAT, p.getMode());
            }
            {
                // 321 = {Get53ResponsePacket@2469} ";;;;sleep:0;;;mode:HEAT;"
                Get53ResponsePacket p = (Get53ResponsePacket) packets[321];
                Assert.assertEquals(OperatingMode.HEAT, p.getMode());
                Assert.assertFalse(p.isSleepMode());
            }
            {
                // 390 = {Get53ResponsePacket@2538} ";;;;sleep:1;;;mode:HEAT;"
                Get53ResponsePacket p = (Get53ResponsePacket) packets[390];
                Assert.assertEquals(OperatingMode.HEAT, p.getMode());
                Assert.assertTrue(p.isSleepMode());
            }
            {
                // 393 = {Get54ResponsePacket@2541} ";quite:0"
                Get54ResponsePacket p = (Get54ResponsePacket) packets[393];
                Assert.assertFalse(p.isQuite());
            }
            {
                // 357 = {Get54ResponsePacket@2505} ";quite:1"
                Get54ResponsePacket p = (Get54ResponsePacket) packets[357];
                Assert.assertTrue(p.isQuite());
            }
            {
                // 348 = {SetPacketResponse@2496} "sleep:0;;temp:25 fan:SPEED_1;mode:HEAT;on:1;;quite:1;"
                SetPacketResponse p = (SetPacketResponse) packets[348];
                Assert.assertFalse(p.isSleep());
                Assert.assertEquals(25, p.getTargetTemperature());
                Assert.assertEquals(FanSpeed.SPEED_1, p.getFanSpeed());
                Assert.assertEquals(OperatingMode.HEAT, p.getMode());
                Assert.assertTrue(p.isOn());
                Assert.assertTrue(p.isQuite());
            }
            {
                // 387 = {SetPacketResponse@2535} "sleep:1;;temp:25 fan:SPEED_1;mode:HEAT;on:1;;quite:0;"
                SetPacketResponse p = (SetPacketResponse) packets[387];
                Assert.assertTrue(p.isSleep());
                Assert.assertFalse(p.isQuite());
            }
            pr.stop();
        }
    }
}
