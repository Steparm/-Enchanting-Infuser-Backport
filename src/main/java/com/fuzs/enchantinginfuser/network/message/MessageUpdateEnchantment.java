package com.fuzs.enchantinginfuser.network.message;

import com.fuzs.enchantinginfuser.world.inventory.ContainerInfuser;
import io.netty.buffer.ByteBuf;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageUpdateEnchantment implements IMessage {

    private int enchantmentId;
    private int level;

    public MessageUpdateEnchantment() {}

    public MessageUpdateEnchantment(Enchantment ench, int level) {
        this.enchantmentId = Enchantment.getEnchantmentID(ench);
        this.level = level;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.enchantmentId = buf.readInt();
        this.level = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.enchantmentId);
        buf.writeInt(this.level);
    }

    public static class Handler implements IMessageHandler<MessageUpdateEnchantment, IMessage> {
        @Override
        public IMessage onMessage(MessageUpdateEnchantment message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> {
                if (player.openContainer instanceof ContainerInfuser) {
                    Enchantment ench = Enchantment.getEnchantmentByID(message.enchantmentId);
                    if (ench != null) {
                        ContainerInfuser container = (ContainerInfuser) player.openContainer;
                        container.tileEntity.updateEnchantment(ench, message.level);
                    }
                }
            });
            return null;
        }
    }
}