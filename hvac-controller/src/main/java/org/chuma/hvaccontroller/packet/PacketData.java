package org.chuma.hvaccontroller.packet;

import java.io.IOException;
import java.util.Arrays;

import static java.lang.String.format;

// 32 84 20 64 21 00 02 DA 00 00 00 00 39 34
// st fr to cm d0 d1 d2 d3 d4 d5 d6 d7 cr en
public class PacketData {
    public static final int START_BYTE = 0x32;
    public static final int STOP_BYTE = 0x34;
    public static final int PACKET_LENGTH = 14;
    public int[] data;
    public int[] rawData;
    public int readTime;
    public int from;
    public int to;
    public int command;


    public PacketData(int[] rawData, int readTime) throws IOException {
        initialize(rawData, readTime);
    }

    public PacketData(int from, int to, int command, int[] data) {
        this.from = from;
        this.to = to;
        this.command = command;
        this.data = data;
        if (data.length != 8) {
            throw new IllegalArgumentException("int[8] data expected");
        }

        rawData = new int[14];
        rawData[0] = START_BYTE;
        rawData[1] = from;
        rawData[2] = to;
        rawData[3] = command;
        System.arraycopy(data, 0, rawData, 4, data.length);
        rawData[4 + data.length] = computeCrc(rawData);
        rawData[4 + data.length + 1] = STOP_BYTE;
    }

    /**
     * From raw line
     */
    public PacketData(String line) throws IOException {
        String[] parts = line.trim().split(" ");
        int readTime = Integer.parseInt(parts[0]);
        int[] rawData = new int[parts.length - 1];
        for (int i = 0; i < rawData.length; i++) {
            rawData[i] = Integer.parseInt(parts[i + 1], 16);
        }
        initialize(rawData, readTime);
    }

    public static void appendFormattedNumber(StringBuilder sb, int num) {
        sb.append(String.format("%02X", num));
    }

    public static String dataArrayToString(int[] data) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < data.length; i++) {
            int b = data[i];
            appendFormattedNumber(sb, b);
            if (i + 1 < data.length)
                sb.append(" ");
        }
        return sb.toString();
    }

    public boolean isRequest() {
        return from == 0x84 || from == 0x85;
    }

    private void initialize(int[] rawDataIn, int readTime) throws IOException {
        this.readTime = readTime;
        this.rawData = rawDataIn.clone();

        int crc = computeCrc(rawData);

        if (rawData.length < 3) {
            throw new IOException(format("Packet is too short: [%3d]: %s", readTime, dataArrayToString(rawData)));
        }

        if (crc != rawData[rawData.length - 2]) {
            throw new IOException(format("CRC Error: [%3d]: %s", readTime, dataArrayToString(rawData)));
        }

        if (rawData[0] == START_BYTE && rawData[rawData.length - 1] == STOP_BYTE) {
            from = rawData[1];
            to = rawData[2];
            command = rawData[3];
            data = Arrays.copyOfRange(rawData, 4, rawData.length - 2);
        } else {
            from = to = -1;
            data = rawData;
        }
    }

    private int computeCrc(int[] rawData) {
        int crc = 0;
        for (int i = 1; i < rawData.length - 2; i++) {
            crc ^= rawData[i];
        }
        return crc;
    }

    public String toRawString() {
        return String.format("%4d %s", readTime, dataArrayToString(rawData));
    }
}
