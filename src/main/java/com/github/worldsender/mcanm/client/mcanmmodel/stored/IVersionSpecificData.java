package com.github.worldsender.mcanm.client.mcanmmodel.stored;

import com.github.worldsender.mcanm.client.mcanmmodel.visitor.IModelVisitor;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface IVersionSpecificData {
    int getVersion();

    void visitBy(IModelVisitor visitor);
}
