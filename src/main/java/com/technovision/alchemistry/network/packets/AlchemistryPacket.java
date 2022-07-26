package com.technovision.alchemistry.network.packets;

import net.minecraft.network.PacketByteBuf;

/**
 * A packet send by the AlchemistyNetwork
 *
 * @author TechnoVision
 */
public interface AlchemistryPacket {

    /**
     * Updates class data members with values from a PacketByteBuf.
     * @param buffer the PacketByteBuf to grab values from.
     */
    void encode(PacketByteBuf buffer);

    /**
     * Creates a PacketByteBuf with class data members.
     * @return PacketByteBuf with class data members.
     */
    PacketByteBuf toByteBuf();
}
