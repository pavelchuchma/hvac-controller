package org.chuma.hvaccontroller;

import org.chuma.hvaccontroller.packet.PacketData;
import org.junit.Assert;
import org.junit.Test;

public class PacketDataTest {
    @Test
    public void testName() throws Exception {
        {
            PacketData p = new PacketData(0x84, 0x20, 0x52, new int[8]);
            int[] expectedData = new int[]{0x32, 0x84, 0x20, 0x52, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xF6, 0x34};
            Assert.assertArrayEquals(expectedData, p.rawData);
        }
        {
            PacketData p = new PacketData(0x84, 0x20, 0xA0, new int[]{0x1F, 0x18, 0x55, 0x04, 0xF4, 0x00, 0x00, 0x00});
            int[] expectedData = new int[]{0x32, 0x84, 0x20, 0xA0, 0x1F, 0x18, 0x55, 0x04, 0xF4, 0x00, 0x00, 0x00, 0xA6, 0x34};
            Assert.assertArrayEquals(expectedData, p.rawData);
        }
    }
}