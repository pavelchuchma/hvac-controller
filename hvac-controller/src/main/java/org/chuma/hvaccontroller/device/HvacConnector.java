package org.chuma.hvaccontroller.device;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.fazecast.jSerialComm.SerialPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.chuma.hvaccontroller.IPacketSource;
import org.chuma.hvaccontroller.packet.PacketData;

public class HvacConnector implements IPacketSource {
    static Logger log = LoggerFactory.getLogger(HvacConnector.class.getName());
    private final AbstractSerialPortConnection serialPortConnection;
    BlockingQueue<PacketData> packetDataQueue = new LinkedBlockingQueue<>();
    PacketReader packetReader = new PacketReader();
    PacketSender packetSender = new PacketSender();

    class SerialPortConnection extends AbstractSerialPortConnection {
        public SerialPortConnection(String portName) {
            super(portName);
        }

        @Override
        protected void initializePort() throws IOException {
            if (serialPort.setComPortParameters(2400, 8, SerialPort.ONE_STOP_BIT, SerialPort.EVEN_PARITY)
                    && serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING,
                    3600_000, 0)
                    && serialPort.setFlowControl(SerialPort.FLOW_CONTROL_DISABLED)
            ) {
                log.debug("Serial port '[{}] {}' configured.", serialPort.getSystemPortPath(), serialPort.getDescriptivePortName());
            } else {
                throw new IOException("Failed to set configure port " + serialPort.getDescriptivePortName());
            }
        }

        @Override
        protected void readImpl() throws IOException {
            PacketData receivedPacket = packetReader.readNext(inputStream);
            packetSender.notifyPacketReceived(outputStream, receivedPacket);
            packetDataQueue.add(receivedPacket);
        }
    }

    public HvacConnector(String portName) {
        serialPortConnection = new SerialPortConnection(portName);
    }

    /**
     * For testing purposes only
     */
    public HvacConnector(InputStream testInputStream, OutputStream testOutputStream) {
        serialPortConnection = new SerialPortConnection("FAKE-PORT");
        serialPortConnection.inputStream = testInputStream;
        serialPortConnection.outputStream = testOutputStream;
    }

    public void sendData(PacketData p) {
        packetSender.send(p);
    }

    public void startRead() {
        serialPortConnection.start();
    }

    @Override
    public void stopRead() {
        serialPortConnection.stop();
    }

    @Override
    public PacketData getPacket() {
        try {
            return packetDataQueue.take();
        } catch (InterruptedException e) {
            return null;
        }
    }

}
