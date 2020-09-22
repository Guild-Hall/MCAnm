package com.github.worldsender.mcanm.client.mcanmmodel.visitor;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface IMaterialVisitor {
    void visitTexture(String textureName);

    void visitEnd();
}
