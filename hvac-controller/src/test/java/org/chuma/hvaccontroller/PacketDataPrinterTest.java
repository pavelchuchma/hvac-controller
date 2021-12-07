package org.chuma.hvaccontroller;

import org.chuma.hvaccontroller.debug.HtmlOutputWriter;
import org.chuma.hvaccontroller.debug.PacketPrinter;
import org.chuma.hvaccontroller.device.HvacConnector;
import org.chuma.hvaccontroller.device.HvacDevice;
import org.chuma.hvaccontroller.device.PacketReaderTest;
import org.junit.Test;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class PacketDataPrinterTest {
    static class FileHvacConnector extends HvacConnector {
        public FileHvacConnector() {
            super(null, false);
        }

        @Override
        public void startRead() throws IOException {
            InputStream inputStream = PacketReaderTest.getSlowTestResourceStream();
            OutputStream outputStream = new FileOutputStream("ttyUSB0.out.tmp");
            startRead(inputStream, outputStream);
        }
    }

    // @Test
    public void testFormatFile() throws Exception {
        HvacConnector hvacConnector = new FileHvacConnector();
        IPacketProcessor printer = new PacketPrinter(new HtmlOutputWriter(new FileWriter("ttyUSB0.html")), true);
        HvacDevice hvacDevice = new HvacDevice(hvacConnector, new IPacketProcessor[]{printer});
        hvacDevice.start();

        Thread.sleep(10000);
    }
}