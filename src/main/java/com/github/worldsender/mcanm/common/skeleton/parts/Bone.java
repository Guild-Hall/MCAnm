package com.github.worldsender.mcanm.common.skeleton.parts;

import java.util.Objects;

import javax.annotation.Nullable;

import com.github.worldsender.mcanm.common.Utils;
import com.github.worldsender.mcanm.common.skeleton.IBone;
import com.github.worldsender.mcanm.common.util.math.Matrix4f;
import com.github.worldsender.mcanm.common.util.math.Point4f;
import com.github.worldsender.mcanm.common.util.math.Quat4f;
import com.github.worldsender.mcanm.common.util.math.Vector3f;
import com.github.worldsender.mcanm.common.util.math.Vector4f;

public class Bone implements IBone {
    private static final Matrix4f identity = new Matrix4f();

    static {
        identity.setIdentity();
    }

    public final @Nullable Bone parent;
    public final String name;
    /** transform from bone-local coordinates to global coordinates */
    public final Matrix4f localToGlobal;
    /** inverse of localToGlobal */
    public final Matrix4f globalToLocal;
    /**
     * matrix transforming from transformed coordinates to object coordinates.
     * 
     * Important for calculations: if a point is transformed by a bone, then its transformed coordinates in the
     * transformed local bone coordinate system will be the as the untransformed coordinates in the untransformed local
     * bone coordinate.
     * 
     * I.e. if a point P in object coordinates is given, then first calculate P' = globalToLocal * P, and then calculate
     * P_trans = transformedLocalToGlobal * P'.
     * 
     * Thus,
     * 
     * <pre>
     *P_trans = (transformedLocalToGlobal * globalToLocal) * P
     *        =: globalToTransformedGlobal * P
     * </pre>
     * 
     */
    public final Matrix4f globalToTransformedGlobal = new Matrix4f();
    /** for normals calculation, use this instead. */
    public final Matrix4f globalToTransformedGlobalNormal = new Matrix4f();

    protected Bone(Matrix4f localMatrix, String name, Bone parent) {
        this.parent = parent;
        this.localToGlobal = new Matrix4f(localMatrix);
        this.globalToLocal = new Matrix4f(localMatrix);
        this.globalToLocal.invert();
        this.name = Objects.requireNonNull(name);
    }

    /**
     * Debug!
     *
     * @return the head of the bone
     */
    public Vector4f getHead() {
        Vector4f head = new Vector4f();
        head.w = 1.0f;
        localToGlobal.transform(head);
        globalToTransformedGlobal.transform(head);
        return head;
    }

    /**
     * Debug!
     *
     * @return the tail of the bone
     */
    public Vector4f getTail() {
        Vector4f tail = new Vector4f();
        tail.y = 1.0f;
        tail.w = 1.0f;
        localToGlobal.transform(tail);
        globalToTransformedGlobal.transform(tail);
        return tail;
    }

    public void resetTransform() {
        // We set everything to the *identity* so 
        globalToTransformedGlobal.set(identity);
        globalToTransformedGlobalNormal.set(identity);
    }

    /**
     * Transforms the source matrix into local space and stores the resulting matrix back in the source.<br>
     *
     * @param src the matrix to transform
     */
    protected void globalToLocal(Matrix4f src) {
        src.mul(globalToLocal, src);
    }

    /**
     * Transforms the position given by the transformation currently acted out by this bone.
     *
     * @param position the position to transform
     */
    @Override
    public void transform(Point4f position) {
        globalToTransformedGlobal.transform(position);
    }

    @Override
    public void transform(Matrix4f matrix) {
        matrix.mul(globalToTransformedGlobal, matrix);
    }

    /**
     * Transforms the normal given by the transformation currently acted out by this bone.
     *
     * @param position the position to transform
     */
    @Override
    public void transformNormal(Vector3f normal) {
        globalToTransformedGlobalNormal.transform(normal);
    }

    public static class BoneBuilder {
        private Quat4f rotation;
        private Vector3f offset;
        private Bone parent;
        private String name;

        public BoneBuilder() {
            this.rotation = new Quat4f();
            this.offset = new Vector3f();
            reset();
        }

        public BoneBuilder(String name) {
            this();
            setName(name);
        }

        public BoneBuilder setName(String name) {
            this.name = Objects.requireNonNull(name);
            return this;
        }

        public BoneBuilder setOffset(Vector3f offset) {
            this.offset.set(offset);
            return this;
        }

        public BoneBuilder setRotation(Quat4f rotation) {
            this.rotation.set(rotation);
            return this;
        }

        public BoneBuilder setParent(Bone parent) {
            this.parent = parent;
            return this;
        }

        private void reset() {
            this.parent = null;
            this.name = null;
            this.offset.set(0, 0, 0);
            this.rotation.set(0, 0, 0, 0);
        }

        public Bone build() {
            Matrix4f localToGlobal = Utils.fromRTS(rotation, offset, 1.0F, new Matrix4f());
            if (parent != null) {
                localToGlobal.mul(parent.localToGlobal, localToGlobal);
            }
            return new Bone(localToGlobal, name, parent);
        }
    }
}
