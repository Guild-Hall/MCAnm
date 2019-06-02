package com.github.worldsender.mcanm.common.util.math;

class VecMathUtil {

	static int floatToIntBits(float f) {
		// Check for +0 or -0
		if (f == 0.0f) {
			return 0;
		} else {
			return Float.floatToIntBits(f);
		}
	}

	static long doubleToLongBits(double d) {
		// Check for +0 or -0
		if (d == 0.0) {
			return 0L;
		} else {
			return Double.doubleToLongBits(d);
		}
	}
	
    private VecMathUtil() {
    }
}
