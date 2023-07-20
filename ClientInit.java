package com.lolipickaxe.Avaritia;

import codechicken.lib.model.ModelRegistryHelper;
import net.covers1624.quack.util.CrashLock;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class ClientInit {
    private static final CrashLock LOCK = new CrashLock("Already Initialized.");

    public static void init() {
        LOCK.lock();
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        onModelRegistryEvent(new ModelRegistryHelper(modEventBus));
        AvaritiaTextures.init();
        AvaritiaShaders.init();
    }

    private static void onModelRegistryEvent(ModelRegistryHelper event) {
        event.register((ModelResourceLocation) HaloItemModelLoader.ID, (BakedModel)HaloItemModelLoader.INSTANCE);
        event.register((ModelResourceLocation) CosmicModelLoader.ID, (BakedModel) CosmicModelLoader.INSTANCE);
    }
}
