package com.fuzs.enchantinginfuser.network.message;

import com.fuzs.enchantinginfuser.world.inventory.ContainerInfuser;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageEnchant implements IMessage {

    private boolean isRepair;

    public MessageEnchant() {}

    public MessageEnchant(boolean isRepair) {
        this.isRepair = isRepair;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.isRepair = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(this.isRepair);
    }

    public static class Handler implements IMessageHandler<MessageEnchant, IMessage> {
        @Override
        public IMessage onMessage(MessageEnchant message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> {
                if (player.openContainer instanceof ContainerInfuser) {
                    ContainerInfuser container = (ContainerInfuser) player.openContainer;
                    if (message.isRepair) {
                        container.tileEntity.repairItem(player);
                    } else {
                        container.tileEntity.enchantItem(player);
                    }
                    container.getSlot(0).onSlotChanged();
                    container.detectAndSendChanges();
                    player.sendContainerToPlayer(container);
                }
            });
            return null;
        }
    }
}