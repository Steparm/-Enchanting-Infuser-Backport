package com.fuzs.enchantinginfuser.network.message;

import com.fuzs.enchantinginfuser.world.inventory.ContainerInfuser;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageEnchant implements IMessage {

    public MessageEnchant() {}

    @Override
    public void fromBytes(ByteBuf buf) {}

    @Override
    public void toBytes(ByteBuf buf) {}

    public static class Handler implements IMessageHandler<MessageEnchant, IMessage> {
        @Override
        public IMessage onMessage(MessageEnchant message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> {
                if (player.openContainer instanceof ContainerInfuser) {
                    ContainerInfuser container = (ContainerInfuser) player.openContainer;
                    container.tileEntity.enchantItem(player);
                }
            });
            return null;
        }
    }
}