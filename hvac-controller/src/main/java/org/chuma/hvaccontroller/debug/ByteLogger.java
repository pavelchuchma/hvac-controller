package org.chuma.hvaccontroller.debug;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ByteLogger {
    FileWriter fileWriter;

    public ByteLogger() throws IOException {
        String now = new SimpleDateFormat("yyyyMMdd_hh-mm-ss").format(new Date());
        fileWriter = new FileWriter("bytes-" + now + ".log");
    }

    public void logByte(int time, int value) throws IOException {
        String s = String.format("%d %02X\n", time, value);
        fileWriter.write(s);
    }

    public void byteSent(int value) throws IOException {
        String s = String.format("out: %02X\n", value);
        fileWriter.write(s);
    }

    public void flush() throws IOException {
        fileWriter.flush();
    }
}
