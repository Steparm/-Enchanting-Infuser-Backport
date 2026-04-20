package com.fuzs.enchantinginfuser.handlers;

import com.fuzs.enchantinginfuser.client.gui.screens.inventory.GuiInfuser;
import com.fuzs.enchantinginfuser.tileentity.TileEntityInfuser;
import com.fuzs.enchantinginfuser.world.inventory.ContainerInfuser;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler {
    
    public static final int INFUSER_GUI = 0;
    public static final int ADVANCED_INFUSER_GUI = 1;
    
    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        TileEntity te = world.getTileEntity(pos);
        
        if (te instanceof TileEntityInfuser) {
            boolean isAdvanced = (ID == ADVANCED_INFUSER_GUI);
            return new ContainerInfuser(player.inventory, world, pos, isAdvanced);
        }
        return null;
    }
    
    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        TileEntity te = world.getTileEntity(pos);
        
        if (te instanceof TileEntityInfuser) {
            boolean isAdvanced = (ID == ADVANCED_INFUSER_GUI);
            return new GuiInfuser(player.inventory, world, pos, isAdvanced);
        }
        return null;
    }
}