package com.fuzs.enchantinginfuser;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fuzs.enchantinginfuser.handlers.RegistryHandler;
import com.fuzs.enchantinginfuser.network.PacketHandler;
import com.fuzs.enchantinginfuser.proxy.CommonProxy;
import com.fuzs.enchantinginfuser.handlers.GuiHandler;
import com.fuzs.enchantinginfuser.tileentity.TileEntityInfuser;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.GameRegistry;

@Mod(modid = EnchantingInfuserBackport.MODID, version = EnchantingInfuserBackport.VERSION)
public class EnchantingInfuserBackport {
    
    public static final String MODID = "enchantinginfuserbackport";
    public static final String VERSION = "1.1.0";
    public static final Logger LOGGER = LogManager.getLogger(MODID);
    
    public static SimpleNetworkWrapper NETWORK;
    
    @Mod.Instance(MODID)
    public static EnchantingInfuserBackport instance;
    
    @SidedProxy(clientSide = "com.fuzs.enchantinginfuser.proxy.ClientProxy", serverSide = "com.fuzs.enchantinginfuser.proxy.CommonProxy")
    public static CommonProxy proxy;
    
    public static CreativeTabs tabEnchantingInfuser = new CreativeTabs("tabEnchantingInfuser") {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(Item.getItemFromBlock(RegistryHandler.INFUSER));
        }
    };
    
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LOGGER.info("PreInitialization started");
        
        NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel(MODID);
        PacketHandler.registerMessages();
        
        GameRegistry.registerTileEntity(TileEntityInfuser.class, MODID + ":infuser_tile");
        NetworkRegistry.INSTANCE.registerGuiHandler(instance, new GuiHandler());
        
        proxy.preInit(event);
    }
    
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        LOGGER.info("Initialization started");
        proxy.init(event);
    }
    
    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        LOGGER.info("PostInitialization started");
        proxy.postInit(event);
    }
}