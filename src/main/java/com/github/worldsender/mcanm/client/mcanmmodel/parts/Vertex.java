package com.github.worldsender.mcanm.client.mcanmmodel.parts;

import com.github.worldsender.mcanm.common.util.math.Point2f;
import com.github.worldsender.mcanm.common.util.math.Point4f;
import com.github.worldsender.mcanm.common.util.math.Tuple2f;
import com.github.worldsender.mcanm.common.util.math.Tuple3f;
import com.github.worldsender.mcanm.common.util.math.Tuple4f;
import com.github.worldsender.mcanm.common.util.math.Vector2f;
import com.github.worldsender.mcanm.common.util.math.Vector3f;
import com.github.worldsender.mcanm.common.util.math.Vector4f;

import net.minecraft.client.renderer.BufferBuilder;


public class Vertex {

    private Point4f pos;
    private Vector3f norm;
    private Point2f uv;

    public Vertex(Tuple4f pos, Vector3f norm2, Vector2f uv2) {
        this.pos = new Point4f(pos);
        this.norm = new Vector3f(norm2);
        this.uv = new Point2f(uv2);
    }

    public Vertex(Vertex copyFrom) {
        this.pos = new Point4f(copyFrom.pos);
        this.norm = new Vector3f(copyFrom.norm);
        this.uv = new Point2f(copyFrom.uv);
    }

    public static void renderTriangle(Vertex v1, Vertex v2, Vertex v3, BufferBuilder buffer) {
        float v1x = v1.pos.x / v1.pos.w, //
                v1y = v1.pos.z / v1.pos.w, //
                v1z = -v1.pos.y / v1.pos.w;
        float v2x = v2.pos.x / v2.pos.w, //
                v2y = v2.pos.z / v2.pos.w, //
                v2z = -v2.pos.y / v2.pos.w;
        float v3x = v3.pos.x / v3.pos.w, //
                v3y = v3.pos.z / v3.pos.w, //
                v3z = -v3.pos.y / v3.pos.w;
        float v31x = v3x - v1x, v21x = v2x - v1x;
        float v31y = v3y - v1y, v21y = v2y - v1y;
        float v31z = v3z - v1z, v21z = v2z - v1z;
        float nx = v31y * v21z - v31z * v21y;
        float ny = v31z * v21x - v31x * v21z;
        float nz = v31x * v21y - v31y * v21x;
        float nscale = (float) (1 / Math.sqrt(nx * nx + ny * ny + nz * nz));
        buffer.pos(v1x, v1y, v1z) //
                .tex(v1.uv.x, v1.uv.y) //
                .normal(nx * nscale, ny * nscale, nz * nscale) //
                .endVertex();
        buffer.pos(v2x, v2y, v2z) //
                .tex(v2.uv.x, v2.uv.y) //
                .normal(nx * nscale, ny * nscale, nz * nscale) //
                .endVertex();
        buffer.pos(v3x, v3y, v3z) //
                .tex(v3.uv.x, v3.uv.y) //
                .normal(nx * nscale, ny * nscale, nz * nscale) //
                .endVertex();
    }

    public void debugRender(BufferBuilder renderer) {
        renderer.pos(pos.x / pos.w, pos.z / pos.w, -pos.y / pos.w).endVertex();
        float normScale = 0.1f;
        renderer.pos(
                pos.x / pos.w + norm.x * normScale,
                pos.z / pos.w + norm.z * normScale,
                -pos.y / pos.w - norm.y * normScale).endVertex();
    }

    /**
     * Offsets this Vertex by the {@link Vector4f} given.
     *
     * @param vector the offset
     */
    public void offset(Point4f point) {
        this.pos.add(point);
    }

    /**
     * Adds the normal to this Vertex basically interpolating between the current normal and the given one (by their
     * scale). No normalization is done as this is taken care of by OpenGL during rendering.
     *
     * @param normal the normal to add
     */
    public void addNormal(Vector3f normal) {
        this.norm.add(normal);
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
        trgt.set(this.norm);
    }

    /**
     * Stores this vertex's position in the target.
     */
    public void getPosition(Tuple4f trgt) {
        trgt.set(this.pos);
    }

    public void retainUVOnly() {
        this.pos.set(0, 0, 0, 0);
        this.norm.set(0, 0, 0);
    }
}
