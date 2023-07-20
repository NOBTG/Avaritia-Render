package com.lolipickaxe.Avaritia;

import codechicken.lib.model.PerspectiveModelState;
import codechicken.lib.model.bakedmodels.WrappedItemModel;
import codechicken.lib.render.item.IItemRenderer;
import codechicken.lib.util.ArrayUtils;
import codechicken.lib.util.LambdaUtils;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Transformation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.SimpleModelState;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class CosmicBakedModel extends WrappedItemModel implements IItemRenderer {
    private final List<BakedQuad> maskQuads;

     final ItemOverrides overrideList = new ItemOverrides() {
        public BakedModel resolve(@NotNull BakedModel originalModel, @NotNull ItemStack stack, @Nullable ClientLevel world, @Nullable LivingEntity entity, int seed) {
            CosmicBakedModel.this.entity = entity;
            CosmicBakedModel.this.world = (world == null) ? ((entity == null) ? null : (ClientLevel)entity.level) : null;
            return CosmicBakedModel.this.wrapped.getOverrides().resolve(originalModel, stack, world, entity, seed);
        }
    };

    public CosmicBakedModel(BakedModel wrapped, TextureAtlasSprite maskSprite) {
        super(wrapped);
        this.maskQuads = bakeItem(maskSprite);
    }

    public void renderItem(ItemStack stack, ItemTransforms.TransformType transformType, PoseStack pStack, MultiBufferSource source, int packedLight, int packedOverlay) {
        renderWrapped(stack, pStack, source, packedLight, packedOverlay, true);
        if (source instanceof MultiBufferSource.BufferSource bs) {
            bs.endBatch();
        }
        float yaw = 0.0F;
        float pitch = 0.0F;
        float scale = 1.0F;
        if (transformType != ItemTransforms.TransformType.GUI) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                yaw = (float)((mc.player.getYRot() * 2.0F) * Math.PI / 360.0D);
            }
            pitch = -((float)((mc.player.getXRot() * 2.0F) * Math.PI / 360.0D));
        } else {
            scale = 25.0F;
        }
        if (AvaritiaShaders.cosmicOpacity != null)
            AvaritiaShaders.cosmicOpacity.glUniform1f(1.0F);
        AvaritiaShaders.cosmicYaw.glUniform1f(yaw);
        AvaritiaShaders.cosmicPitch.glUniform1f(pitch);
        AvaritiaShaders.cosmicExternalScale.glUniform1f(scale);
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        VertexConsumer cons = source.getBuffer(RenderType.create("lolipickaxe:cosmic", DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS, 2097152, true, false, RenderType.CompositeState.builder()
                .setShaderState(new RenderStateShard.ShaderStateShard(() -> AvaritiaShaders.cosmicShader))
                .setDepthTestState(RenderStateShard.EQUAL_DEPTH_TEST)
                .setLightmapState(RenderStateShard.LIGHTMAP)
                .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                .setTextureState(RenderStateShard.BLOCK_SHEET_MIPPED)
                .createCompositeState(true)));
        itemRenderer.renderQuadList(pStack, cons, this.maskQuads, stack, packedLight, packedOverlay);
    }

    public ModelState getModelTransform() {
        return this.parentState;
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction direction, @NotNull RandomSource source) {
        return new ArrayList<>();
    }

    public boolean useAmbientOcclusion() {
        return this.wrapped.useAmbientOcclusion();
    }

    public boolean isGui3d() {
        return this.wrapped.isGui3d();
    }

    public boolean usesBlockLight() {
        return this.wrapped.usesBlockLight();
    }

    @Override
    public boolean isCustomRenderer() {
        return true;
    }

    @Override
    public @NotNull TextureAtlasSprite getParticleIcon() {
        return (TextureAtlasSprite) Minecraft.getInstance().getTextureAtlas(new ResourceLocation(""));
    }

    public @NotNull ItemOverrides getOverrides() {
        return this.overrideList;
    }

    private static List<BakedQuad> bakeItem(TextureAtlasSprite... sprites) {
        return bakeItem(Transformation.identity(), sprites);
    }

    private static final ItemModelGenerator ITEM_MODEL_GENERATOR = new ItemModelGenerator();

    private static final FaceBakery FACE_BAKERY = new FaceBakery();

    private static List<BakedQuad> bakeItem(Transformation state, TextureAtlasSprite... sprites) {
        ModelState modelState = new SimpleModelState(state, false);
        LambdaUtils.checkArgument(sprites, "Sprites must not be Null or empty!", ArrayUtils::isNullOrContainsNull);
        List<BakedQuad> quads = new LinkedList<>();
        for (int i = 0; i < sprites.length; i++) {
            TextureAtlasSprite sprite = sprites[i];
            List<BlockElement> unbaked = ITEM_MODEL_GENERATOR.processFrames(i, "layer" + i, sprite);
            for (BlockElement element : unbaked) {
                for (Map.Entry<Direction, BlockElementFace> entry : element.faces.entrySet())
                    quads.add(FACE_BAKERY.bakeQuad(element.from, element.to, entry.getValue(), sprite, entry.getKey(), modelState, element.rotation, element.shade, new ResourceLocation("avaritia", "dynamic")));
            }
        }
        return quads;
    }

    @Override
    public @Nullable PerspectiveModelState getModelState() {
        return (PerspectiveModelState) getModelTransform();
    }
}