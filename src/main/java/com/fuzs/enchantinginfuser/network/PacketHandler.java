package com.fuzs.enchantinginfuser.network;

import com.fuzs.enchantinginfuser.EnchantingInfuserBackport;
import com.fuzs.enchantinginfuser.network.message.MessageEnchant;
import com.fuzs.enchantinginfuser.network.message.MessageUpdateEnchantment;
import net.minecraftforge.fml.relauncher.Side;

public class PacketHandler {
    
    public static void registerMessages() {
        int id = 0;
        EnchantingInfuserBackport.NETWORK.registerMessage(MessageEnchant.Handler.class, MessageEnchant.class, id++, Side.SERVER);
        EnchantingInfuserBackport.NETWORK.registerMessage(MessageUpdateEnchantment.Handler.class, MessageUpdateEnchantment.class, id++, Side.SERVER);
    }
}