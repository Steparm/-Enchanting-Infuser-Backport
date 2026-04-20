package com.fuzs.enchantinginfuser.api.world.item.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.block.state.IBlockState;

public interface EnchantStatsProvider {
    EnchantStatsProvider INSTANCE = new EnchantStatsProviderImpl();

    String getSourceNamespace();

    default int getPriority() {
        return 10;
    }

    String[] getScalingNamespaces();

    float getEnchantPowerBonus(IBlockState state, World world, BlockPos pos);

    Enchantment.Rarity getRarity(Enchantment enchantment);

    boolean isCompatibleWith(Enchantment enchantment, Enchantment other);

    int getMinLevel(Enchantment enchantment);

    int getMaxLevel(Enchantment enchantment);

    int getMinCost(Enchantment enchantment, int level);

    int getMaxCost(Enchantment enchantment, int level);

    boolean isTreasureOnly(Enchantment enchantment);

    boolean isCurse(Enchantment enchantment);
    
    boolean isTradeable(Enchantment enchantment);
    
    boolean isDiscoverable(Enchantment enchantment);
}