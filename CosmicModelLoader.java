package com.lolipickaxe.Avaritia;

import com.lolipickaxe.LoliPickaxeMod;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

public class CosmicModelLoader implements UnbakedModel {

    public static final ResourceLocation ID = new ResourceLocation(LoliPickaxeMod.MODID, "cosmic");

    private final BlockModel baseModel = (BlockModel) Minecraft.getInstance().getModelManager().getModel(ID);
    private final String maskTexture;

    public CosmicModelLoader() {
        this.maskTexture = String.valueOf(ID);
    }

    @Override
    public @NotNull Collection<ResourceLocation> getDependencies() {
        Set<ResourceLocation> arrayList = new HashSet<>();
        arrayList.add(ID);
        return arrayList;
    }

    @Override
    public @NotNull Collection<Material> getMaterials(@NotNull Function<ResourceLocation, UnbakedModel> p_119538_, @NotNull Set<Pair<String, String>> p_119539_) {
        Set<Material> arrayList = new HashSet<>();
        Material maskMaterial = null;
        if (maskTexture != null) {
            ResourceLocation maskLocation = new ResourceLocation(maskTexture);
            UnbakedModel unbakedModel = p_119538_.apply(maskLocation);
            if (unbakedModel instanceof BlockModel maskModel) {
                maskMaterial = maskModel.getMaterial("particle");
                if (Objects.equals(maskMaterial.texture(), MissingTextureAtlasSprite.getLocation()))
                    p_119539_.add(Pair.of(maskTexture, "unknown"));
            }
        }
        if (maskMaterial != null)
            arrayList.add(maskMaterial);
        return arrayList;
    }

    @Nullable
    @Override
    public BakedModel bake(@NotNull ModelBakery p_119534_, @NotNull Function<Material, TextureAtlasSprite> p_119535_, @NotNull ModelState p_119536_, @NotNull ResourceLocation p_119537_) {
        return (BakedModel) (new CosmicModelGeometry(baseModel, maskTexture)).baseModel;
    }

    public static class CosmicModelGeometry implements IUnbakedGeometry<CosmicModelLoader.CosmicModelGeometry> {
        final BlockModel baseModel;
        private final String maskTexture;
        private Material maskMaterial;

        public CosmicModelGeometry(BlockModel baseModel, String maskTexture) {
            this.baseModel = baseModel;
            this.maskTexture = maskTexture;
        }

        public BakedModel bake(@NotNull IGeometryBakingContext owner, @NotNull ModelBakery bakery, @NotNull Function<Material, TextureAtlasSprite> spriteGetter, @NotNull ModelState modelTransform, @NotNull ItemOverrides overrides, @NotNull ResourceLocation modelLocation) {
            BakedModel baseBakedModel = this.baseModel.bake(bakery, spriteGetter, modelTransform, modelLocation);
            return new CosmicBakedModel(baseBakedModel, spriteGetter.apply(this.maskMaterial));
        }

        public Collection<Material> getMaterials(@NotNull IGeometryBakingContext owner, @NotNull Function<ResourceLocation, UnbakedModel> modelGetter, @NotNull Set<Pair<String, String>> missingTextureErrors) {
            Set<Material> materials = new HashSet<>();
            if (maskTexture != null) {
                ResourceLocation maskLocation = new ResourceLocation(maskTexture);
                UnbakedModel unbakedModel = modelGetter.apply(maskLocation);
                if (unbakedModel instanceof BlockModel maskModel) {
                    this.maskMaterial = maskModel.getMaterial("particle");
                    if (Objects.equals(this.maskMaterial.texture(), MissingTextureAtlasSprite.getLocation()))
                        missingTextureErrors.add(Pair.of(maskTexture, owner.getModelName()));
                    materials.add(maskMaterial);
                }
            }
            materials.addAll(this.baseModel.getMaterials(modelGetter, missingTextureErrors));
            return materials;
        }
    }
    static CosmicModelLoader INSTANCE = new CosmicModelLoader();
}
