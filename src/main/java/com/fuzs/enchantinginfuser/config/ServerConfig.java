package com.fuzs.enchantinginfuser.config;

import com.fuzs.enchantinginfuser.EnchantingInfuserBackport;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemHoe;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = EnchantingInfuserBackport.MODID, name = "enchanting_infuser_server")
@Mod.EventBusSubscriber(modid = EnchantingInfuserBackport.MODID)
public class ServerConfig {

    @Config.Name("Normal Infuser Settings")
    public static InfuserConfig normalInfuser = new InfuserConfig();

    @Config.Name("Advanced Infuser Settings")
    public static InfuserConfig advancedInfuser = new InfuserConfig();

    static {

        advancedInfuser.allowRepairing = AllowedRepairItems.TOOLS_AND_ARMOR;
        advancedInfuser.allowBooks = true;
        advancedInfuser.allowModifyingEnchantments = ModifiableItems.ALL;
        advancedInfuser.costs.maximumCost = 20;
        advancedInfuser.types.allowAnvilEnchantments = true;
    }

    public static class InfuserConfig {
        @Config.Name("Allow Books")
        @Config.Comment("If true, books can be enchanted in the infuser.")
        public boolean allowBooks = false;
        
        @Config.Name("Allow Repairing")
        @Config.Comment("Defines which items can be repaired. NONE = no repairing, TOOLS = only tools, ARMOR = only armor, TOOLS_AND_ARMOR = tools and armor.")
        public AllowedRepairItems allowRepairing = AllowedRepairItems.NONE;
        
        @Config.Name("Allow Modifying Enchantments")
        @Config.Comment("Defines which items can have their enchantments modified. UNENCHANTED = only unenchanted items, ALL = all enchantable items.")
        public ModifiableItems allowModifyingEnchantments = ModifiableItems.UNENCHANTED;
        
        @Config.Name("Costs")
        @Config.Comment("Configuration for enchantment costs.")
        public CostsConfig costs = new CostsConfig();
        
        @Config.Name("Types")
        @Config.Comment("Configuration for allowed enchantment types.")
        public TypesConfig types = new TypesConfig();
    }

    public static class CostsConfig {
        @Config.Name("Maximum Cost")
        @Config.Comment("The maximum number of experience levels an enchantment can cost.")
        @Config.RangeInt(min = 1, max = 100)
        public int maximumCost = 30;
        
        @Config.Name("Cost Multiplier")
        @Config.Comment("Multiplier applied to the base cost of enchantments.")
        @Config.RangeDouble(min = 0.1, max = 10.0)
        public double costMultiplier = 1.0;
        
        @Config.Name("Repair Cost Multiplier")
        @Config.Comment("Multiplier applied to the cost of repairing items.")
        @Config.RangeDouble(min = 0.1, max = 10.0)
        public double repairCostMultiplier = 0.5;
    }

    public static class TypesConfig {
        @Config.Name("Allow Anvil Enchantments")
        @Config.Comment("If true, enchantments normally only available through anvils can be applied.")
        public boolean allowAnvilEnchantments = false;
        
        @Config.Name("Allow Undiscoverable Enchantments")
        @Config.Comment("If true, enchantments that cannot be discovered in the enchanting table can be applied.")
        public boolean allowUndiscoverableEnchantments = false;
        
        @Config.Name("Allow Untradeable Enchantments")
        @Config.Comment("If true, enchantments that cannot be obtained from villagers can be applied.")
        public boolean allowUntradeableEnchantments = false;
        
        @Config.Name("Allow Curse Enchantments")
        @Config.Comment("If true, curse enchantments can be applied.")
        public boolean allowCursesEnchantments = false;
        
        @Config.Name("Allow Treasure Enchantments")
        @Config.Comment("If true, treasure enchantments (like Mending) can be applied.")
        public boolean allowTreasureEnchantments = false;
    }

    public enum AllowedRepairItems {
        NONE, TOOLS, ARMOR, TOOLS_AND_ARMOR;

        public boolean test(ItemStack stack) {
            if (stack.isEmpty()) return false;
            boolean isTool = stack.getItem() instanceof ItemTool || stack.getItem() instanceof ItemSword || stack.getItem() instanceof ItemHoe;
            boolean isArmor = stack.getItem() instanceof ItemArmor;

            switch (this) {
                case TOOLS: return isTool;
                case ARMOR: return isArmor;
                case TOOLS_AND_ARMOR: return isTool || isArmor;
                default: return false;
            }
        }
    }

    public enum ModifiableItems {
        UNENCHANTED, ALL;

        public boolean test(ItemStack stack) {
            if (stack.isEmpty()) return false;
            if (this == ALL) return stack.getItem().isEnchantable(stack) || stack.isItemEnchanted();
            return stack.getItem().isEnchantable(stack) && !stack.isItemEnchanted();
        }
    }

    @SubscribeEvent
    public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equals(EnchantingInfuserBackport.MODID)) {
            ConfigManager.sync(EnchantingInfuserBackport.MODID, Config.Type.INSTANCE);
        }
    }
}