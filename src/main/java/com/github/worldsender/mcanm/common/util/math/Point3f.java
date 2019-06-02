package com.github.worldsender.mcanm.common.util.math;


public class Point3f extends Tuple3f {

    public Point3f(float x, float y, float z) {
        super(x, y, z);
    }

    public Point3f(float[] p) {
        super(p);
    }
    

    public Point3f(Point3f p1) {
        super(p1);
    }

    public Point3f(Point3d p1) {
        super(p1);
    }

    public Point3f(Tuple3f t1) {
        super(t1);
    }

    public Point3f(Tuple3d t1) {
        super(t1);
    }

    public Point3f() {
        super();
    }

    public final float distanceSquared(Point3f p1) {
        float dx, dy, dz;

        dx = this.x - p1.x;
        dy = this.y - p1.y;
        dz = this.z - p1.z;
        return dx * dx + dy * dy + dz * dz;
    }

    public final float distance(Point3f p1) {
        float dx, dy, dz;

        dx = this.x - p1.x;
        dy = this.y - p1.y;
        dz = this.z - p1.z;
        return (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    public final float distanceL1(Point3f p1) {
        return (Math.abs(this.x - p1.x) + Math.abs(this.y - p1.y) + Math.abs(this.z - p1.z));
    }

    public final float distanceLinf(Point3f p1) {
        float tmp;
        tmp = Math.max(Math.abs(this.x - p1.x), Math.abs(this.y - p1.y));
        return (Math.max(tmp, Math.abs(this.z - p1.z)));

    }

    public final void project(Point4f p1) {
        float oneOw;

        oneOw = 1 / p1.w;
        x = p1.x * oneOw;
        y = p1.y * oneOw;
        z = p1.z * oneOw;

    }
}
