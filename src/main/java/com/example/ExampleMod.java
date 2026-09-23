package com.example;

import net.fabricmc.api.ModInitializer;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExampleMod implements ModInitializer {
    public static final String MOD_ID = "modid";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // コントローラーアイテムの定義
    public static final Item CONTROLLER_ITEM = new ControllerItem(new Item.Settings().maxCount(1));

    @Override
    public void onInitialize() {
        // ゲーム内にアイテム「controller」として登録
        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "controller"), CONTROLLER_ITEM);
        LOGGER.info("Mob Controller Mod Initialized!");
    }
}
