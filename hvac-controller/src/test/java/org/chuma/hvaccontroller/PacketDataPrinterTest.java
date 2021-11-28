package org.chuma.hvaccontroller;

import java.io.FileOutputStream;
import java.io.FileWriter;

import org.chuma.hvaccontroller.debug.HtmlOutputWriter;
import org.chuma.hvaccontroller.debug.PacketFileSource;
import org.chuma.hvaccontroller.debug.PacketPrinter;
import org.chuma.hvaccontroller.device.HvacDevice;
import org.junit.Test;

public class PacketDataPrinterTest {
    @Test
    public void testFormatFile() throws Exception {
        PacketFileSource reader = new PacketFileSource("C:\\Home\\chuma\\work\\HomeAutomation\\HvacController\\data\\raw-20171113_10-15-08.log");
        IPacketProcessor printer = new PacketPrinter(new HtmlOutputWriter(new FileWriter("C:\\Home\\chuma\\work\\HomeAutomation\\HvacController\\data\\A0-2\\A0.html")), true);
        HvacDevice hvacDevice = new HvacDevice(reader, new IPacketProcessor[]{printer});
        hvacDevice.start();

        Thread.sleep(2000);
    }

    @Test
    public void testWriteBytes() throws Exception {
        try (FileOutputStream os = new FileOutputStream("aaa.tmp")) {
            int[] data = new int[]{0x32, 0x84, 0x20, 0xA0, 0x1F, 0x18, 0x55, 0x04, 0xF4, 0x00, 0x00, 0x00, 0xA6, 0x34};
            for (int c : data) {
                os.write(new byte[]{(byte) c});
            }
        }
    }
}