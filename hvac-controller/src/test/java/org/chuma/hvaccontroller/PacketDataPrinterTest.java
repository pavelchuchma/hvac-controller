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
        InputStream inputStream;

        public FileHvacConnector() {
            super(null);
        }

        @Override
        public void startRead() throws IOException {
            inputStream = PacketReaderTest.getSlowTestResourceStream();
            OutputStream outputStream = new FileOutputStream("ttyUSB0.out.tmp");
            startRead(inputStream, outputStream);
        }
    }

    @Test
    public void testFormatFile() throws Exception {
        FileHvacConnector hvacConnector = new FileHvacConnector();
        IPacketProcessor printer = new PacketPrinter(new HtmlOutputWriter(new FileWriter("ttyUSB0.html")), true);
        HvacDevice hvacDevice = new HvacDevice(hvacConnector, 0x85, 0x20, new IPacketProcessor[]{printer});
        hvacDevice.start();

        // ugly wait for source file to be read
        while (hvacConnector.inputStream.available() > 0) {
            //noinspection BusyWait
            Thread.sleep(10);
        }
    }
}