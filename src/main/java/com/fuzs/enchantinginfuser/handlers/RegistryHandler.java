package com.fuzs.enchantinginfuser.handlers;

import com.fuzs.enchantinginfuser.EnchantingInfuserBackport;
import com.fuzs.enchantinginfuser.world.level.block.BlockInfuser;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod.EventBusSubscriber(modid = EnchantingInfuserBackport.MODID)
public class RegistryHandler {
    
    public static final Block INFUSER = new BlockInfuser(false)
            .setRegistryName("enchanting_infuser")
            .setTranslationKey(EnchantingInfuserBackport.MODID + ".enchanting_infuser")
            .setCreativeTab(EnchantingInfuserBackport.tabEnchantingInfuser);

    public static final Block ADVANCED_INFUSER = new BlockInfuser(true)
            .setRegistryName("advanced_enchanting_infuser")
            .setTranslationKey(EnchantingInfuserBackport.MODID + ".advanced_enchanting_infuser")
            .setCreativeTab(EnchantingInfuserBackport.tabEnchantingInfuser);

    @SubscribeEvent
    public static void onBlockRegister(RegistryEvent.Register<Block> event) {
        event.getRegistry().registerAll(INFUSER, ADVANCED_INFUSER);
    }

    @SubscribeEvent
    public static void onItemRegister(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(new ItemBlock(INFUSER).setRegistryName(INFUSER.getRegistryName()));
        event.getRegistry().register(new ItemBlock(ADVANCED_INFUSER).setRegistryName(ADVANCED_INFUSER.getRegistryName()));
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        registerModel(Item.getItemFromBlock(INFUSER));
        registerModel(Item.getItemFromBlock(ADVANCED_INFUSER));
    }

    @SideOnly(Side.CLIENT)
    private static void registerModel(Item item) {
        ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
    }
}