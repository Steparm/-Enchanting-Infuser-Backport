package com.fuzs.enchantinginfuser.core;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;

public interface CommonAbstractions {
    CommonAbstractions INSTANCE = new com.fuzs.enchantinginfuser.core.CommonAbstractionsImpl();

    boolean canApplyAtEnchantingTable(Enchantment enchantment, ItemStack stack);

    boolean isAllowedOnBooks(Enchantment enchantment);
}