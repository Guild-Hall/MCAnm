package com.github.worldsender.mcanm.common.animation.parts;


import java.io.DataInputStream;

import com.github.worldsender.mcanm.common.util.math.Vector2f;



/**
 * Constant ease describes a horizontal line that has the value of it's right end point and also ends there, something
 * about this:<br>
 * ------------------------- <br>
 *
 * @author WorldSEnder
 */
public class ConstantEaseIn extends Spline {
    public static final IEaseInSplineFactory factory = new IEaseInSplineFactory() {
        @Override
        public Spline newSpline(Vector2f right, DataInputStream additionalData) {
            return new ConstantEaseIn(right);
        }
    };
    private Vector2f right;

    private ConstantEaseIn(Vector2f right) {
        this.right = right;
    }

    @Override
    public boolean isInRange(float frame) {
        return frame <= this.right.x;
    }

    @Override
    public float getValueAt(float frame) {
        return this.right.y;
    }
}
