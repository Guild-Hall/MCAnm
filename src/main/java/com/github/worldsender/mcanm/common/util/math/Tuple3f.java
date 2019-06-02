package com.github.worldsender.mcanm.common.util.math;


public class Tuple3f {

	public float x;
	public float y;
	public float z;

	public Tuple3f(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Tuple3f(float[] t) {
		this.x = t[0];
		this.y = t[1];
		this.z = t[2];
	}

	public Tuple3f(Tuple3f t1) {
		this.x = t1.x;
		this.y = t1.y;
		this.z = t1.z;
	}

	public Tuple3f(Tuple3d t1) {
		this.x = (float) t1.x;
		this.y = (float) t1.y;
		this.z = (float) t1.z;
	}

	public Tuple3f() {
		this.x = 0.0f;
		this.y = 0.0f;
		this.z = 0.0f;
	}

	public String toString() {
		return "(" + this.x + ", " + this.y + ", " + this.z + ")";
	}

	public final void set(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public final void set(float[] t) {
		this.x = t[0];
		this.y = t[1];
		this.z = t[2];
	}

	public final void set(Tuple3f t1) {
		this.x = t1.x;
		this.y = t1.y;
		this.z = t1.z;
	}

	public final void set(Tuple3d t1) {
		this.x = (float) t1.x;
		this.y = (float) t1.y;
		this.z = (float) t1.z;
	}

	public final void get(float[] t) {
		t[0] = this.x;
		t[1] = this.y;
		t[2] = this.z;
	}

	public final void get(Tuple3f t) {
		t.x = this.x;
		t.y = this.y;
		t.z = this.z;
	}

	public final void add(Tuple3f t1, Tuple3f t2) {
		this.x = t1.x + t2.x;
		this.y = t1.y + t2.y;
		this.z = t1.z + t2.z;
	}

	public final void add(Tuple3f t1) {
		this.x += t1.x;
		this.y += t1.y;
		this.z += t1.z;
	}

	public final void sub(Tuple3f t1, Tuple3f t2) {
		this.x = t1.x - t2.x;
		this.y = t1.y - t2.y;
		this.z = t1.z - t2.z;
	}

	public final void sub(Tuple3f t1) {
		this.x -= t1.x;
		this.y -= t1.y;
		this.z -= t1.z;
	}

	public final void negate(Tuple3f t1) {
		this.x = -t1.x;
		this.y = -t1.y;
		this.z = -t1.z;
	}

	public final void negate() {
		this.x = -this.x;
		this.y = -this.y;
		this.z = -this.z;
	}

	public final void scale(float s, Tuple3f t1) {
		this.x = s * t1.x;
		this.y = s * t1.y;
		this.z = s * t1.z;
	}

	public final void scale(float s) {
		this.x *= s;
		this.y *= s;
		this.z *= s;
	}

	public final void scaleAdd(float s, Tuple3f t1, Tuple3f t2) {
		this.x = s * t1.x + t2.x;
		this.y = s * t1.y + t2.y;
		this.z = s * t1.z + t2.z;
	}

	public final void scaleAdd(float s, Tuple3f t1) {
		this.x = s * this.x + t1.x;
		this.y = s * this.y + t1.y;
		this.z = s * this.z + t1.z;
	}

	public boolean equals(Tuple3f t1) {
		try {
			return (this.x == t1.x && this.y == t1.y && this.z == t1.z);
		} catch (NullPointerException e2) {
			return false;
		}
	}

	public boolean equals(Object t1) {
		try {
			Tuple3f t2 = (Tuple3f) t1;
			return (this.x == t2.x && this.y == t2.y && this.z == t2.z);
		} catch (NullPointerException e2) {
			return false;
		} catch (ClassCastException e1) {
			return false;
		}
	}

	public boolean epsilonEquals(Tuple3f t1, float epsilon) {
		float diff;

		diff = x - t1.x;
		if (Float.isNaN(diff))
			return false;
		if ((diff < 0 ? -diff : diff) > epsilon)
			return false;

		diff = y - t1.y;
		if (Float.isNaN(diff))
			return false;
		if ((diff < 0 ? -diff : diff) > epsilon)
			return false;

		diff = z - t1.z;
		if (Float.isNaN(diff))
			return false;
		if ((diff < 0 ? -diff : diff) > epsilon)
			return false;

		return true;

	}

	public int hashCode() {
		long bits = 1L;
		bits = 31L * bits + (long) VecMathUtil.floatToIntBits(x);
		bits = 31L * bits + (long) VecMathUtil.floatToIntBits(y);
		bits = 31L * bits + (long) VecMathUtil.floatToIntBits(z);
		return (int) (bits ^ (bits >> 32));
	}

	public final void clamp(float min, float max, Tuple3f t) {
		if (t.x > max) {
			x = max;
		} else if (t.x < min) {
			x = min;
		} else {
			x = t.x;
		}

		if (t.y > max) {
			y = max;
		} else if (t.y < min) {
			y = min;
		} else {
			y = t.y;
		}

		if (t.z > max) {
			z = max;
		} else if (t.z < min) {
			z = min;
		} else {
			z = t.z;
		}

	}

	public final void clampMin(float min, Tuple3f t) {
		if (t.x < min) {
			x = min;
		} else {
			x = t.x;
		}

		if (t.y < min) {
			y = min;
		} else {
			y = t.y;
		}

		if (t.z < min) {
			z = min;
		} else {
			z = t.z;
		}

	}

	public final void clampMax(float max, Tuple3f t) {
		if (t.x > max) {
			x = max;
		} else {
			x = t.x;
		}

		if (t.y > max) {
			y = max;
		} else {
			y = t.y;
		}

		if (t.z > max) {
			z = max;
		} else {
			z = t.z;
		}

	}

	public final void absolute(Tuple3f t) {
		x = Math.abs(t.x);
		y = Math.abs(t.y);
		z = Math.abs(t.z);
	}

	public final void clamp(float min, float max) {
		if (x > max) {
			x = max;
		} else if (x < min) {
			x = min;
		}

		if (y > max) {
			y = max;
		} else if (y < min) {
			y = min;
		}

		if (z > max) {
			z = max;
		} else if (z < min) {
			z = min;
		}

	}

	public final void clampMin(float min) {
		if (x < min)
			x = min;
		if (y < min)
			y = min;
		if (z < min)
			z = min;

	}

	public final void clampMax(float max) {
		if (x > max)
			x = max;
		if (y > max)
			y = max;
		if (z > max)
			z = max;

	}

	public final void absolute() {
		x = Math.abs(x);
		y = Math.abs(y);
		z = Math.abs(z);

	}

	public final void interpolate(Tuple3f t1, Tuple3f t2, float alpha) {
		this.x = (1 - alpha) * t1.x + alpha * t2.x;
		this.y = (1 - alpha) * t1.y + alpha * t2.y;
		this.z = (1 - alpha) * t1.z + alpha * t2.z;

	}

	public final void interpolate(Tuple3f t1, float alpha) {
		this.x = (1 - alpha) * this.x + alpha * t1.x;
		this.y = (1 - alpha) * this.y + alpha * t1.y;
		this.z = (1 - alpha) * this.z + alpha * t1.z;

	}

	public final float getX() {
		return x;
	}

	public final void setX(float x) {
		this.x = x;
	}

	public final float getY() {
		return y;
	}

	public final void setY(float y) {
		this.y = y;
	}

	public final float getZ() {
		return z;
	}

	public final void setZ(float z) {
		this.z = z;
	}

}
