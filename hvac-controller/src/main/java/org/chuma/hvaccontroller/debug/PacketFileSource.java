package org.chuma.hvaccontroller.debug;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.chuma.hvaccontroller.IPacketSource;
import org.chuma.hvaccontroller.packet.PacketData;

import java.lang.UnsupportedOperationException;

public class PacketFileSource implements IPacketSource {
    private final String file;
    private final Object startSyncObj = new Object();
    private BufferedReader reader;

    public PacketFileSource(String file) {
        this.file = file;
    }

    @Override
    public PacketData getPacket() {
        try {
            while (reader == null) {
                synchronized (startSyncObj) {
                    startSyncObj.wait();
                }
            }
            String line = reader.readLine();
            if (line != null) {
                return new PacketData(line);
            }
        } catch (IOException e) {
            // ignore it
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void startRead() throws IOException {
        FileReader fr = new FileReader(file);
        reader = new BufferedReader(fr);
        synchronized (startSyncObj) {
            startSyncObj.notify();
        }
    }

    @Override
    public void stopRead() {

    }

    @Override
    public void sendData(PacketData data) {
        throw new UnsupportedOperationException();
    }
}
