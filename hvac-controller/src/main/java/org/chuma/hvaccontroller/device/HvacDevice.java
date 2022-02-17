package org.chuma.hvaccontroller.device;

import org.chuma.hvaccontroller.IPacketProcessor;
import org.chuma.hvaccontroller.IPacketSource;
import org.chuma.hvaccontroller.packet.Get52ResponsePacket;
import org.chuma.hvaccontroller.packet.Get53ResponsePacket;
import org.chuma.hvaccontroller.packet.Get54ResponsePacket;
import org.chuma.hvaccontroller.packet.Get64ResponsePacket;
import org.chuma.hvaccontroller.packet.Packet;
import org.chuma.hvaccontroller.packet.PacketData;
import org.chuma.hvaccontroller.packet.PacketFactory;
import org.chuma.hvaccontroller.packet.SetPacketRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class HvacDevice {

    static Logger log = LoggerFactory.getLogger(HvacDevice.class.getName());
    private final Collection<IPacketProcessor> processors;
    private final IPacketSource connector;
    private final int thisControllerAddress;
    private final int targetHvacDeviceAddress;

    // HVAC state
    private boolean running;
    private FanSpeed fanSpeed = FanSpeed.NONE;
    private OperatingMode currentMode = OperatingMode.NONE;
    private OperatingMode targetMode = OperatingMode.NONE;
    private boolean autoMode;
    private boolean quiteMode;
    private boolean sleepMode;
    private int targetTemperature;
    private int roomTemperature;
    private int airTemperature;
    private int air2Temperature;
    private boolean defrost;
    private double unitTemperature;

    /**
     * @param portName                   Name of serial port. For example "COM5" or "/dev/ttyUSB0"
     * @param thisControllerAddress      Address of this controller. Usually the main controller has address 0x84 and 0x85
     *                                   or higher is suitable for this.
     * @param targetHvacDeviceAddress    Address of controlled hvac device. Usually the primary hvac device on the bus has address 0x20.
     * @param additionalPacketProcessors Allows adding of custom processors to process all received packets.
     */
    public HvacDevice(String portName, int thisControllerAddress, int targetHvacDeviceAddress, IPacketProcessor[] additionalPacketProcessors) {
        this(new HvacConnector(portName), thisControllerAddress, targetHvacDeviceAddress, additionalPacketProcessors);
    }

    public HvacDevice(IPacketSource packetSource, int thisControllerAddress, int targetHvacDeviceAddress, IPacketProcessor[] additionalProcessors) {
        connector = packetSource;
        this.thisControllerAddress = thisControllerAddress;
        this.targetHvacDeviceAddress = targetHvacDeviceAddress;
        this.processors = new ArrayList<>();
        processors.add(getStateUpdateProcessor());
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

    private IPacketProcessor getStateUpdateProcessor() {
        return new IPacketProcessor() {
            @Override
            public void start() {
                log.info("Starting HvacDevice");
            }

            @Override
            public void stop() {

            }

            @Override
            public void process(Packet packet) {
                if (packet instanceof Get52ResponsePacket) {
                    Get52ResponsePacket get52Resp = (Get52ResponsePacket) packet;
                    running = get52Resp.isOn();
                    fanSpeed = get52Resp.getFanSpeed();
                    currentMode = get52Resp.getMode();
                    autoMode = get52Resp.isModeAuto();
                    targetTemperature = get52Resp.getTargetTemperature();
                    roomTemperature = get52Resp.getRoomTemperature();
                    airTemperature = get52Resp.getAirTemperature();
                    air2Temperature = get52Resp.getAir2Temperature();
                    defrost = get52Resp.isDefrost();
                } else if (packet instanceof Get53ResponsePacket) {
                    Get53ResponsePacket get53Resp = (Get53ResponsePacket) packet;
                    targetMode = get53Resp.getMode();
                    sleepMode = get53Resp.isSleepMode();
                } else if (packet instanceof Get54ResponsePacket) {
                    Get54ResponsePacket get54Resp = (Get54ResponsePacket) packet;
                    quiteMode = get54Resp.isQuite();
                } else if (packet instanceof Get64ResponsePacket) {
                    Get64ResponsePacket get64Resp = (Get64ResponsePacket) packet;
                    unitTemperature = get64Resp.getUnitTemperature();
                }

                if (log.isDebugEnabled()) {
                    if (packet.getCommand() == 0xD1) {
                        log.debug("State: " + HvacDevice.this);
                    }
                }
            }
        };
    }

    public void set(Boolean on, OperatingMode mode, FanSpeed fanSpeed, int temp, Boolean sleep, Boolean quite) {
        SetPacketRequest setPacketRequest = new SetPacketRequest(
                thisControllerAddress,
                targetHvacDeviceAddress,
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

    public int getRoomTemperature() {
        return roomTemperature;
    }

    public int getAirTemperature() {
        return airTemperature;
    }

    public int getAir2Temperature() {
        return air2Temperature;
    }

    public boolean isDefrost() {
        return defrost;
    }

    public double getUnitTemperature() {
        return unitTemperature;
    }

    private int boolAsInt(boolean b) {
        return (b) ? 1 : 0;
    }

    @Override
    public String toString() {
        return String.format("on:%d mode:%s(%s) fan:%s tgtTemp:%d roomTemp:%d airTemp:%d air2Temp:%d auto:%d sleep:%d quite:%d defrost:%d unitTemp:%.2f",
                boolAsInt(running), targetMode, currentMode, fanSpeed, targetTemperature, roomTemperature,
                airTemperature, air2Temperature, boolAsInt(autoMode), boolAsInt(sleepMode), boolAsInt(quiteMode),
                boolAsInt(defrost), unitTemperature);
    }
}
