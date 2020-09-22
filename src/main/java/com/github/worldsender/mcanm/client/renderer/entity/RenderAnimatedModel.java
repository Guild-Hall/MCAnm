package com.github.worldsender.mcanm.client.renderer.entity;

import com.github.worldsender.mcanm.client.ClientLoader;
import com.github.worldsender.mcanm.client.mcanmmodel.IModel;
import com.github.worldsender.mcanm.client.model.IEntityAnimator;
import com.github.worldsender.mcanm.client.model.IRenderPassInformation;
import com.github.worldsender.mcanm.client.model.ModelAnimated;
import com.github.worldsender.mcanm.client.model.util.RenderPass;
import com.github.worldsender.mcanm.client.model.util.RenderPassInformation;
import com.github.worldsender.mcanm.client.renderer.IAnimatedObject;
import com.github.worldsender.mcanm.common.skeleton.ISkeleton;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.LightType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.registry.IRenderFactory;

@OnlyIn(Dist.CLIENT)
public class RenderAnimatedModel<T extends LivingEntity> extends EntityRenderer<T> {
    private static final ResourceLocation UNUSED_TEXTURE = DefaultPlayerSkin.getDefaultSkinLegacy();
    protected ModelAnimated model;
    private IEntityAnimator<T> animator;

    private RenderPassInformation renderPassCache = new RenderPassInformation();
    private RenderPass<T> renderPass = new RenderPass<>(renderPassCache);
    private float partialTick;

    public RenderAnimatedModel(
            EntityRendererManager manager,
            ModelAnimated model,
            IEntityAnimator<T> animator,
            float shadowSize) {
        super(manager);
        this.model = model;
        this.animator = animator;
    }

    /**
     * Convenience alternative to the constructor. The entity this render is used for has to extend
     * {@link IAnimatedObject}.
     *
     * @param model      the model to use
     * @param shadowSize the shadow size...
     * @return
     */
    public static <T extends LivingEntity & IAnimatedObject> IRenderFactory<T> fromModel(
            IModel model,
            float shadowSize) {
        return fromModel(IAnimatedObject.ANIMATOR_ADAPTER(), model, shadowSize);
    }

    /**
     * Convenience alternative to the constructor. A new {@link ModelAnimated} is instantiated from the
     * {@link ResourceLocation} given (using the normal constructor).
     *
     * @param animator   the animator for the entity
     * @param model      the model to use
     * @param shadowSize the shadow size...
     * @return the constructed {@link RenderAnimatedModel}
     * @see IEntityAnimator
     * @see IEntityAnimator#STATIC_ENTITY
     * @see IAnimatedObject#ANIMATOR_ADAPTER
     * @see ClientLoader#loadModel(ResourceLocation, ISkeleton)
     */
    public static <T extends LivingEntity> IRenderFactory<T> fromModel(
            IEntityAnimator<T> animator,
            IModel model,
            float shadowSize) {
        ModelAnimated mcmodel = new ModelAnimated(model);

        return manager -> new RenderAnimatedModel<T>(manager, mcmodel, animator, shadowSize);
    }

    @Override
    public void render(T entity, float entityYaw, float partialTicks, MatrixStack matrixStackIn,
            IRenderTypeBuffer bufferIn, int packedLightIn) {
        renderPass.reset();
        renderPassCache.reset();

        // Setup information about this render pass
        matrixStackIn.push();
        Pose pose = entity.getPose();
        float rotationYaw = MathHelper.interpolateAngle(partialTicks, entity.prevRenderYawOffset, entity.renderYawOffset);
        if (pose != Pose.SLEEPING) {
            matrixStackIn.rotate(Vector3f.YP.rotationDegrees(180.0F - rotationYaw));
        }
        matrixStackIn.scale(-1.0F, -1.0F, 1.0F);
        matrixStackIn.translate(0.0D, (double)-1.501F, 0.0D);

        renderPass.setRendertype(bufferIn);
        renderPassCache.setMatrixStack(matrixStackIn);
        int packedOverlay = LivingRenderer.getPackedOverlay(entity, this.getOverlayProgress(entity, partialTicks));
        renderPassCache.setPackedColorOverlay(packedOverlay);
        int blockLight = this.getBlockLight(entity, partialTicks);
        int skyLight = entity.world.getLightFor(LightType.SKY, new BlockPos(entity.getEyePosition(partialTicks)));
        renderPassCache.setPackedLightmap(LightTexture.packLight(blockLight, skyLight));

        IRenderPassInformation actualRenderPass =
            this.animator.preRenderCallback(entity, renderPassCache, partialTick);
        renderPass.setRenderPassInformation(actualRenderPass);

        model.renderWithPass(renderPass);

        super.render(entity, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
        matrixStackIn.pop();
    }

    protected float getOverlayProgress(T entity, float partialTicks) {
        return 0.0F;
    }

    @Override
    public ResourceLocation getEntityTexture(T entity) {
        // We have to return non-null here to get rendered.
        return UNUSED_TEXTURE;
    }
}
