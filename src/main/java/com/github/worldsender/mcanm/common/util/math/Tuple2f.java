package com.github.worldsender.mcanm.common.util.math;


public class Tuple2f {
    public float x;
    public float y;

    public Tuple2f(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Tuple2f(float[] t) {
        this.x = t[0];
        this.y = t[1];
    }

    public Tuple2f(Tuple2f t1) {
        this.x = t1.x;
        this.y = t1.y;
    }

    public Tuple2f(Tuple2d t1) {
        this.x = (float) t1.x;
        this.y = (float) t1.y;
    }

    public Tuple2f() {
        this.x = (float) 0.0;
        this.y = (float) 0.0;
    }

    public final void set(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public final void set(float[] t) {
        this.x = t[0];
        this.y = t[1];
    }

    public final void set(Tuple2f t1) {
        this.x = t1.x;
        this.y = t1.y;
    }

    public final void set(Tuple2d t1) {
        this.x = (float) t1.x;
        this.y = (float) t1.y;
    }

    public final void get(float[] t) {
        t[0] = this.x;
        t[1] = this.y;
    }

    public final void add(Tuple2f t1, Tuple2f t2) {
        this.x = t1.x + t2.x;
        this.y = t1.y + t2.y;
    }

    public final void add(Tuple2f t1) {
        this.x += t1.x;
        this.y += t1.y;
    }

    public final void sub(Tuple2f t1, Tuple2f t2) {
        this.x = t1.x - t2.x;
        this.y = t1.y - t2.y;
    }

    public final void sub(Tuple2f t1) {
        this.x -= t1.x;
        this.y -= t1.y;
    }

    public final void negate(Tuple2f t1) {
        this.x = -t1.x;
        this.y = -t1.y;
    }

    public final void negate() {
        this.x = -this.x;
        this.y = -this.y;
    }

    public final void scale(float s, Tuple2f t1) {
        this.x = s * t1.x;
        this.y = s * t1.y;
    }

    public final void scale(float s) {
        this.x *= s;
        this.y *= s;
    }

    public final void scaleAdd(float s, Tuple2f t1, Tuple2f t2) {
        this.x = s * t1.x + t2.x;
        this.y = s * t1.y + t2.y;
    }

    public final void scaleAdd(float s, Tuple2f t1) {
        this.x = s * this.x + t1.x;
        this.y = s * this.y + t1.y;
    }

    public int hashCode() {
        long bits = 1L;
        bits = 31L * bits + (long) VecMathUtil.floatToIntBits(x);
        bits = 31L * bits + (long) VecMathUtil.floatToIntBits(y);
        return (int) (bits ^ (bits >> 32));
    }

    public boolean equals(Tuple2f t1) {
        try {
            return (this.x == t1.x && this.y == t1.y);
        } catch (NullPointerException e2) {
            return false;
        }

    }

    public boolean equals(Object t1) {
        try {
            Tuple2f t2 = (Tuple2f) t1;
            return (this.x == t2.x && this.y == t2.y);
        } catch (NullPointerException e2) {
            return false;
        } catch (ClassCastException e1) {
            return false;
        }

    }

    public boolean epsilonEquals(Tuple2f t1, float epsilon) {
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

        return true;
    }

    public String toString() {
        return ("(" + this.x + ", " + this.y + ")");
    }

    public final void clamp(float min, float max, Tuple2f t) {
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

    }

    public final void clampMin(float min, Tuple2f t) {
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

    }

    public final void clampMax(float max, Tuple2f t) {
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

    }

    public final void absolute(Tuple2f t) {
        x = Math.abs(t.x);
        y = Math.abs(t.y);
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

    }

    public final void clampMin(float min) {
        if (x < min)
            x = min;
        if (y < min)
            y = min;
    }

    public final void clampMax(float max) {
        if (x > max)
            x = max;
        if (y > max)
            y = max;
    }

    public final void absolute() {
        x = Math.abs(x);
        y = Math.abs(y);
    }

    public final void interpolate(Tuple2f t1, Tuple2f t2, float alpha) {
        this.x = (1 - alpha) * t1.x + alpha * t2.x;
        this.y = (1 - alpha) * t1.y + alpha * t2.y;

    }

    public final void interpolate(Tuple2f t1, float alpha) {

        this.x = (1 - alpha) * this.x + alpha * t1.x;
        this.y = (1 - alpha) * this.y + alpha * t1.y;

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
}