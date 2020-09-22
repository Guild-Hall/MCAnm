package com.github.worldsender.mcanm;

import com.github.worldsender.mcanm.client.config.MCAnmConfiguration;
import com.github.worldsender.mcanm.common.util.SidedObject;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Reference.core_modid)
public class MCAnm {
    private static MCAnm instance;

    private static final Logger LOGGER = LogManager.getLogger();
    private final Logger logger = LOGGER;
    /**
     * Enables various visual outputs, e.g. the bones of models are rendered.
     */
    public static final boolean isDebug;

    static {
        isDebug = LOGGER.isDebugEnabled();
    }

    private MCAnmConfiguration configuration;
    private SidedObject<IProxy, IProxy.IClientProxy, IProxy.IServerProxy> sidedProxy;

    public MCAnm() {
        if(instance != null) { throw new IllegalArgumentException("initialized twice"); }
        instance = this;

        Pair<MCAnmConfiguration, ForgeConfigSpec> configSpec = MCAnmConfiguration.createConfigSpec();
        configuration = configSpec.getLeft();
        ModLoadingContext.get().registerConfig(Type.COMMON, configSpec.getRight());
        // ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.CONFIGGUIFACTORY, extension);
        MinecraftForge.EVENT_BUS.register(this);
        FMLJavaModLoadingContext.get().getModEventBus().register(this);
        sidedProxy = SidedObject.of(
            () -> MCAnm::createClientProxy,
            () -> MCAnm::createServerProxy
        );
    }

    @OnlyIn(Dist.CLIENT)
    private static IProxy.IClientProxy createClientProxy() {
        return new com.github.worldsender.mcanm.client.ClientProxy();
    }

    @OnlyIn(Dist.DEDICATED_SERVER)
    private static IProxy.IServerProxy createServerProxy() {
        return new com.github.worldsender.mcanm.server.ServerProxy();
    }

    public static MCAnmConfiguration configuration() {
        return instance.configuration;
    }

    public static Logger logger() {
        return instance.getLogger();
    }

	public static IProxy getSidedProxy() {
		return instance.sidedProxy.get();
	}

    @SubscribeEvent
    public void commonSetup(FMLCommonSetupEvent pre) {
        logger.info("Setting up MCAnm");
    }

    public MCAnmConfiguration getConfiguration() {
        return this.configuration;
    }

    public Logger getLogger() {
        return this.logger;
    }
}
