package org.chuma.hvaccontroller;

import java.io.FileWriter;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Test;

import org.chuma.hvaccontroller.debug.HtmlOutputWriter;
import org.chuma.hvaccontroller.debug.PacketPrinter;
import org.chuma.hvaccontroller.device.HvacConnector;
import org.chuma.hvaccontroller.device.HvacDevice;
import org.chuma.hvaccontroller.device.PacketReaderTest;

public class PacketDataPrinterTest {
    @Test
    public void testFormatFile() throws Exception {
        InputStream inputStream = PacketReaderTest.getSlowTestResourceStream();
        HvacConnector hvacConnector = new HvacConnector(inputStream, Files.newOutputStream(Paths.get("ttyUSB0.out.tmp")));

        IPacketProcessor printer = new PacketPrinter(new HtmlOutputWriter(new FileWriter("ttyUSB0.html")), true);
        HvacDevice hvacDevice = new HvacDevice(hvacConnector, 0x85, 0x20, new IPacketProcessor[]{printer});
        hvacDevice.start();

        // ugly wait for source file to be read
        while (inputStream.available() > 0) {
            //noinspection BusyWait
            Thread.sleep(100);
        }
    }
}