package org.chuma.hvaccontroller.device;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import com.fazecast.jSerialComm.SerialPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractSerialPortConnection {
    static Logger log = LoggerFactory.getLogger(AbstractSerialPortConnection.class.getName());
    protected SerialPort serialPort;
    protected InputStream inputStream = null;
    protected OutputStream outputStream = null;
    protected final String portName;
    protected boolean running = false;

    public AbstractSerialPortConnection(String portName) {
        this.portName = portName;
    }

    protected abstract void initializePort() throws IOException;

    protected void openSerialPort() throws IOException {
        log.debug("Opening serial port {}", portName);
        String portList = String.join(", ", Arrays.stream(SerialPort.getCommPorts()).map(sp -> "[" + sp.getSystemPortPath() + "] " + sp.getDescriptivePortName()).toArray(CharSequence[]::new));
        log.info("Existing serial ports: {}", portList);
        if (serialPort != null) {
            serialPort.closePort();
        }

        serialPort = SerialPort.getCommPort(portName);

        if (!serialPort.openPort()) {
            throw new IOException("Failed to open port " + portName + ", existing ports are: " + portList);
        }
    }

    public void start() {
        running = true;
        new Thread(() -> {
            log.debug("Starting serial port reader thread");
            try {
                while (running) {
                    while (inputStream == null) {
                        try {
                            openSerialPort();
                            initializePort();
                            inputStream = serialPort.getInputStream();
                            outputStream = serialPort.getOutputStream();
                        } catch (Exception e) {
                            log.error("Port initialization failed, going to sleep for a moment before retry", e);
                            try {
                                //noinspection BusyWait
                                Thread.sleep(10_000);
                            } catch (InterruptedException ignored) {
                            }
                        }
                    }

                    try {
                        readImpl();
                    } catch (IOException e) {
                        log.error("Read failed", e);
                        inputStream = null;
                        outputStream = null;
                    }
                }
            } finally {
                log.debug("Serial port reader thread finished");
            }
        }, "ComRead-" + portName).start();
    }

    public void stop() {
        running = false;
        if (serialPort != null) {
            serialPort.closePort();
            serialPort = null;
            inputStream = null;
            outputStream = null;
            log.debug("Serial port listener closed");
        }
    }

    protected abstract void readImpl() throws IOException;
}
