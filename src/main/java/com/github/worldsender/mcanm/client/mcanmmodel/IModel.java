package com.github.worldsender.mcanm.client.mcanmmodel;

import com.github.worldsender.mcanm.client.IRenderPass;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface IModel {
    void render(IRenderPass renderPass);
}
