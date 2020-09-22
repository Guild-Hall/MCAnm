package com.github.worldsender.mcanm.test;

import com.github.worldsender.mcanm.client.model.util.RenderPassInformation;
import com.github.worldsender.mcanm.client.renderer.IAnimatedObject;
import com.github.worldsender.mcanm.common.CommonLoader;
import com.github.worldsender.mcanm.common.animation.IAnimation;

import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import java.util.Optional;

public class CubeEntity extends PigEntity implements IAnimatedObject {
    public static final IAnimation animation;

    private static final ResourceLocation ID_CUBE = new ResourceLocation("mcanm:cube");
    public static final EntityType<CubeEntity> ENTITY_TYPE;

    static {
        animation = CommonLoader.loadAnimation(new ResourceLocation("mcanm:models/cube/idle.mcanm"));
        ENTITY_TYPE = EntityType.Builder
            .create(CubeEntity::new, EntityClassification.MISC)
            .disableSerialization()
            .immuneToFire()
            .size(1, 1)
            .build(ID_CUBE.toString());
        ENTITY_TYPE.setRegistryName(ID_CUBE);
    }

    public CubeEntity(EntityType<? extends CubeEntity> entityType, World w) {
        super(entityType, w);
    }

    @Override
    public RenderPassInformation preRenderCallback(float subFrame, RenderPassInformation callback) {
        return callback.setAnimation(Optional.of(animation)).setFrame(this.ticksExisted % 90 + subFrame);
    }
}
