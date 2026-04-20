package com.fuzs.enchantinginfuser.api.world.item.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.block.state.IBlockState;

class EnchantStatsProviderImpl implements EnchantStatsProvider {

    @Override
    public String getSourceNamespace() {
        return "minecraft";
    }

    @Override
    public int getPriority() {
        return -1;
    }

    @Override
    public String[] getScalingNamespaces() {
        return new String[]{"minecraft"};
    }

    @Override
    public float getEnchantPowerBonus(IBlockState state, World world, BlockPos pos) {
        return state.getBlock().getEnchantPowerBonus(world, pos);
    }

    @Override
    public Enchantment.Rarity getRarity(Enchantment enchantment) {
        return enchantment.getRarity();
    }

    @Override
    public boolean isCompatibleWith(Enchantment enchantment, Enchantment other) {
        return enchantment.isCompatibleWith(other);
    }

    @Override
    public int getMinLevel(Enchantment enchantment) {
        return enchantment.getMinLevel();
    }

    @Override
    public int getMaxLevel(Enchantment enchantment) {
        return enchantment.getMaxLevel();
    }

    @Override
    public int getMinCost(Enchantment enchantment, int level) {
        return enchantment.getMinEnchantability(level);
    }

    @Override
    public int getMaxCost(Enchantment enchantment, int level) {
        return enchantment.getMaxEnchantability(level);
    }

    @Override
    public boolean isTreasureOnly(Enchantment enchantment) {
        return enchantment.isTreasureEnchantment();
    }

    @Override
    public boolean isCurse(Enchantment enchantment) {
        return enchantment.isCurse();
    }

    @Override
    public boolean isTradeable(Enchantment enchantment) {
        return !enchantment.isTreasureEnchantment();
    }

    @Override
    public boolean isDiscoverable(Enchantment enchantment) {
        return true;
    }
}