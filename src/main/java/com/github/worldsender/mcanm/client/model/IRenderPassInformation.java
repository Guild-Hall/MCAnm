package com.github.worldsender.mcanm.client.model;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface IRenderPassInformation extends IModelStateInformation {
    /**
     * Retrieves the texture that should go into the texture slot slot
     *
     * @param slot the name of the texture slot getting polled
     * @return the resource location of the texture to stick
     * 
     */
    /// TODO: this should probably be update to {@link net.minecraft.client.renderer.RenderType}
    ResourceLocation getActualResourceLocation(String slot);
    /**
     * Get the active matrix stack for this render pass
     * @return
     */
    MatrixStack getActiveMatrixStack();
    /**
     * Get the packed light source active for the rendered model. See also {@link net.minecraft.client.renderer.LightTexture#packLight(int, int)}
     * @return
     */
    int getPackedLight();
    /**
     * Get the packed color overlay for the rendered model. See also {@link net.minecraft.client.renderer.entity.LivingRenderer#getPackedLight(net.minecraft.entity.Entity, float)}
     * @return
     */
    int getPackedOverlay();
}
