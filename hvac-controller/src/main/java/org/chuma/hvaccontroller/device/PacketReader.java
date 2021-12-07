package org.chuma.hvaccontroller.device;

import org.apache.log4j.Logger;
import org.chuma.hvaccontroller.debug.ByteLogger;
import org.chuma.hvaccontroller.packet.PacketData;

import java.io.IOException;
import java.io.InputStream;

class PacketReader {
    public static final int MAX_PACKET_BYTE_READ_TIME = 10;

    static class ReceivedChar {
        int character;
        int readTime;

        public ReceivedChar(int character, int readTime) {
            this.character = character;
            this.readTime = readTime;
        }
    }

    private final InputStream inputStream;
    private final ByteLogger byteLogger;

    private boolean stopped = false;
    private ReceivedChar c;
    int[] buff = new int[PacketData.PACKET_LENGTH];
    static Logger log = Logger.getLogger(PacketReader.class.getName());

    public PacketReader(InputStream inputStream, ByteLogger byteLogger) {
        this.inputStream = inputStream;
        this.byteLogger = byteLogger;
    }

    public PacketData readNext() throws IOException {
        c = waitForStartChar(c);
        int initialReadTime = c.readTime;
        for (int i = 0; i < PacketData.PACKET_LENGTH - 1 && !stopped; i++) {
            buff[i] = c.character;
            c = readChar();
        }
        buff[PacketData.PACKET_LENGTH - 1] = c.character;

        if (buff[PacketData.PACKET_LENGTH - 1] != PacketData.STOP_BYTE) {
            throw new IOException("Read packet not terminated by STOP_BYTE");
        }

        return new PacketData(buff, initialReadTime);
    }

    public void stop() {
        stopped = true;
    }

    private ReceivedChar waitForStartChar(ReceivedChar c) throws IOException {
        if (c == null || c.character == PacketData.STOP_BYTE) {
            c = readChar();
            if (c.character == PacketData.START_BYTE) {
                return c;
            }
        }
        // clear read cache and wait for start char
        while (c.character != PacketData.START_BYTE || c.readTime <= MAX_PACKET_BYTE_READ_TIME) {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Ignoring initial char %02X with read time: %d%n", c.character, c.readTime));
            }
            c = readChar();
        }
        return c;
    }

    private ReceivedChar readChar() throws IOException {
        long startTime = System.currentTimeMillis();
        int b = inputStream.read();
        if (b < 0) {
            throw new IOException("End of stream reached");
        }
        int readTime = (int) (System.currentTimeMillis() - startTime);
        if (byteLogger != null) {
            byteLogger.logByte(readTime, b);
        }
        return new ReceivedChar(b, readTime);
    }
}
