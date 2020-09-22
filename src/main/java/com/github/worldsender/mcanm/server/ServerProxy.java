package com.github.worldsender.mcanm.server;

import com.github.worldsender.mcanm.IProxy;
import com.github.worldsender.mcanm.common.resource.IResourceLocation;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@OnlyIn(Dist.DEDICATED_SERVER)
public class ServerProxy implements IProxy.IServerProxy {
    public ServerProxy() {
        FMLJavaModLoadingContext.get().getModEventBus().register(this);
    }

    @SubscribeEvent
    public void setup(FMLDedicatedServerSetupEvent setupEvent) {
        ServerResourcePool.instance.activate(setupEvent.getServerSupplier().get().getResourceManager());
    }

    @Override
    public IResourceLocation getSidedResource(ResourceLocation resLoc, ClassLoader context) {
        return ServerResourcePool.instance.makeResourceLocation(resLoc);
    }
}
