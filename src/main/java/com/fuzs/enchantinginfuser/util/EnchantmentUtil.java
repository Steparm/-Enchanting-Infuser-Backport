package com.fuzs.enchantinginfuser.util;

import com.fuzs.enchantinginfuser.api.EnchantingInfuserAPI;
import com.google.common.collect.Lists;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Items;
import net.minecraft.item.ItemEnchantedBook;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.List;
import java.util.Map;

public class EnchantmentUtil {

    public static List<Enchantment> getAvailableEnchantments(ItemStack stack, boolean allowAnvil, boolean allowTreasure, boolean allowUndiscoverable, boolean allowUntradeable, boolean allowCurse) {
        List<Enchantment> list = Lists.newArrayList();
        boolean isBook = stack.getItem() == Items.BOOK || stack.getItem() == Items.ENCHANTED_BOOK;

        for (Enchantment enchantment : Enchantment.REGISTRY) {
            if ((allowAnvil ? enchantment.canApply(stack) : enchantment.type.canEnchantItem(stack.getItem())) || (isBook && enchantment.isAllowedOnBooks())) {
                if (!enchantment.isTreasureEnchantment() || allowTreasure) {
                    list.add(enchantment);
                }
            }
        }
        return list;
    }

    public static ItemStack applyEnchantments(ItemStack stack, Map<Enchantment, Integer> enchantmentsToLevel, boolean increaseRepairCost) {
        boolean isEnchanted = !enchantmentsToLevel.isEmpty();
        ItemStack newStack = getNewEnchantmentStack(stack, isEnchanted, true);
        
        if (newStack.getTagCompound() != null) {
            newStack.getTagCompound().removeTag("ench");
            newStack.getTagCompound().removeTag("StoredEnchantments");
        }

        EnchantmentHelper.setEnchantments(enchantmentsToLevel, newStack);
        
        if (increaseRepairCost && newStack.isItemEnchanted()) {
            newStack.setRepairCost(stack.getRepairCost() * 2 + 1);
        }
        return newStack;
    }

    public static ItemStack getNewEnchantmentStack(ItemStack stack, boolean enchanted, boolean copyResult) {
        ItemStack newStack;
        if (stack.getItem() == Items.ENCHANTED_BOOK && !enchanted) {
            newStack = new ItemStack(Items.BOOK);
        } else if (stack.getItem() == Items.BOOK && enchanted) {
            newStack = new ItemStack(Items.ENCHANTED_BOOK);
        } else {
            return copyResult ? stack.copy() : stack;
        }
        
        if (stack.hasTagCompound()) {
            newStack.setTagCompound(stack.getTagCompound().copy());
        }
        return newStack;
    }

    public static ITextComponent getPlainEnchantmentName(Enchantment enchantment, int level) {
        ITextComponent s = new TextComponentTranslation(enchantment.getName());
        if (level != 1 || enchantment.getMaxLevel() != 1) {
            s.appendText(" ").appendSibling(new TextComponentTranslation("enchantment.level." + level));
        }
        return s;
    }
}