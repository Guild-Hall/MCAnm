package com.github.worldsender.mcanm.client.model;

import com.github.worldsender.mcanm.common.animation.IAnimation;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface IModelStateInformation {

    IAnimation getAnimation();

    float getFrame();

    boolean shouldRenderPart(String part);

}
