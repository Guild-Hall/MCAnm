package com.github.worldsender.mcanm.client;

import com.github.worldsender.mcanm.client.model.IRenderPassInformation;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * An extension of only the user supplied information. This adds engine-based properties such as the used
 * {@link Tessellator}.
 *
 * @author WorldSEnder
 */
@OnlyIn(Dist.CLIENT)
public interface IRenderPass extends IRenderPassInformation {
    /**
     * Get the render type buffer for this pass
     */
    IRenderTypeBuffer getRenderTypeBuffer();
}
