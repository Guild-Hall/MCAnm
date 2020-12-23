package com.github.worldsender.mcanm.client.model;

import com.github.worldsender.mcanm.common.animation.IPose;

public interface IModelStateInformation {

    IPose getModelPose();

    boolean shouldRenderPart(String part);

}
