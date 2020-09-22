package com.github.worldsender.mcanm.client.model.util;

import java.util.Objects;

import com.github.worldsender.mcanm.client.IRenderPass;
import com.github.worldsender.mcanm.client.model.IRenderPassInformation;
import com.github.worldsender.mcanm.common.animation.IAnimation;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

/**
 * A collection of information neccessary to render the model.
 *
 * @author WorldSEnder
 */
public class RenderPass<T extends Entity> implements IRenderPass {
    private static final class NoopRenderTypeBuffer implements IRenderTypeBuffer {
        static final NoopRenderTypeBuffer INSTANCE = new NoopRenderTypeBuffer();
        @Override
        public IVertexBuilder getBuffer(RenderType p_getBuffer_1_) {
            return new IVertexBuilder(){
                @Override
                public IVertexBuilder color(int red, int green, int blue, int alpha) { return this; }
                @Override
                public IVertexBuilder lightmap(int u, int v) { return this; }
                @Override
                public IVertexBuilder normal(float x, float y, float z) { return this; }
                @Override
                public IVertexBuilder overlay(int u, int v) { return this; }
                @Override
                public IVertexBuilder pos(double x, double y, double z) { return this; }
                @Override
                public IVertexBuilder tex(float u, float v) { return this; }
                @Override
                public void endVertex() {}
            };
        }
    }

    private IRenderPassInformation userInfo;
    private IRenderTypeBuffer rendertype;

    public RenderPass(IRenderPassInformation info) {
        this.userInfo = Objects.requireNonNull(info);
        reset();
    }

    public void reset() {
        this.setRendertype(NoopRenderTypeBuffer.INSTANCE);
    }

    @Override
    public IAnimation getAnimation() {
        return userInfo.getAnimation();
    }

    @Override
    public float getFrame() {
        return userInfo.getFrame();
    }

    @Override
    public boolean shouldRenderPart(String part) {
        return userInfo.shouldRenderPart(part);
    }

    @Override
    public ResourceLocation getActualResourceLocation(String in) {
        return userInfo.getActualResourceLocation(in);
    }

    public RenderPass<T> setRenderPassInformation(IRenderPassInformation info) {
        this.userInfo = Objects.requireNonNull(info);
        return this;
    }

    public void setRendertype(IRenderTypeBuffer rendertype) {
        this.rendertype = Objects.requireNonNull(rendertype);;
    }

    @Override
    public int getPackedLight() {
        return this.userInfo.getPackedLight();
    }

    @Override
    public int getPackedOverlay() {
        return this.userInfo.getPackedOverlay();
    }

    @Override
    public MatrixStack getActiveMatrixStack() {
        return this.userInfo.getActiveMatrixStack();
    }

    @Override
    public IRenderTypeBuffer getRenderTypeBuffer() {
        return this.rendertype;
    }
}
