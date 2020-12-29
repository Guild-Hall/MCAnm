package com.github.worldsender.mcanm.common.skeleton.stored.parts;

import com.github.worldsender.mcanm.common.exceptions.ModelFormatException;
import com.github.worldsender.mcanm.common.skeleton.visitor.IConstraintVisitor;

public final class CopyRotation implements IConstraintVisitable {
    public static enum CoordinateSystem {
        /**
         * Local coordinates for a pose are the bone's own transformation. A bone is in its rest position iff this is
         * the identity transformation.
         */
        LOCAL,
        /**
         * Local with parent coordinates for a pose are the bone's total transform, after parenting has been applied,
         * relative to its position when the whole skeleton is in its rest pose.
         */
        LOCAL_WITH_PARENT,
        /**
         * Pose coordinates are the bone's transformation in the skeleton's local coordinate system, disregarding rest
         * poses.
         */
        POSE;

        public static CoordinateSystem decode(int coded) throws ModelFormatException {
            // compare encoding in "export.py"
            switch (coded) {
            case 0:
                return CoordinateSystem.LOCAL;
            case 1:
                return CoordinateSystem.LOCAL_WITH_PARENT;
            case 2:
                return CoordinateSystem.POSE;
            }
            throw new ModelFormatException("unrecognized coordinate system: " + coded);
        }
    }

    public static enum MixMode {
        /**
         * Replace the transformation given by pose + parenting
         */
        REPLACE,
        /**
         * Add the copied rotation before the one from pose + parenting
         */
        BEFORE,
        /**
         * Add the copied rotation on top of the one from pose + parenting
         */
        AFTER;

        public static MixMode decode(int coded) throws ModelFormatException {
            // compare encoding in "export.py"
            switch (coded) {
            case 0:
                return MixMode.REPLACE;
            case 1:
                return MixMode.BEFORE;
            case 2:
                return MixMode.AFTER;
            }
            throw new ModelFormatException("unrecognized coordinate system: " + coded);
        }
    }

    public float influence;

    /** Target = the bone to copy from */
    public int targetBoneIdx;
    public CoordinateSystem targetSystem;

    public int controlledBoneIdx;
    public CoordinateSystem controlledSystem;

    public boolean useX, useY, useZ, invertX, invertY, invertZ;
    public MixMode mixMode;

    public CopyRotation() {
        this.influence = 1.f;
        this.targetBoneIdx = -1;
        this.targetSystem = CoordinateSystem.LOCAL;
        this.controlledBoneIdx = -1;
        this.controlledSystem = CoordinateSystem.LOCAL;
        this.useX = this.useY = this.useZ = true;
        this.invertX = this.invertY = this.invertZ = false;
        this.mixMode = MixMode.REPLACE;
    }

    @Override
    public void visitBy(IConstraintVisitor visitor) {
        visitor.visitCopyRotiation(this);
    }
}
