package org.chuma.hvaccontroller.device;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import org.apache.log4j.Logger;
import org.chuma.hvaccontroller.IPacketSource;
import org.chuma.hvaccontroller.packet.PacketData;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class HvacConnector implements IPacketSource {
    static Logger log = Logger.getLogger(HvacConnector.class.getName());
    private final String portName;
    BlockingQueue<PacketData> packetDataQueue = new LinkedBlockingQueue<>();
    boolean stopped = false;
    PacketReader packetReader;
    PacketSender packetSender;

    public HvacConnector(String portName) {
        this.portName = portName;
    }

    private SerialPort openSerialPort() throws IOException {
        //noinspection rawtypes
        Enumeration portList = CommPortIdentifier.getPortIdentifiers();

        while (portList.hasMoreElements()) {
            CommPortIdentifier portId = (CommPortIdentifier) portList.nextElement();
            log.debug("Checking: " + portId.getName());

            if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                if (portId.getName().equals(portName)) {
                    log.info("Port found: " + portId.getName());

                    try {
                        SerialPort serialPort = (SerialPort) portId.open("SimpleReadApp", 2000);
                        serialPort.notifyOnDataAvailable(false);
                        serialPort.setSerialPortParams(2400,
                                SerialPort.DATABITS_8,
                                SerialPort.STOPBITS_1,
                                SerialPort.PARITY_EVEN);
                        serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
                        serialPort.setOutputBufferSize(0);
                        serialPort.setInputBufferSize(0);
                        serialPort.enableReceiveTimeout(600_000);
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

    public void startRead() throws IOException {
        SerialPort serialPort = openSerialPort();
        startRead(serialPort.getInputStream(), serialPort.getOutputStream());
    }

    public void startRead(InputStream inputStream, OutputStream outputStream) {
        this.packetReader = new PacketReader(inputStream);
        this.packetSender = new PacketSender(outputStream);

        Thread thread = new Thread(() -> {
            while (!stopped) {
                try {
                    PacketData receivedPacket = packetReader.readNext();
                    packetSender.notifyPacketReceived(receivedPacket);
                    packetDataQueue.add(receivedPacket);
                } catch (IOException e) {
                    log.error("Read failure", e);
                }
            }
        }, "HvacListener");
        thread.setPriority(Thread.MAX_PRIORITY);
        thread.start();
    }

    @Override
    public void stopRead() {
        stopped = true;
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
