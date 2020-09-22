package com.github.worldsender.mcanm.client.mcanmmodel.stored.parts;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class HeaderV1 {
    /**
     * Unsigned byte
     */
    public int nbrParts;
    /**
     * Unsigned byte
     */
    public int nbrBones;
}
