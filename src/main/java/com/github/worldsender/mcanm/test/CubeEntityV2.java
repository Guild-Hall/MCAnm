package com.github.worldsender.mcanm.test;

import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class CubeEntityV2 extends PigEntity {

    private static final ResourceLocation ID_CUBE_V2 = new ResourceLocation("mcanm:cube2");
    public static final EntityType<CubeEntityV2> ENTITY_TYPE;

    static {
        ENTITY_TYPE = EntityType.Builder
            .create(CubeEntityV2::new, EntityClassification.MISC)
            .disableSerialization()
            .immuneToFire()
            .size(1, 1)
            .build(ID_CUBE_V2.toString());
        ENTITY_TYPE.setRegistryName(ID_CUBE_V2);
    }

    public CubeEntityV2(EntityType<? extends CubeEntityV2> entityType, World w) {
        super(entityType, w);
    }
}
