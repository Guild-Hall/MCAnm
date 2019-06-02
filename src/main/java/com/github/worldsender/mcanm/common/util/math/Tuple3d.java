package com.github.worldsender.mcanm.common.util.math;


public abstract class Tuple3d {
	
    public double x;
    public double y;
    public double z;
    
    public Tuple3d(double x, double y, double z)
    {
	this.x = x;
	this.y = y;
	this.z = z;
    }
    public Tuple3d(double[] t)
    {
	this.x = t[0];
	this.y = t[1];
	this.z = t[2];
    }
    public Tuple3d(Tuple3d t1)
    {
	this.x = t1.x;
	this.y = t1.y;
	this.z = t1.z;
    }
    public Tuple3d(Tuple3f t1)
    {
	this.x = (double) t1.x;
	this.y = (double) t1.y;
	this.z = (double) t1.z;
    }

    public Tuple3d()
    {
	this.x = (double) 0.0;
	this.y = (double) 0.0;
	this.z = (double) 0.0;
    }

    public Tuple3d(Point3f p1) {
	}
	public final void set(double x, double y, double z)
    {
	this.x = x;
	this.y = y;
	this.z = z;
    }

    public final void set(double[] t)
    {
	this.x = t[0];
	this.y = t[1];
	this.z = t[2];
    }

    public final void set(Tuple3d t1)
    {
	this.x = t1.x;
	this.y = t1.y;
	this.z = t1.z;
    }

    public final void set(Tuple3f t1)
    {
	this.x = (double) t1.x;
	this.y = (double) t1.y;
	this.z = (double) t1.z;
    }

    public final void get(double[] t)
    {
        t[0] = this.x;
        t[1] = this.y;
        t[2] = this.z;
    }

    public final void get(Tuple3d t)
    {
        t.x = this.x;
        t.y = this.y;
        t.z = this.z;
    }

    public final void add(Tuple3d t1, Tuple3d t2)
    {
	this.x = t1.x + t2.x;
	this.y = t1.y + t2.y;
	this.z = t1.z + t2.z;
    }

    public final void add(Tuple3d t1)
    { 
        this.x += t1.x;
        this.y += t1.y;
        this.z += t1.z;
    }
    public final void sub(Tuple3d t1, Tuple3d t2)
    {
	this.x = t1.x - t2.x;
	this.y = t1.y - t2.y;
	this.z = t1.z - t2.z;
    }
    public final void sub(Tuple3d t1)
    { 
        this.x -= t1.x;
        this.y -= t1.y;
        this.z -= t1.z;
    }

    public final void negate(Tuple3d t1)
    {
	this.x = -t1.x;
	this.y = -t1.y;
	this.z = -t1.z;
    }

    public final void negate()
    {
	this.x = -this.x;
	this.y = -this.y;
	this.z = -this.z;
    }

    public final void scale(double s, Tuple3d t1)
    {
	this.x = s*t1.x;
	this.y = s*t1.y;
	this.z = s*t1.z;
    }

    public final void scale(double s)
    {
        this.x *= s;
        this.y *= s;
        this.z *= s;
    }

    public final void scaleAdd(double s, Tuple3d t1, Tuple3d t2)
    {
	this.x = s*t1.x + t2.x;
	this.y = s*t1.y + t2.y;
	this.z = s*t1.z + t2.z;
    }

    public final void scaleAdd(double s, Tuple3f t1) {
	scaleAdd(s, new Point3d(t1));
    }

    public final void scaleAdd(double s, Tuple3d t1) {
        this.x = s*this.x + t1.x;
        this.y = s*this.y + t1.y;
        this.z = s*this.z + t1.z;
    }

    public String toString() {
        return "(" + this.x + ", " + this.y + ", " + this.z + ")";
    }


 

}
