package com.fuzs.enchantinginfuser.api;

import com.fuzs.enchantinginfuser.api.world.item.enchantment.EnchantStatsProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class EnchantingInfuserAPI {
    
public static final Logger LOGGER = LogManager.getLogger("Enchanting Infuser API");


    private static EnchantStatsProvider enchantStatsProvider = EnchantStatsProvider.INSTANCE;

    public static synchronized boolean setEnchantStatsProvider(EnchantStatsProvider provider) {
        if (provider == null) throw new IllegalStateException("Provider cannot be null!");
        
        if (enchantStatsProvider != null) {
            if (provider != EnchantStatsProvider.INSTANCE && provider.getPriority() <= enchantStatsProvider.getPriority()) {
                return false;
            }
        }
        
        enchantStatsProvider = provider;
        LOGGER.info("Set new EnchantStatsProvider for mod {}", provider.getSourceNamespace());
        return true;
    }

    public static EnchantStatsProvider getEnchantStatsProvider() {
        if (enchantStatsProvider == null) throw new IllegalStateException("EnchantStatsProvider is not initialized!");
        return enchantStatsProvider;
    }
}