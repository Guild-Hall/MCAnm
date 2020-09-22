package com.github.worldsender.mcanm.client.model;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.github.worldsender.mcanm.client.mcanmmodel.ModelMCMD;
import com.github.worldsender.mcanm.client.model.util.ModelStateInformation;
import com.github.worldsender.mcanm.common.animation.IAnimation;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.IModelTransform;
import net.minecraft.client.renderer.model.IUnbakedModel;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.model.Material;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.geometry.IModelGeometry;

/**
 * A model loader for item models (new with 1.9) (IModel)
 *
 * @author WorldSEnder
 */
@OnlyIn(Dist.CLIENT)
public enum ModelLoader implements IModelLoader<ModelLoader.ModelWrapper> {
    INSTANCE;

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {
        // nothing to do
    }

    @Override
    public ModelWrapper read(JsonDeserializationContext deserializationContext, JsonObject modelContents) {
        ModelDescription description = ModelDescription.parse(modelContents);
        return new ModelWrapper(description);
    }

    private static class AnimationStateProxy implements IAnimation {
        public AnimationStateProxy() {
        }

        @Override
        public boolean storeCurrentTransformation(String bone, float frame, BoneTransformation transform) {
            transform.matrix.setIdentity();
            return true;
        }
    }

    public static class ModelWrapper implements IModelGeometry<ModelWrapper> {
        private final ModelMCMD actualModel;

        public ModelWrapper(ModelDescription description) {
            this.actualModel = description.getModel();
        }

        @Override
        public IBakedModel bake(IModelConfiguration owner, ModelBakery bakery,
                Function<Material, TextureAtlasSprite> spriteGetter, IModelTransform modelTransform,
                ItemOverrideList overrides, ResourceLocation modelLocation) {
            ImmutableMap.Builder<String, TextureAtlasSprite> slotToTexSprite = ImmutableMap.builder();

            for (String textureSlot : actualModel.getTextureSlots()) {
                slotToTexSprite.put(textureSlot, spriteGetter.apply(owner.resolveTexture(textureSlot)));
            }
            // Note the missing leading '#', surely it does not collide
            TextureAtlasSprite particleSprite = spriteGetter.apply(owner.resolveTexture("particles"));
            return new BakedModelWrapper(actualModel, particleSprite, slotToTexSprite.build(), overrides);
        }

        @Override
        public Collection<Material> getTextures(IModelConfiguration owner,
                Function<ResourceLocation, IUnbakedModel> modelGetter,
                Set<com.mojang.datafixers.util.Pair<String, String>> missingTextureErrors) {
            return actualModel.getTextureSlots().stream().map(owner::resolveTexture).collect(Collectors.toSet());
        }
    }

    public static class BakedModelWrapper implements IBakedModel {
        private final ModelMCMD actualModel;
        private final ImmutableMap<String, TextureAtlasSprite> slotToSprite;
        private final TextureAtlasSprite particleSprite;
        private final ModelStateInformation stateInformation;
        private final ItemOverrideList itemOverrides;

        public BakedModelWrapper(
                ModelMCMD model,
                TextureAtlasSprite particleSprite,
                ImmutableMap<String, TextureAtlasSprite> slotToSprite,
                ItemOverrideList itemOverrides) {
            this.actualModel = Objects.requireNonNull(model);
            this.slotToSprite = Objects.requireNonNull(slotToSprite);
            // There is at least the "missingno" texture in the list
            this.particleSprite = particleSprite;
            this.stateInformation = new ModelStateInformation();
            stateInformation.setAnimation(new AnimationStateProxy());
            stateInformation.setFrame(0);
            this.itemOverrides = Objects.requireNonNull(itemOverrides);
        }

        @Override
        public boolean isBuiltInRenderer() {
            return false;
        }

        @Override
        public boolean isAmbientOcclusion() {
            return true;
        }

        @Override
        public boolean isGui3d() {
            return true;
        }

        @Override
        public boolean func_230044_c_() {
            return false; // soft shadows
        }

        @Override
        public List<BakedQuad> getQuads(BlockState state, Direction side, Random rand) {
            return actualModel.getAsBakedQuads(stateInformation, slotToSprite);
        }

        @Override
        public ItemOverrideList getOverrides() {
            return itemOverrides;
        }

        @Override
        public TextureAtlasSprite getParticleTexture() {
            return particleSprite;
        }
    }
}
