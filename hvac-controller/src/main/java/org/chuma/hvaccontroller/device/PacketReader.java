package org.chuma.hvaccontroller.device;

import org.chuma.hvaccontroller.packet.PacketData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private boolean stopped = false;
    private ReceivedChar c;
    int[] buff = new int[PacketData.PACKET_LENGTH];
    static Logger log = LoggerFactory.getLogger(PacketReader.class.getName());

    public PacketData readNext(InputStream inputStream) throws IOException {
        c = waitForStartChar(inputStream, c);
        int initialReadTime = c.readTime;
        for (int i = 0; i < PacketData.PACKET_LENGTH - 1 && !stopped; i++) {
            buff[i] = c.character;
            c = readChar(inputStream);
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

    private ReceivedChar waitForStartChar(InputStream inputStream, ReceivedChar c) throws IOException {
        if (c == null || c.character == PacketData.STOP_BYTE) {
            c = readChar(inputStream);
            if (c.character == PacketData.START_BYTE) {
                return c;
            }
        }
        // clear read cache and wait for start char
        while (c.character != PacketData.START_BYTE || c.readTime <= MAX_PACKET_BYTE_READ_TIME) {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Ignoring initial char %02X with read time: %d%n", c.character, c.readTime));
            }
            c = readChar(inputStream);
        }
        return c;
    }

    private ReceivedChar readChar(InputStream inputStream) throws IOException {
        long startTime = System.currentTimeMillis();
        int b = inputStream.read();
        if (b < 0) {
            throw new IOException("End of stream reached");
        }
        int readTime = (int) (System.currentTimeMillis() - startTime);
        if (log.isTraceEnabled()) {
            log.trace(String.format("%d %02X\n", readTime, b));
        }
        return new ReceivedChar(b, readTime);
    }
}
