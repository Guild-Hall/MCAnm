package com.github.worldsender.mcanm.client;

import java.util.Objects;

import com.github.worldsender.mcanm.MCAnm;
import com.github.worldsender.mcanm.client.mcanmmodel.ModelMCMD;
import com.github.worldsender.mcanm.common.CommonLoader;
import com.github.worldsender.mcanm.common.skeleton.ISkeleton;
import com.github.worldsender.mcanm.common.util.CallResolver;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientLoader {
    /**
     * Loads a .mcmd model from the given {@link ResourceLocation} and binds it to the skeleton. Note that while the raw
     * data from the ResourceLocation is cached, the model is not (because its state is not immutable), so try to not
     * load a model per frame but save it somewhere.
     *
     * @param resLoc   the resource location to load from
     * @param skeleton the skeleton that the model should use
     * @return
     * @see CommonLoader#loadSkeleton(ResourceLocation)
     * @see CommonLoader#loadLegacySkeleton(ResourceLocation)
     * @see CommonLoader#loadAnimation(ResourceLocation)
     * @see ISkeleton#EMPTY
     */
    public static ModelMCMD loadModel(ResourceLocation resLoc, ISkeleton skeleton) {
        Objects.requireNonNull(skeleton);
        ClassLoader loader = CallResolver.INSTANCE.getCallingClass().getClassLoader();
        ModelMCMD model = new ModelMCMD(MCAnm.getSidedProxy().getSidedResource(resLoc, loader), skeleton);
        return model;
    }
}
