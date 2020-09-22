package com.github.worldsender.mcanm.client;

import com.github.worldsender.mcanm.common.resource.ResourcePool;

import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

/**
 * Used to collect all {@link ClientResourceLocation}s and trigger an update when the {@link IResourceManager}
 * changes.
 *
 * @author WorldSEnder
 */
public class ClientResourcePool extends ResourcePool<ClientResourceLocation> {
    public static final ClientResourcePool instance = new ClientResourcePool();

    protected ClientResourceLocation createResourceLocation(ResourceLocation resLoc) {
        return new ClientResourceLocation(this, resLoc);
    }
}
