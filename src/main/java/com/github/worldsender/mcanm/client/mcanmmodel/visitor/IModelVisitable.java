package com.github.worldsender.mcanm.client.mcanmmodel.visitor;

import java.util.UUID;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface IModelVisitable {
    void visitBy(IModelVisitor visitor);

    String getArtist();

    UUID getModelUUID();
}
