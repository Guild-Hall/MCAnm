package com.github.worldsender.mcanm.common.util.math;

import java.nio.FloatBuffer;

public class Vector2f extends Tuple2f{
	public float x;
	public float y;
	
	public Vector2f(){
		x = 0;
		y = 0;
	}
	
	public Vector2f(double x, double y){
		this.x = (float) x;
		this.y = (float) y;
	}

	public Vector2f(float x, float y) {
		this.x = x;
		this.y = y;
	}
	
	public Vector2f(Vector2f other)
	{
		this(other.x, other.y);
	}
	
	public void set(double x, double y)
	{
		this.x = (float) x;
		this.y = (float) y;
	}


	public void set(Vector2f vec)
	{
		x = vec.x;
		y = vec.y;
	}

	public void add(Vector2f vec)
	{
		x += vec.x;
		y += vec.y;
	}

	public void sub(Vector2f vec)
	{
		x -= vec.x;
		y -= vec.y;
	}


	public void scale(double s)
	{
		x *= s;
		y *= s;
	}

	public void normalize()
	{
		double scale = 1.0 / Math.sqrt(x * x + y * y);
		scale(scale);
	}

	public double lengthSquared()
	{
		return x * x + y * y;
	}

	public double length()
	{
		return Math.sqrt(lengthSquared());
	}

	public double distanceSquared(Vector2f v)
	{
		double dx, dy;
		dx = x - v.x;
		dy = y - v.y;
		return (dx * dx + dy * dy);
	}

	public double distance(Vector2f v)
	{
		return Math.sqrt(distanceSquared(v));
	}
	
	public Vector2f translate(float x, float y) {
		this.x += x;
		this.y += y;
		return this;
	}
	
	public Vector2f negate(Vector2f dest) {
		if (dest == null)
			dest = new Vector2f();
		dest.x = -x;
		dest.y = -y;
		return dest;
	}
	
	public Vector2f store(FloatBuffer buf) {
		buf.put(x);
		buf.put(y);
		return this;
	}

	public Vector2f load(FloatBuffer buf) {
		x = buf.get();
		y = buf.get();
		return this;
	}

	@Override
	public String toString()
	{
		return "Vector2f(" + x + ", " + y + ")";
	}
}
