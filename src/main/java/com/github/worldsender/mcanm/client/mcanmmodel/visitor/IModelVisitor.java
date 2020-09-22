package com.github.worldsender.mcanm.client.mcanmmodel.visitor;

import java.util.UUID;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface IModelVisitor {
    void visitModelUUID(UUID uuid);

    void visitArtist(String artist);

    IPartVisitor visitPart(String name);

    void visitEnd();
}
