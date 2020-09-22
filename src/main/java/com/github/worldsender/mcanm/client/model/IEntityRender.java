package com.github.worldsender.mcanm.client.model;

import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface IEntityRender<T extends Entity> {
    /**
     * Retrieves the current animator
     *
     * @return
     */
    IEntityAnimator<T> getAnimator();

    /**
     * Binds a texture
     */
    void bindTextureFrom(ResourceLocation resLoc);
}
