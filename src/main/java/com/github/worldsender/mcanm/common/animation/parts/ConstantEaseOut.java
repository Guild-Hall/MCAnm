package com.github.worldsender.mcanm.common.animation.parts;

import org.lwjgl.util.vector.Vector2f;

import java.io.DataInputStream;

public class ConstantEaseOut extends Spline {
    public static final IEaseOutSplineFactory factory = new IEaseOutSplineFactory() {
        @Override
        public Spline newSpline(Vector2f left, DataInputStream additionalData) {
            return new ConstantEaseOut(left);
        }
    };

    private Vector2f left;

    public ConstantEaseOut(Vector2f left) {
        this.left = left;
    }

    @Override
    public boolean isInRange(float frame) {
        return frame >= left.x;
    }

    @Override
    public float getValueAt(float frame) {
        return left.y;
    }
}
