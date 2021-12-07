package org.chuma.hvaccontroller;

import org.chuma.hvaccontroller.debug.ConsoleOutputWriter;
import org.chuma.hvaccontroller.debug.PacketPrinter;
import org.chuma.hvaccontroller.device.FanSpeed;
import org.chuma.hvaccontroller.device.HvacDevice;
import org.chuma.hvaccontroller.device.OperatingMode;


public class Main {


    public static void main(String[] args) {

        try {
            IPacketProcessor packetPrinter = new PacketPrinter(new ConsoleOutputWriter(), false);
            String portName = isWindows() ? "COM5" : "/dev/ttyUSB0";

            HvacDevice hvacDevice = new HvacDevice(portName, true, new IPacketProcessor[]{packetPrinter});

            String cmd = (args.length > 0) ? args[0] : "test";
            new Thread(() -> schedule(hvacDevice, cmd)).start();

            hvacDevice.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static boolean isWindows() {
        return System.getenv("COMPUTERNAME") != null;
    }

    private static void schedule(HvacDevice device, String cmd) {
        try {
            switch (cmd) {
                case "test":
                    Thread.sleep(5000);
                    device.set(true, OperatingMode.FAN, FanSpeed.SPEED_1, 25, false, false);
                    Thread.sleep(10000);
                    device.set(true, OperatingMode.FAN, FanSpeed.SPEED_2, 25, false, false);
                    Thread.sleep(10000);
                    device.set(false, OperatingMode.HEAT, FanSpeed.SPEED_1, 23, false, false);
                    break;
                case "on":
                    Thread.sleep(2000);
                    device.set(true, OperatingMode.HEAT, FanSpeed.SPEED_1, 23, false, false);
                    break;
                case "on4":
                    Thread.sleep(2000);
                    device.set(true, OperatingMode.HEAT, FanSpeed.SPEED_2, 23, false, false);
                    break;

                case "on5":
                    Thread.sleep(2000);
                    device.set(true, OperatingMode.HEAT, FanSpeed.SPEED_3, 23, false, false);
                    break;

                case "off":
                    Thread.sleep(2000);
                    device.set(false, OperatingMode.HEAT, FanSpeed.SPEED_1, 23, false, false);
                    break;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
