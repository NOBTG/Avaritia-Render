package com.lolipickaxe.Avaritia;

import codechicken.lib.model.CachedFormat;
import codechicken.lib.model.Quad;
import com.lolipickaxe.LoliPickaxeMod;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

public class HaloItemModelLoader implements UnbakedModel {
    public static final ResourceLocation ID = new ResourceLocation(LoliPickaxeMod.MODID, "halo");
    private final BlockModel baseModel = (BlockModel) Minecraft.getInstance().getModelManager().getModel(ID);
    private final String maskTexture = "";
    public HaloItemModelLoader() {
    }
    @Override
    public @NotNull Collection<ResourceLocation> getDependencies() {
        Set<ResourceLocation> arrayList = new HashSet<>();
        arrayList.add(new ResourceLocation(LoliPickaxeMod.MODID, "cosmic"));
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
        return (BakedModel) (new CosmicModelLoader.CosmicModelGeometry(baseModel, maskTexture)).baseModel;
    }


    public static class HaloItemModelGeometry implements IUnbakedGeometry<HaloItemModelGeometry> {
        private final BlockModel baseModel;

        private final IntList layerColors;

        private final String texture;

        private final int color;

        private final int size;

        private final boolean pulse;

        private Material haloMaterial;

        public HaloItemModelGeometry(BlockModel baseModel, IntList layerColors, String texture, int color, int size, boolean pulse) {
            this.baseModel = baseModel;
            this.layerColors = layerColors;
            this.texture = texture;
            this.color = color;
            this.size = size;
            this.pulse = pulse;
        }

        public BakedModel bake(IGeometryBakingContext owner, ModelBakery bakery, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelTransform, ItemOverrides overrides, ResourceLocation modelLocation) {
            BakedModel bakedBaseModel = this.baseModel.bake(bakery, this.baseModel, spriteGetter, modelTransform, modelLocation, false);
            return new HaloBakedModel(tintLayers(bakedBaseModel, this.layerColors), spriteGetter.apply(this.haloMaterial), this.color, this.size, this.pulse);
        }

        public Collection<Material> getMaterials(IGeometryBakingContext owner, Function<ResourceLocation, UnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
            Set<Material> materials = new HashSet<>();
            this.haloMaterial = owner.getMaterial(this.texture);
            if (Objects.equals(this.haloMaterial.texture(), MissingTextureAtlasSprite.getLocation()))
                missingTextureErrors.add(Pair.of(this.texture, owner.getModelName()));
            materials.add(this.haloMaterial);
            materials.addAll(this.baseModel.getMaterials(modelGetter, missingTextureErrors));
            return materials;
        }
        public static final ModelProperty<Material> HALO_MATERIAL_PROPERTY = new ModelProperty<>();
        private static BakedModel tintLayers(BakedModel model, IntList layerColors) {
            if (layerColors.isEmpty())
                return model;
            Map<Direction, List<BakedQuad>> faceQuads = new HashMap<>();
            for (Direction face : Direction.values())
                faceQuads.put(face, transformQuads(model.getQuads(null, face, RandomSource.createNewThreadLocalInstance(), ModelData.builder().build(), RenderType.CUTOUT), layerColors));
            List<BakedQuad> unculled = transformQuads(model.getQuads(null, null, RandomSource.createNewThreadLocalInstance(), ModelData.builder().build(), RenderType.CUTOUT), layerColors);
            return new SimpleBakedModel(unculled, faceQuads, model
                    .useAmbientOcclusion(), model
                    .usesBlockLight(), model
                    .isGui3d(), model.getParticleIcon(), model
                    .getTransforms(), ItemOverrides.EMPTY);
        }

        private static List<BakedQuad> transformQuads(List<BakedQuad> quads, IntList layerColors) {
            List<BakedQuad> newQuads = new ArrayList<>(quads.size());
            for (BakedQuad quad : quads)
                newQuads.add(transformQuad(quad, layerColors));
            return newQuads;
        }

        private static BakedQuad transformQuad(BakedQuad quad, IntList layerColors) {
            int tintIndex = quad.getTintIndex();
            if (tintIndex == -1 || tintIndex >= layerColors.size())
                return quad;
            int tint = layerColors.getInt(tintIndex);
            if (tint == -1)
                return quad;
            Quad newQuad = new Quad();
            newQuad.reset(CachedFormat.BLOCK);
            float r = (tint >> 16 & 0xFF) / 255.0F;
            float g = (tint >> 8 & 0xFF) / 255.0F;
            float b = (tint & 0xFF) / 255.0F;
            for (Quad.Vertex v : newQuad.vertices) {
                v.color[0] = v.color[0] * r;
                v.color[1] = v.color[1] * g;
                v.color[2] = v.color[2] * b;
            }
            newQuad.tintIndex = -1;
            return newQuad.bake();
        }
    }
    static HaloItemModelLoader INSTANCE = new HaloItemModelLoader();
}
