package com.fuzs.enchantinginfuser.config;

import com.google.gson.*;
import com.fuzs.enchantinginfuser.EnchantingInfuserBackport;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

public class ModConfig {
    
    private static final Path CONFIG_PATH = Paths.get("config", EnchantingInfuserBackport.MODID);
    private static final Path BOOKSHELF_FILE = CONFIG_PATH.resolve("bookshelf_blocks.json");
    private static final Path ENCHANTMENT_BLACKLIST_FILE = CONFIG_PATH.resolve("enchantment_blacklist.json");
    
    public static Set<Block> validBookshelves = new HashSet<>();
    
    public static Set<ResourceLocation> enchantmentBlacklist = new HashSet<>();
    public static Set<ResourceLocation> advancedEnchantmentBlacklist = new HashSet<>();
    
    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();
    
    public static void loadConfig() {
        try {
            Files.createDirectories(CONFIG_PATH);
        } catch (IOException e) {
            EnchantingInfuserBackport.LOGGER.error("Failed to create config folder!", e);
        }
        
        loadBookshelves();
        loadEnchantmentBlacklists();
    }
    
    private static void loadBookshelves() {
        if (!Files.exists(BOOKSHELF_FILE)) {
            createDefaultBookshelfConfig();
        }
        
        try (Reader reader = new FileReader(BOOKSHELF_FILE.toFile())) {
            JsonObject json = gson.fromJson(reader, JsonObject.class);
            
            if (json.has("bookshelves") && json.get("bookshelves").isJsonArray()) {
                JsonArray bookshelves = json.getAsJsonArray("bookshelves");
                
                validBookshelves.clear();
                
                for (JsonElement element : bookshelves) {
                    String blockName = element.getAsString();
                    Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockName));
                    
                    if (block != null && block != Blocks.AIR) {
                        validBookshelves.add(block);
                        EnchantingInfuserBackport.LOGGER.info("Added bookshelf: " + blockName);
                    } else {
                        EnchantingInfuserBackport.LOGGER.warn("Block not found: " + blockName);
                    }
                }
                
                EnchantingInfuserBackport.LOGGER.info("Loaded " + validBookshelves.size() + " valid bookshelves");
            } else {
                EnchantingInfuserBackport.LOGGER.error("Config missing 'bookshelves' section! Using default.");
                loadDefaultBookshelves();
            }
            
        } catch (Exception e) {
            EnchantingInfuserBackport.LOGGER.error("Error loading bookshelf config!", e);
            loadDefaultBookshelves();
        }
    }
    
    private static void createDefaultBookshelfConfig() {
        JsonObject json = new JsonObject();
        json.addProperty("description", "Blocks that count as bookshelves for the enchanting infuser");
        json.addProperty("notes", "Format: 'modid:block_name'. Restart game after changing.");
        
        JsonArray bookshelves = new JsonArray();
        bookshelves.add("minecraft:bookshelf");
        
        json.add("bookshelves", bookshelves);
        
        try (Writer writer = new FileWriter(BOOKSHELF_FILE.toFile())) {
            gson.toJson(json, writer);
            EnchantingInfuserBackport.LOGGER.info("Created default bookshelf config: " + BOOKSHELF_FILE);
        } catch (IOException e) {
            EnchantingInfuserBackport.LOGGER.error("Failed to create default bookshelf config!", e);
        }
    }
    
    private static void loadDefaultBookshelves() {
        validBookshelves.clear();
        validBookshelves.add(Blocks.BOOKSHELF);
        EnchantingInfuserBackport.LOGGER.info("Loaded default bookshelves (vanilla only)");
    }
    
    private static void loadEnchantmentBlacklists() {
        if (!Files.exists(ENCHANTMENT_BLACKLIST_FILE)) {
            createDefaultEnchantmentBlacklistConfig();
        }
        
        enchantmentBlacklist.clear();
        advancedEnchantmentBlacklist.clear();
        
        try (Reader reader = new FileReader(ENCHANTMENT_BLACKLIST_FILE.toFile())) {
            JsonObject json = gson.fromJson(reader, JsonObject.class);
            
            if (json.has("enchanting_infuser") && json.get("enchanting_infuser").isJsonArray()) {
                JsonArray arr = json.getAsJsonArray("enchanting_infuser");
                for (JsonElement el : arr) {
                    ResourceLocation rl = parseResourceLocation(el.getAsString());
                    if (rl != null) {
                        enchantmentBlacklist.add(rl);
                    }
                }
            }
            
            if (json.has("advanced_enchanting_infuser") && json.get("advanced_enchanting_infuser").isJsonArray()) {
                JsonArray arr = json.getAsJsonArray("advanced_enchanting_infuser");
                for (JsonElement el : arr) {
                    ResourceLocation rl = parseResourceLocation(el.getAsString());
                    if (rl != null) {
                        advancedEnchantmentBlacklist.add(rl);
                    }
                }
            }
            
            EnchantingInfuserBackport.LOGGER.info("Loaded " + enchantmentBlacklist.size() + " entries in enchanting_infuser blacklist");
            EnchantingInfuserBackport.LOGGER.info("Loaded " + advancedEnchantmentBlacklist.size() + " entries in advanced_enchanting_infuser blacklist");
            
        } catch (Exception e) {
            EnchantingInfuserBackport.LOGGER.error("Error loading enchantment blacklist!", e);
        }
    }
    
    private static void createDefaultEnchantmentBlacklistConfig() {
        JsonObject json = new JsonObject();
        json.addProperty("description", "Enchantments that are hidden and unavailable in each infuser");
        json.addProperty("notes", "Format: 'modid:enchantment_name' (e.g. 'minecraft:mending'). Empty list = all enchantments allowed. Restart game after changing.");
        
        json.add("enchanting_infuser", new JsonArray());
        json.add("advanced_enchanting_infuser", new JsonArray());
        
        try (Writer writer = new FileWriter(ENCHANTMENT_BLACKLIST_FILE.toFile())) {
            gson.toJson(json, writer);
            EnchantingInfuserBackport.LOGGER.info("Created default enchantment blacklist config: " + ENCHANTMENT_BLACKLIST_FILE);
        } catch (IOException e) {
            EnchantingInfuserBackport.LOGGER.error("Failed to create default enchantment blacklist config!", e);
        }
    }
    
    private static ResourceLocation parseResourceLocation(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        s = s.trim();
        if (!s.contains(":")) {
            s = "minecraft:" + s;
        }
        try {
            return new ResourceLocation(s);
        } catch (Exception e) {
            EnchantingInfuserBackport.LOGGER.warn("Invalid enchantment id in blacklist: " + s);
            return null;
        }
    }
    
    public static boolean isEnchantmentBlacklisted(Enchantment ench, boolean advanced) {
        if (ench == null) return false;
        ResourceLocation rl = ench.getRegistryName();
        if (rl == null) return false;
        return advanced ? advancedEnchantmentBlacklist.contains(rl) : enchantmentBlacklist.contains(rl);
    }
    
    public static void reload() {
        EnchantingInfuserBackport.LOGGER.info("Reloading config...");
        loadConfig();
    }
}