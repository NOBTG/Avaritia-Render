package com.lolipickaxe.Avaritia;

import codechicken.lib.texture.SpriteRegistryHelper;
import com.lolipickaxe.LoliPickaxeMod;
import net.covers1624.quack.util.CrashLock;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

public class AvaritiaTextures {
    private static final CrashLock LOCK = new CrashLock("Already Initialized");

    private static final SpriteRegistryHelper SPRITE_HELPER = new SpriteRegistryHelper();

    public static TextureAtlasSprite[] COSMIC_SPRITES = new TextureAtlasSprite[10];

    public static TextureAtlasSprite COSMIC_0;

    public static TextureAtlasSprite COSMIC_1;

    public static TextureAtlasSprite COSMIC_2;

    public static TextureAtlasSprite COSMIC_3;

    public static TextureAtlasSprite COSMIC_4;

    public static TextureAtlasSprite COSMIC_5;

    public static TextureAtlasSprite COSMIC_6;

    public static TextureAtlasSprite COSMIC_7;

    public static TextureAtlasSprite COSMIC_8;

    public static TextureAtlasSprite COSMIC_9;

    public static void init() {
        LOCK.lock();
        SPRITE_HELPER.addIIconRegister(registrar -> {
            registrar.registerSprite(shader("cosmic_0"));
            registrar.registerSprite(shader("cosmic_1"));
            registrar.registerSprite(shader("cosmic_2"));
            registrar.registerSprite(shader("cosmic_3"));
            registrar.registerSprite(shader("cosmic_4"));
            registrar.registerSprite(shader("cosmic_5"));
            registrar.registerSprite(shader("cosmic_6"));
            registrar.registerSprite(shader("cosmic_7"));
            registrar.registerSprite(shader("cosmic_8"));
            registrar.registerSprite(shader("cosmic_9"));
        });
    }

    private static ResourceLocation shader(String path) {
        return new ResourceLocation(LoliPickaxeMod.MODID, "shader/" + path);
    }
}