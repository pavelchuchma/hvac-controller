package org.chuma.hvaccontroller.device;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.chuma.hvaccontroller.IPacketProcessor;
import org.chuma.hvaccontroller.IPacketSource;
import org.chuma.hvaccontroller.packet.Get52ResponsePacket;
import org.chuma.hvaccontroller.packet.Get53ResponsePacket;
import org.chuma.hvaccontroller.packet.Get54ResponsePacket;
import org.chuma.hvaccontroller.packet.Packet;
import org.chuma.hvaccontroller.packet.PacketData;
import org.chuma.hvaccontroller.packet.PacketFactory;
import org.chuma.hvaccontroller.packet.SetPacketRequest;
import org.apache.log4j.Logger;

public class HvacDevice {

    public static final int ADDR_THIS_CONTROLLER = 0x85;
    public static final int ADDR_HVAC_DEVICE = 0x20;
    static Logger log = Logger.getLogger(HvacDevice.class.getName());
    private final Collection<IPacketProcessor> processors;
    private IPacketSource connector;
    // HVAC state
    private boolean running;
    private FanSpeed fanSpeed;
    private OperatingMode currentMode;
    private OperatingMode targetMode;
    private boolean autoMode;
    private boolean quiteMode;
    private boolean sleepMode;
    private int targetTemperature;
    private int airTemperature;
    private boolean x;
    private boolean y;

    public HvacDevice(String portName, boolean logBytes, IPacketProcessor additionalProcessors[]) {
        this(new HvacConnector(portName, logBytes), additionalProcessors);
    }

    public HvacDevice(IPacketSource packetSource, IPacketProcessor additionalProcessors[]) {
        connector = packetSource;
        this.processors = new ArrayList<>();
        processors.add(getProcessor());
        if (additionalProcessors != null) {
            Collections.addAll(processors, additionalProcessors);
        }
    }

    private static boolean selectBoolean(Boolean input, boolean current) {
        return (input != null) ? input : current;
    }

    public void start() throws IOException {
        log.debug("Starting HvacDevice");

        for (IPacketProcessor proc : processors) {
            proc.start();
        }

        new Thread(() -> {
            while (true) {
                PacketData packetData = connector.getPacket();
                if (packetData == null) {
                    break;
                }
                Packet packet = PacketFactory.Deserialize(packetData);
                for (IPacketProcessor proc : processors) {
                    try {
                        proc.process(packet);
                    } catch (IOException e) {
                        log.error("Failed to process packet: " + packet, e);
                    }
                }
            }
            for (IPacketProcessor proc : processors) {
                try {
                    proc.stop();
                } catch (IOException e) {
                    log.error("Failed to stop processor", e);
                }
            }
        }, "HvacProcessor").start();

        connector.startRead();
    }

    public IPacketProcessor getProcessor() {
        return new IPacketProcessor() {
            @Override
            public void start() throws IOException {
                log.info("Starting HvacDevice");
            }

            @Override
            public void stop() throws IOException {

            }

            @Override
            public void process(Packet packet) throws IOException {
                if (packet instanceof Get52ResponsePacket) {
                    Get52ResponsePacket get52Resp = (Get52ResponsePacket) packet;
                    running = get52Resp.isOn();
                    fanSpeed = get52Resp.getFanSpeed();
                    currentMode = get52Resp.getMode();
                    autoMode = get52Resp.isModeAuto();
                    targetTemperature = get52Resp.getTargetTemperature();
                    airTemperature = get52Resp.getAirTemperature();
                    x = get52Resp.isX();
                    y = get52Resp.isY();
                } else if (packet instanceof Get53ResponsePacket) {
                    Get53ResponsePacket get53Resp = (Get53ResponsePacket) packet;
                    targetMode = get53Resp.getMode();
                    sleepMode = get53Resp.isSleepMode();
                } else if (packet instanceof Get54ResponsePacket) {
                    Get54ResponsePacket get54Resp = (Get54ResponsePacket) packet;
                    quiteMode = get54Resp.isQuite();
                }

                if (log.isDebugEnabled()) {
                    if (packet.getCommand() == 0xD1) {
                        log.debug("State: " + HvacDevice.this.toString());
                    }
                }
            }
        };
    }

    public void set(Boolean on, OperatingMode mode, FanSpeed fanSpeed, int temp, Boolean sleep, Boolean quite) {
        SetPacketRequest setPacketRequest = new SetPacketRequest(
                ADDR_THIS_CONTROLLER,
                ADDR_HVAC_DEVICE,
                selectBoolean(on, running),
                (mode != OperatingMode.NONE) ? mode : currentMode,
                (fanSpeed != FanSpeed.NONE) ? fanSpeed : this.fanSpeed,
                (temp > 0) ? temp : this.targetTemperature,
                selectBoolean(sleep, sleepMode),
                selectBoolean(quite, quiteMode)
        );

        if (log.isDebugEnabled()) {
            log.debug("Sending: " + setPacketRequest);
        }
        connector.sendData(setPacketRequest.getData());
    }

    public boolean isRunning() {
        return running;
    }

    public FanSpeed getFanSpeed() {
        return fanSpeed;
    }

    public OperatingMode getCurrentMode() {
        return currentMode;
    }

    public OperatingMode getTargetMode() {
        return targetMode;
    }

    public boolean isAutoMode() {
        return autoMode;
    }

    public boolean isQuiteMode() {
        return quiteMode;
    }

    public boolean isSleepMode() {
        return sleepMode;
    }

    public int getTargetTemperature() {
        return targetTemperature;
    }

    public int getAirTemperature() {
        return airTemperature;
    }

    public boolean isX() {
        return x;
    }

    public boolean isY() {
        return y;
    }

    private int boolAsInt(boolean b) {
        return (b) ? 1 : 0;
    }

    @Override
    public String toString() {
        return String.format("on:%d mode:%s(%s) fan:%s tgtTemp:%d airTemp:%d auto:%d sleep:%d quite:%d x:%d y:%d",
                boolAsInt(running), targetMode, currentMode, fanSpeed, targetTemperature, airTemperature, boolAsInt(autoMode), boolAsInt(sleepMode), boolAsInt(quiteMode), boolAsInt(x), boolAsInt(y));
    }
}
