package com.github.worldsender.mcanm.common.util.math;


public class Tuple4d {

    public double x;
    public double y;
    public double z;
    public double w;

    public Tuple4d(double x, double y, double z, double w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public Tuple4d(double[] t) {
        this.x = t[0];
        this.y = t[1];
        this.z = t[2];
        this.w = t[3];
    }

    public Tuple4d(Tuple4d t1) {
        this.x = t1.x;
        this.y = t1.y;
        this.z = t1.z;
        this.w = t1.w;
    }

    public Tuple4d(Tuple4f t1) {
        this.x = t1.x;
        this.y = t1.y;
        this.z = t1.z;
        this.w = t1.w;
    }

    public Tuple4d() {
        this.x = 0.0;
        this.y = 0.0;
        this.z = 0.0;
        this.w = 0.0;
    }

    public final void set(double x, double y, double z, double w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public final void set(double[] t) {
        this.x = t[0];
        this.y = t[1];
        this.z = t[2];
        this.w = t[3];
    }

    public final void set(Tuple4d t1) {
        this.x = t1.x;
        this.y = t1.y;
        this.z = t1.z;
        this.w = t1.w;
    }

    public final void set(Tuple4f t1) {
        this.x = t1.x;
        this.y = t1.y;
        this.z = t1.z;
        this.w = t1.w;
    }

    public final void get(double[] t) {
        t[0] = this.x;
        t[1] = this.y;
        t[2] = this.z;
        t[3] = this.w;
    }

    public final void get(Tuple4d t) {
        t.x = this.x;
        t.y = this.y;
        t.z = this.z;
        t.w = this.w;
    }

    public final void add(Tuple4d t1, Tuple4d t2) {
        this.x = t1.x + t2.x;
        this.y = t1.y + t2.y;
        this.z = t1.z + t2.z;
        this.w = t1.w + t2.w;
    }

    public final void add(Tuple4d t1) {
        this.x += t1.x;
        this.y += t1.y;
        this.z += t1.z;
        this.w += t1.w;
    }

    public final void sub(Tuple4d t1, Tuple4d t2) {
        this.x = t1.x - t2.x;
        this.y = t1.y - t2.y;
        this.z = t1.z - t2.z;
        this.w = t1.w - t2.w;
    }

    public final void sub(Tuple4d t1) {
        this.x -= t1.x;
        this.y -= t1.y;
        this.z -= t1.z;
        this.w -= t1.w;
    }

    public final void negate(Tuple4d t1) {
        this.x = -t1.x;
        this.y = -t1.y;
        this.z = -t1.z;
        this.w = -t1.w;
    }

    public final void negate() {
        this.x = -this.x;
        this.y = -this.y;
        this.z = -this.z;
        this.w = -this.w;
    }

    public final void scale(double s, Tuple4d t1) {
        this.x = s * t1.x;
        this.y = s * t1.y;
        this.z = s * t1.z;
        this.w = s * t1.w;
    }

    public final void scale(double s) {
        this.x *= s;
        this.y *= s;
        this.z *= s;
        this.w *= s;
    }

    public final void scaleAdd(double s, Tuple4d t1, Tuple4d t2) {
        this.x = s * t1.x + t2.x;
        this.y = s * t1.y + t2.y;
        this.z = s * t1.z + t2.z;
        this.w = s * t1.w + t2.w;
    }

    public final void scaleAdd(double s, Tuple4d t1) {
        this.x = s * this.x + t1.x;
        this.y = s * this.y + t1.y;
        this.z = s * this.z + t1.z;
        this.w = s * this.w + t1.w;
    }

    public String toString() {
        return "(" + this.x + ", " + this.y + ", " + this.z + ", " + this.w + ")";
    }

    public boolean equals(Tuple4d t1) {
        try {
            return (this.x == t1.x && this.y == t1.y && this.z == t1.z && this.w == t1.w);
        } catch (NullPointerException e2) {
            return false;
        }
    }

    public boolean equals(Object t1) {
        try {

            Tuple4d t2 = (Tuple4d) t1;
            return (this.x == t2.x && this.y == t2.y && this.z == t2.z && this.w == t2.w);
        } catch (NullPointerException e2) {
            return false;
        } catch (ClassCastException e1) {
            return false;
        }
    }

    public boolean epsilonEquals(Tuple4d t1, double epsilon) {
        double diff;

        diff = x - t1.x;
        if (Double.isNaN(diff))
            return false;
        if ((diff < 0 ? -diff : diff) > epsilon)
            return false;

        diff = y - t1.y;
        if (Double.isNaN(diff))
            return false;
        if ((diff < 0 ? -diff : diff) > epsilon)
            return false;

        diff = z - t1.z;
        if (Double.isNaN(diff))
            return false;
        if ((diff < 0 ? -diff : diff) > epsilon)
            return false;

        diff = w - t1.w;
        if (Double.isNaN(diff))
            return false;
        if ((diff < 0 ? -diff : diff) > epsilon)
            return false;

        return true;

    }

    public int hashCode() {
        long bits = 1L;
        bits = 31L * bits + VecMathUtil.doubleToLongBits(x);
        bits = 31L * bits + VecMathUtil.doubleToLongBits(y);
        bits = 31L * bits + VecMathUtil.doubleToLongBits(z);
        bits = 31L * bits + VecMathUtil.doubleToLongBits(w);
        return (int) (bits ^ (bits >> 32));
    }

    public final void clamp(double min, double max, Tuple4d t) {
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

        if (t.w > max) {
            w = max;
        } else if (t.w < min) {
            w = min;
        } else {
            w = t.w;
        }

    }

    public final void clampMin(double min, Tuple4d t) {
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

        if (t.w < min) {
            w = min;
        } else {
            w = t.w;
        }

    }

    public final void clampMax(double max, Tuple4d t) {
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

        if (t.w > max) {
            w = max;
        } else {
            w = t.z;
        }

    }

    public final void absolute(Tuple4d t) {
        x = Math.abs(t.x);
        y = Math.abs(t.y);
        z = Math.abs(t.z);
        w = Math.abs(t.w);

    }

    public final void clamp(double min, double max) {
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

        if (w > max) {
            w = max;
        } else if (w < min) {
            w = min;
        }

    }

    public final void clampMin(double min) {
        if (x < min)
            x = min;
        if (y < min)
            y = min;
        if (z < min)
            z = min;
        if (w < min)
            w = min;
    }

    public final void clampMax(double max) {
        if (x > max)
            x = max;
        if (y > max)
            y = max;
        if (z > max)
            z = max;
        if (w > max)
            w = max;

    }

    public final void absolute() {
        x = Math.abs(x);
        y = Math.abs(y);
        z = Math.abs(z);
        w = Math.abs(w);

    }

    public void interpolate(Tuple4d t1, Tuple4d t2, double alpha) {
        this.x = (1 - alpha) * t1.x + alpha * t2.x;
        this.y = (1 - alpha) * t1.y + alpha * t2.y;
        this.z = (1 - alpha) * t1.z + alpha * t2.z;
        this.w = (1 - alpha) * t1.w + alpha * t2.w;
    }

    public void interpolate(Tuple4d t1, double alpha) {
        this.x = (1 - alpha) * this.x + alpha * t1.x;
        this.y = (1 - alpha) * this.y + alpha * t1.y;
        this.z = (1 - alpha) * this.z + alpha * t1.z;
        this.w = (1 - alpha) * this.w + alpha * t1.w;
    }

    public final double getX() {
        return x;
    }

    public final void setX(double x) {
        this.x = x;
    }

    public final double getY() {
        return y;
    }

    public final void setY(double y) {
        this.y = y;
    }

    public final double getZ() {
        return z;
    }

    public final void setZ(double z) {
        this.z = z;
    }

    public final double getW() {
        return w;
    }

    public final void setW(double w) {
        this.w = w;
    }

}
