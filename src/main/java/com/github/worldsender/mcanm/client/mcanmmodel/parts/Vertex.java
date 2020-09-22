package com.github.worldsender.mcanm.client.mcanmmodel.parts;


import com.github.worldsender.mcanm.client.IRenderPass;
import com.github.worldsender.mcanm.common.util.math.Point4f;
import com.github.worldsender.mcanm.common.util.math.Tuple2f;
import com.github.worldsender.mcanm.common.util.math.Tuple3f;
import com.github.worldsender.mcanm.common.util.math.Tuple4f;
import com.github.worldsender.mcanm.common.util.math.Vector2f;
import com.mojang.blaze3d.matrix.MatrixStack.Entry;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.Vector4f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Vertex {

    private Vector4f pos;
    private Vector3f norm;
    private Vector2f uv;

    public Vertex(Tuple4f pos, Tuple3f norm2, Tuple2f uv2) {
        this.pos = new Vector4f(pos.x, pos.y, pos.z, pos.w);
        this.norm = new Vector3f(norm2.x, norm2.y, norm2.z);
        this.uv = new Vector2f(uv2.x, uv2.y);
    }

    public Vertex(Vertex copyFrom) {
        Vector4f p = copyFrom.pos;
        Vector3f n = copyFrom.norm;
        this.pos = new Vector4f(p.getX(), p.getY(), p.getZ(), p.getW());
        this.norm = new Vector3f(n.getX(), n.getY(), n.getZ());
        this.uv = new Vector2f(copyFrom.uv);
    }

    /**
     * Uses the {@link IVertexBuilder} to draw the model.
     */
    public void render(IVertexBuilder buffer, IRenderPass renderPass) {
        buffer.addVertex(
            pos.getX() / pos.getW(), pos.getY() / pos.getW(), pos.getZ() / pos.getW(),
            1.0F, 1.0F, 1.0F, 1.0F,
            uv.x, uv.y,
            renderPass.getPackedOverlay(),
            renderPass.getPackedLight(),
            norm.getX(), norm.getY(), norm.getZ());
    }

    /**
     * Offsets this Vertex by the {@link Vector4f} given.
     *
     * @param vector the offset
     */
    public void offset(Point4f point) {
        this.pos.set(this.pos.getX() + point.getX(), this.pos.getY() + point.getY(), this.pos.getZ() + point.getZ(), this.pos.getW() + point.getW());
    }

    /**
     * Adds the normal to this Vertex basically interpolating between the current normal and the given one (by their
     * scale). No normalization is done as this is taken care of by OpenGL during rendering.
     *
     * @param normal the normal to add
     */
    public void addNormal(Tuple3f normal) {
        this.norm.set(this.norm.getX() + normal.getX(), this.norm.getY() + normal.getY(), this.norm.getZ() + normal.getZ());
    }

    /**
     * Stores this vertex's uv coordinates in the target.
     */
    public void getUV(Tuple2f trgt) {
        trgt.set(this.uv);
    }

    /**
     * Stores this vertex's normal in the target.
     */
    public void getNormal(Tuple3f trgt) {
        trgt.set(this.norm.getX(), this.norm.getY(), this.norm.getZ());
    }

    /**
     * Stores this vertex's position in the target.
     */
    public void getPosition(Tuple4f trgt) {
        trgt.set(this.pos.getX(), this.pos.getY(), this.pos.getZ(), this.pos.getW());
    }

    public void retainUVOnly() {
        this.pos.set(0, 0, 0, 0);
        this.norm.set(0, 0, 0);
    }

    public void setRetainUV(Vertex copyFrom) {
        Vector4f p = copyFrom.pos;
        Vector3f n = copyFrom.norm;
        this.pos.set(p.getX(), p.getY(), p.getZ(), p.getW());
        this.norm.set(n.getX(), n.getY(), n.getZ());
    }

	public void globalTransform(Entry globalMatrix) {
        this.pos.transform(globalMatrix.getMatrix());
        this.norm.transform(globalMatrix.getNormal());
	}
}
