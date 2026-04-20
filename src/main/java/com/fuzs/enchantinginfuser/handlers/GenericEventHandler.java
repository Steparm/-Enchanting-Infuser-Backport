package com.fuzs.enchantinginfuser.handlers;

import com.fuzs.enchantinginfuser.EnchantingInfuserBackport;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class GenericEventHandler {
    private int messageCountdown = 10;

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        Entity entity = event.player;
        if (!entity.world.isRemote && entity instanceof EntityPlayerMP) {
            EnchantingInfuserBackport.LOGGER.info("Player logged in: " + entity.getName());
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.player instanceof EntityPlayerMP && this.messageCountdown != 0) {
            this.messageCountdown--;
        }
    }
}