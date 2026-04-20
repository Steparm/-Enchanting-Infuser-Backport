package com.fuzs.enchantinginfuser.core;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;

public class CommonAbstractionsImpl implements CommonAbstractions {

    @Override
    public boolean canApplyAtEnchantingTable(Enchantment enchantment, ItemStack stack) {

        return enchantment.canApply(stack);
    }

    @Override
    public boolean isAllowedOnBooks(Enchantment enchantment) {
        return enchantment.type != null;
    }
}