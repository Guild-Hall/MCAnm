package com.github.worldsender.mcanm.client;

import java.util.Locale;
import java.util.function.Function;

import com.github.worldsender.mcanm.IProxy;
import com.github.worldsender.mcanm.MCAnm;
import com.github.worldsender.mcanm.Reference;
import com.github.worldsender.mcanm.client.mcanmmodel.IModel;
import com.github.worldsender.mcanm.client.model.IEntityAnimator;
import com.github.worldsender.mcanm.client.model.ModelLoader;
import com.github.worldsender.mcanm.client.model.util.RenderPassInformation;
import com.github.worldsender.mcanm.client.renderer.entity.RenderAnimatedModel;
import com.github.worldsender.mcanm.common.CommonLoader;
import com.github.worldsender.mcanm.common.resource.IResourceLocation;
import com.github.worldsender.mcanm.common.skeleton.ISkeleton;
import com.github.worldsender.mcanm.test.CubeEntity;
import com.github.worldsender.mcanm.test.CubeEntityV2;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

//checking if this sustain server issues.
@OnlyIn(Dist.CLIENT)
public class ClientProxy implements IProxy.IClientProxy {
    private static <T extends Entity> IEntityAnimator<T> makeAnimator(String textureDir) {
        Function<String, ResourceLocation> cachingTransform =
            RenderPassInformation.makeCachingTransform(key -> new ResourceLocation(textureDir + key.toLowerCase(Locale.ENGLISH) + ".png"));
        IEntityAnimator<T> animator = (_entity, buffer, _partialTick) -> {
            return buffer.setTextureTransform(cachingTransform);
        };
        return animator;
    }

    public ClientProxy() {
        FMLJavaModLoadingContext.get().getModEventBus().register(this);
    }

    @SubscribeEvent
    public void onRegisterItems(RegistryEvent.Register<Item> itemRegistry) {
        // We do this here, since it appears that ModelRegistryEvent is fired too late.
        ModelLoaderRegistry.registerLoader(new ResourceLocation(Reference.core_modid, "loader"), ModelLoader.INSTANCE);

        if (!MCAnm.isDebug) return;

        Item debug = new Item(new Item.Properties());
        debug.addPropertyOverride(new ResourceLocation("test"), new IItemPropertyGetter() {
            @Override
            public float call(ItemStack stack, World worldIn, LivingEntity entity) {
                return entity == null ? 0.0F : (entity.ticksExisted / 100f) % 1f;
            }
        });
        debug.setRegistryName(Reference.core_modid, "debug_item");
        itemRegistry.getRegistry().register(debug);
        //net.minecraftforge.client.model.ModelLoader.setCustomModelResourceLocation(debug, 0,
        //        new ModelResourceLocation("mcanm:models/item/debug_item.mcmdl#inventory"));
    }

    @SubscribeEvent
    public void onRegisterEntities(RegistryEvent.Register<EntityType<?>> regEvent) {
        if (!MCAnm.isDebug) return;
        regEvent.getRegistry().register(CubeEntity.ENTITY_TYPE);
        regEvent.getRegistry().register(CubeEntityV2.ENTITY_TYPE);
    }

    @SubscribeEvent
    public void setup(FMLClientSetupEvent setupEvent) {
        ClientResourcePool.instance.activate(Minecraft.getInstance().getResourceManager());

        if (MCAnm.isDebug) {
            ResourceLocation modelSrc = new ResourceLocation("mcanm:models/cube/cube.mcmd");
            @SuppressWarnings("deprecation")
            ISkeleton skeleton = CommonLoader.loadLegacySkeleton(modelSrc);
            IModel model = ClientLoader.loadModel(modelSrc, skeleton);
            IRenderFactory<CubeEntity> renderer = RenderAnimatedModel.fromModel(model, 1.0f);

            ResourceLocation model2Src = new ResourceLocation("mcanm:models/cubev2/cube.mcmd");
            IModel model2 = ClientLoader.loadModel(model2Src, ISkeleton.EMPTY);
            IRenderFactory<CubeEntityV2> renderer2 = RenderAnimatedModel
                    .fromModel(makeAnimator("mcanm:textures/models/cube/"), model2, 1.0f);

            RenderingRegistry.registerEntityRenderingHandler(CubeEntity.ENTITY_TYPE, renderer);
            RenderingRegistry.registerEntityRenderingHandler(CubeEntityV2.ENTITY_TYPE, renderer2);
        }
    }

    @Override
    public IResourceLocation getSidedResource(ResourceLocation resLoc, ClassLoader context) {
        return ClientResourcePool.instance.makeResourceLocation(resLoc);
    }
}
