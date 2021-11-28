package org.chuma.hvaccontroller.packet;

public class PacketFactory {
    public static Packet Deserialize(PacketData data) {
        if (data.command == PacketType.CMD_SET) {
            return new SetPacketRequest(data);
        }
        if (data.command == PacketType.CMD_SET_RESPONSE) {
            return new SetPacketResponse(data);
        }
        if (data.command == PacketType.CMD_GET_52 && !data.isRequest()) {
            return new Get52ResponsePacket(data);
        }
        if (data.command == PacketType.CMD_GET_53 && !data.isRequest()) {
            return new Get53ResponsePacket(data);
        }
        if (data.command == PacketType.CMD_GET_54 && !data.isRequest()) {
            return new Get54ResponsePacket(data);
        }
        return new UnknownPacket(data);
    }
}
