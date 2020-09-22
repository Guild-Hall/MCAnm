package com.github.worldsender.mcanm.client;

import java.io.IOException;
import java.util.Objects;

import com.github.worldsender.mcanm.common.resource.IResource;
import com.github.worldsender.mcanm.common.resource.ResourceAdapter;
import com.github.worldsender.mcanm.common.resource.ResourceLocationAdapter;

import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientResourceLocation extends ResourceLocationAdapter {
    private final ResourceLocation loc;
    private final ClientResourcePool pool;
    /**
     * Warning: You should probably use {@link MinecraftResourcePool#instance#makeResourceLocation(ResourceLocation)}
     * else the resource won't listen to resource pack reloads.
     *
     * @param loc
     */
    public ClientResourceLocation(ResourceLocation loc) {
        this(ClientResourcePool.instance, loc);
    }

    /**
     * Warning: You should probably use {@link MinecraftResourcePool#instance#makeResourceLocation(ResourceLocation)}
     * else the resource won't listen to resource pack reloads.
     *
     * @param loc
     */
    public ClientResourceLocation(ClientResourcePool pool, ResourceLocation loc) {
        this.loc = Objects.requireNonNull(loc);
        this.pool = Objects.requireNonNull(pool);
    }

    @Override
    public IResource open() throws IOException {
        return new MinecraftResource(loc, pool.getCurrentResourceManager());
    }

    @Override
    public String getResourceName() {
        return loc.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((loc == null) ? 0 : loc.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof ClientResourceLocation)) {
            return false;
        }
        ClientResourceLocation other = (ClientResourceLocation) obj;
        if (loc == null) {
            return other.loc == null;
        } else return loc.equals(other.loc);
    }

    private /*static*/ class MinecraftResource extends ResourceAdapter {
        public MinecraftResource(ResourceLocation resLoc, IResourceManager manager) throws IOException {
            super(ClientResourceLocation.this, manager.getResource(resLoc).getInputStream());
        }
    }
}
