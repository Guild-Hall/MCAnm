package com.github.worldsender.mcanm.common.util.math;

public class Point4d extends Tuple4d {

    public Point4d(double x, double y, double z, double w) {
        super(x, y, z, w);
    }

    public Point4d(double[] p) {
        super(p);
    }

    public Point4d(Point4d p1) {
        super(p1);
    }

    public Point4d(Point4f p1) {
        super(p1);
    }

    public Point4d(Tuple4f t1) {
        super(t1);
    }

    public Point4d(Tuple4d t1) {
        super(t1);
    }

    public Point4d(Tuple3d t1) {
        super(t1.x, t1.y, t1.z, 1.0);
    }

    public Point4d() {
        super();
    }

    public final void set(Tuple3d t1) {
        this.x = t1.x;
        this.y = t1.y;
        this.z = t1.z;
        this.w = 1.0;
    }

    public final double distanceSquared(Point4d p1) {
        double dx, dy, dz, dw;

        dx = this.x - p1.x;
        dy = this.y - p1.y;
        dz = this.z - p1.z;
        dw = this.w - p1.w;
        return (dx * dx + dy * dy + dz * dz + dw * dw);
    }

    public final double distance(Point4d p1) {
        double dx, dy, dz, dw;

        dx = this.x - p1.x;
        dy = this.y - p1.y;
        dz = this.z - p1.z;
        dw = this.w - p1.w;
        return Math.sqrt(dx * dx + dy * dy + dz * dz + dw * dw);
    }

    public final double distanceL1(Point4d p1) {
        return Math.abs(this.x - p1.x) + Math.abs(this.y - p1.y) + Math.abs(this.z - p1.z) + Math.abs(this.w - p1.w);
    }

    public final double distanceLinf(Point4d p1) {
        double t1, t2;
        t1 = Math.max(Math.abs(this.x - p1.x), Math.abs(this.y - p1.y));
        t2 = Math.max(Math.abs(this.z - p1.z), Math.abs(this.w - p1.w));

        return Math.max(t1, t2);
    }

    public final void project(Point4d p1) {
        double oneOw;

        oneOw = 1 / p1.w;
        x = p1.x * oneOw;
        y = p1.y * oneOw;
        z = p1.z * oneOw;
        w = 1.0;

    }

}
