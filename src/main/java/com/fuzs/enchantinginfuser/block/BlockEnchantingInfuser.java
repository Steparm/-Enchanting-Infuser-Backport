
package com.fuzs.enchantinginfuser.blocks;

import com.fuzs.enchantinginfuser.EnchantingInfuserBackport;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;

public class BlockEnchantingInfuser extends Block {
    public BlockEnchantingInfuser(String name) {
        super(Material.ROCK);
        setRegistryName(name);
        setTranslationKey(EnchantingInfuserBackport.MODID + "." + name);
        setSoundType(SoundType.STONE);
        setHardness(1.0F);
        setResistance(10.0F);
        setCreativeTab(EnchantingInfuserBackport.tabEnchantingInfuser);
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }
}