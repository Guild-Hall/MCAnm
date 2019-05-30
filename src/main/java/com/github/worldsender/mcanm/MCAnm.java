package com.github.worldsender.mcanm;

import com.github.worldsender.mcanm.client.ClientProxy;
import com.github.worldsender.mcanm.client.config.MCAnmConfiguration;
import com.github.worldsender.mcanm.test.CubeEntity;
import com.github.worldsender.mcanm.test.CubeEntityV2;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import org.apache.logging.log4j.Logger;

@Mod(
        modid = Reference.core_modid,
        name = Reference.core_modname,
        version = Reference.core_modversion,
        guiFactory = "com.github.worldsender.mcanm.client.config.MCAnmGuiFactory")
public class MCAnm {
    /**
     * Enables various visual outputs, e.g. the bones of models are rendered.
     */
    public static final boolean isDebug;
    @Mod.Instance(Reference.core_modid)
    public static MCAnm instance;
    @SidedProxy(
            modId = Reference.core_modid,
            clientSide = "com.github.worldsender.mcanm.client.ClientProxy",
            serverSide = "com.github.worldsender.mcanm.server.ServerProxy")
    public static Proxy proxy;
    public static ClientProxy cp;
    public static boolean isClientSided = false;
    static {
        Object deobfEnv = Launch.blackboard.get("fml.deobfuscatedEnvironment");
        Boolean isDeobfEnv = deobfEnv instanceof Boolean ? (Boolean) deobfEnv : null;
        isDebug = isDeobfEnv != null && isDeobfEnv.booleanValue();
    }

    private MCAnmConfiguration config;
    private Logger logger;

    public static MCAnmConfiguration configuration() {
        return instance.getConfiguration();
    }

    public static Logger logger() {
        return instance.getLogger();
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent pre) {
        logger = pre.getModLog();
        config = new MCAnmConfiguration(pre.getSuggestedConfigurationFile());
        MinecraftForge.EVENT_BUS.register(this);
        logger.info("Successfully loaded MC Animations");
        if(proxy instanceof ClientProxy) {
			isClientSided = true;
			cp = (ClientProxy) proxy;
			cp.preInit();
		} else {
			isClientSided = false;
		}
    }

    @Mod.EventHandler
    public void init(@SuppressWarnings("unused") FMLInitializationEvent event) {
        if (isDebug) {
            ResourceLocation ID_CUBE = new ResourceLocation("mcanm:cube");
            ResourceLocation ID_CUBE_V2 = new ResourceLocation("mcanm:cube2");
            EntityRegistry.registerModEntity(ID_CUBE, CubeEntity.class, "Cube", 0, this, 80, 1, true);
            EntityRegistry.registerModEntity(ID_CUBE_V2, CubeEntityV2.class, "CubeV2", 1, this, 80, 1, true);


        }
        proxy.init();
    }

    @SubscribeEvent
    public void onConfigChange(OnConfigChangedEvent occe) {
        if (!occe.getModID().equals(Reference.core_modid))
            return;
        config.onConfigChange(occe);
    }

    public MCAnmConfiguration getConfiguration() {
        return this.config;
    }

    public Logger getLogger() {
        return this.logger;
    }
}
