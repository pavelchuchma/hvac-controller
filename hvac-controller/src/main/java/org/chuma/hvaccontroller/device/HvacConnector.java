package org.chuma.hvaccontroller.device;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.chuma.hvaccontroller.IPacketSource;
import org.chuma.hvaccontroller.debug.ByteLogger;
import org.chuma.hvaccontroller.packet.PacketData;
import org.chuma.hvaccontroller.packet.PacketType;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import org.apache.log4j.Logger;

public class HvacConnector implements IPacketSource {
    public static final int MAX_PACKET_BYTE_READ_TIME = 10;
    static Logger log = Logger.getLogger(HvacConnector.class.getName());
    private final String portName;
    private final boolean logBytes;
    BlockingQueue<PacketData> packetDataQueue = new LinkedBlockingQueue<>();
    boolean closed = false;
    ByteLogger byteLogger;
    PacketReader packetReader = new PacketReader();
    PacketSender packetSender = new PacketSender();
    private InputStream inputStream;
    private OutputStream outputStream;

    public HvacConnector(String portName, boolean logBytes) {
        this.portName = portName;
        this.logBytes = logBytes;
    }

    private SerialPort openSerialPort() throws IOException {
        Enumeration portList = CommPortIdentifier.getPortIdentifiers();
        SerialPort serialPort;
        int baudRate = 2400;

        while (portList.hasMoreElements()) {
            CommPortIdentifier portId = (CommPortIdentifier) portList.nextElement();
            log.debug("Checking: " + portId.getName());

            if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                if (portId.getName().equals(portName)) {
                    log.info("Port found: " + portId.getName());

                    try {
                        serialPort = (SerialPort) portId.open("SimpleReadApp", 2000);
                        serialPort.notifyOnDataAvailable(false);
                        serialPort.setSerialPortParams(baudRate,
                                SerialPort.DATABITS_8,
                                SerialPort.STOPBITS_1,
                                SerialPort.PARITY_EVEN);
                        serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
                        serialPort.setOutputBufferSize(0);
                        serialPort.setInputBufferSize(0);
                        serialPort.enableReceiveTimeout(600000);

                        return serialPort;

                    } catch (Exception e) {
                        throw new IOException("Failed to open serial port '" + portName + "'", e);
                    }
                }
            }
        }
        throw new IOException("Serial port '" + portName + "' not found");
    }

    public void sendData(PacketData p) {
        packetSender.send(p);
    }

    void sendDataImpl(PacketData p) throws IOException {
        try {
            Thread.sleep(20);
            for (int c : p.rawData) {
                outputStream.write(new byte[]{(byte) c});
                outputStream.flush();
                if (byteLogger != null) {
                    byteLogger.byteSent(c);
                }
                Thread.sleep(5);
            }
            log.debug("packet sent");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void startRead() throws IOException {
        SerialPort serialPort = openSerialPort();
        this.inputStream = serialPort.getInputStream();
        this.outputStream = serialPort.getOutputStream();
        if (logBytes) {
            byteLogger = new ByteLogger();
        }

        Thread thread = new Thread(() -> {
            while (!closed) {
                try {
                    PacketData receivedPacket = packetReader.readNext();
                    packetSender.notifyPacketReceived(receivedPacket);
                    packetDataQueue.add(receivedPacket);

                    if (byteLogger != null) {
                        byteLogger.flush();
                    }
                } catch (IOException e) {
                    log.error("Read failure", e);
                }
            }
        }, "HvacListener");
        thread.setPriority(Thread.MAX_PRIORITY);
        thread.start();
    }

    @Override
    public PacketData getPacket() {
        try {
            return packetDataQueue.take();
        } catch (InterruptedException e) {
            return null;
        }
    }

    static class ReceivedChar {
        int character;
        int readTime;

        public ReceivedChar(int character, int readTime) {
            this.character = character;
            this.readTime = readTime;
        }
    }

    class PacketReader {
        public ReceivedChar c;
        int[] buff = new int[PacketData.PACKET_LENGTH];

        public PacketData readNext() throws IOException {
            c = waitForStartChar(c);
            int initialReadTime = c.readTime;
            for (int i = 0; i < PacketData.PACKET_LENGTH - 1 && !closed; i++) {
                buff[i] = c.character;
                c = readChar();
            }
            buff[PacketData.PACKET_LENGTH - 1] = c.character;

            if (buff[PacketData.PACKET_LENGTH - 1] != PacketData.STOP_BYTE) {
                throw new IOException("Read packet not terminated by STOP_BYTE");
            }

            return new PacketData(buff, initialReadTime);
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

    private class PacketSender {
        public static final int SEND_RETRY_COUNT = 5;
        ConcurrentLinkedQueue<PacketData> sendQueue = new ConcurrentLinkedQueue<>();
        PacketData current;
        boolean packetSent = false;
        private int retryCount;

        void send(PacketData packet) {
            sendQueue.add(packet);
        }

        void notifyPacketReceived(PacketData receivedPacket) {
            if (packetSent) {
                if (receivedPacket.command == PacketType.CMD_SET_RESPONSE
                        && receivedPacket.from == current.to && receivedPacket.to == current.from
                        && Arrays.equals(current.data, receivedPacket.data)) {
                    log.debug("send of packet confirmed");
                    // sent, remove head of queue
                    current = null;
                } else {
                    // sent failure
                    if (retryCount-- < 0) {
                        log.error("send failed, discarding packet: " + current.toRawString());
                        current = null;
                    } else {
                        log.error("send failed, retrying: " + current.toRawString());
                    }
                }
                packetSent = false;
            }

            if (receivedPacket.command == PacketType.CMD_CONTINUE) {
                if (current == null) {
                    current = sendQueue.poll();
                    retryCount = SEND_RETRY_COUNT;
                }

                if (current != null) {
                    try {
                        sendDataImpl(current);
                        packetSent = true;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
