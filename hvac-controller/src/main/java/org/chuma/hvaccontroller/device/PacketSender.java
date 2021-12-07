package org.chuma.hvaccontroller.device;

import org.chuma.hvaccontroller.debug.ByteLogger;
import org.chuma.hvaccontroller.packet.PacketData;
import org.chuma.hvaccontroller.packet.PacketType;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;

class PacketSender {
    public static final int SEND_RETRY_COUNT = 5;
    ConcurrentLinkedQueue<PacketData> sendQueue = new ConcurrentLinkedQueue<>();
    PacketData current;
    boolean packetSent = false;
    private int retryCount;
    private final OutputStream outputStream;
    private final ByteLogger byteLogger;

    public PacketSender(OutputStream outputStream, ByteLogger byteLogger) {
        this.outputStream = outputStream;
        this.byteLogger = byteLogger;
    }

    void send(PacketData packet) {
        sendQueue.add(packet);
    }

    void notifyPacketReceived(PacketData receivedPacket) {
        if (packetSent) {
            if (receivedPacket.command == PacketType.CMD_SET_RESPONSE
                    && receivedPacket.from == current.to && receivedPacket.to == current.from
                    && Arrays.equals(current.data, receivedPacket.data)) {
                HvacConnector.log.debug("send of packet confirmed");
                // sent, remove head of queue
                current = null;
            } else {
                // sent failure
                if (retryCount-- < 0) {
                    HvacConnector.log.error("send failed, discarding packet: " + current.toRawString());
                    current = null;
                } else {
                    HvacConnector.log.error("send failed, retrying: " + current.toRawString());
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
            HvacConnector.log.debug("packet sent");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
