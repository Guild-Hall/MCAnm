package com.github.worldsender.mcanm.server;

import com.github.worldsender.mcanm.common.resource.ResourcePool;

import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Used to collect all {@link ServerResourceLocation}s and trigger an update when the {@link IResourceManager}
 * changes.
 *
 * @author WorldSEnder
 */
@OnlyIn(Dist.DEDICATED_SERVER)
public class ServerResourcePool extends ResourcePool<ServerResourceLocation> {

    public static final ServerResourcePool instance = new ServerResourcePool();

    @Override
    protected ServerResourceLocation createResourceLocation(ResourceLocation resLoc) {
        return new ServerResourceLocation(this, resLoc);
    }

}
