package com.github.worldsender.mcanm.common.util.math;


public class Tuple2d {

    public	double	x;
    public	double	y;

    public Tuple2d(double x, double y)
    {
    this.x = x;
    this.y = y;
    }
    public Tuple2d(double[] t)
    {
    this.x = t[0];
    this.y = t[1];
    }

    public Tuple2d(Tuple2d t1)
    {
    this.x = t1.x;
    this.y = t1.y;
    }
    public Tuple2d(Tuple2f t1)
    {
    this.x = (double) t1.x;
    this.y = (double) t1.y;
    }
    public Tuple2d()
    {
    this.x = 0.0;
    this.y = 0.0;
    }

    public final void set(double x, double y)
    {
    this.x = x;
    this.y = y;
    }

    public final void set(double[] t)
    {
    this.x = t[0];
    this.y = t[1];
    }
    public final void set(Tuple2d t1)
    {
    this.x = t1.x;
    this.y = t1.y;
    }
 
    public final void set(Tuple2f t1)
    {
    this.x = (double) t1.x;
    this.y = (double) t1.y;
    }

   public final void get(double[] t)
    {
        t[0] = this.x;
        t[1] = this.y;
    }

    public final void add(Tuple2d t1, Tuple2d t2)
    {
    this.x = t1.x + t2.x;
    this.y = t1.y + t2.y;
    }

    public final void add(Tuple2d t1)
    {
        this.x += t1.x;
        this.y += t1.y;
    }

    public final void sub(Tuple2d t1, Tuple2d t2)
    {
        this.x = t1.x - t2.x;
        this.y = t1.y - t2.y;
    }  

    public final void sub(Tuple2d t1)
    {
        this.x -= t1.x;
        this.y -= t1.y;
    }

    public final void negate(Tuple2d t1)
    {
    this.x = -t1.x;
    this.y = -t1.y;
    }
    public final void negate()
    {
    this.x = -this.x;
    this.y = -this.y;
    }
    public final void scale(double s, Tuple2d t1)
    {
    this.x = s*t1.x;
    this.y = s*t1.y;
    }

    public final void scale(double s)
    {
    this.x *= s;
    this.y *= s;
    }

    public final void scaleAdd(double s, Tuple2d t1, Tuple2d t2)
    {
        this.x = s*t1.x + t2.x; 
        this.y = s*t1.y + t2.y; 
    } 
 
    public final void scaleAdd(double s, Tuple2d t1)
    {
        this.x = s*this.x + t1.x;
        this.y = s*this.y + t1.y;
    }


    public int hashCode() {
    long bits = 1L;
    bits = 31L * bits + VecMathUtil.doubleToLongBits(x);
    bits = 31L * bits + VecMathUtil.doubleToLongBits(y);
    return (int) (bits ^ (bits >> 32));
    }

    public boolean equals(Tuple2d t1)
    {
        try {
           return(this.x == t1.x && this.y == t1.y);
        }
        catch (NullPointerException e2) {return false;}

    }
    public boolean equals(Object t1)
    {
        try {
           Tuple2d t2 = (Tuple2d) t1;
           return(this.x == t2.x && this.y == t2.y);
        }
        catch (NullPointerException e2) {return false;}
        catch (ClassCastException   e1) {return false;}

    }

    public boolean epsilonEquals(Tuple2d t1, double epsilon)
    {
       double diff;

       diff = x - t1.x;
       if(Double.isNaN(diff)) return false;
       if((diff<0?-diff:diff) > epsilon) return false;

       diff = y - t1.y;
       if(Double.isNaN(diff)) return false;
       if((diff<0?-diff:diff) > epsilon) return false;

       return true;
    }
   public String toString()
   {
        return("(" + this.x + ", " + this.y + ")");
   }

   public final void clamp(double min, double max, Tuple2d t)
   {
        if( t.x > max ) { 
          x = max;
        } else if( t.x < min ){
          x = min;
        } else {
          x = t.x;
        }

        if( t.y > max ) { 
          y = max;
        } else if( t.y < min ){
          y = min;
        } else {
          y = t.y;
        }

   }

   public final void clampMin(double min, Tuple2d t) 
   { 
        if( t.x < min ) { 
          x = min;
        } else {
          x = t.x;
        }

        if( t.y < min ) { 
          y = min;
        } else {
          y = t.y;
        }

   } 

   public final void clampMax(double max, Tuple2d t)  
   {  
        if( t.x > max ) { 
          x = max;
        } else { 
          x = t.x;
        }
 
        if( t.y > max ) {
          y = max;
        } else {
          y = t.y;
        }

   } 


  public final void absolute(Tuple2d t)
  {
       x = Math.abs(t.x);
       y = Math.abs(t.y);
  } 


   public final void clamp(double min, double max)
   {
        if( x > max ) {
          x = max;
        } else if( x < min ){
          x = min;
        }
 
        if( y > max ) {
          y = max;
        } else if( y < min ){
          y = min;
        }

   }
   public final void clampMin(double min)
   { 
      if( x < min ) x=min;
      if( y < min ) y=min;
   } 
 
   public final void clampMax(double max)
   { 
      if( x > max ) x=max;
      if( y > max ) y=max;
   }
  public final void absolute()
  {
     x = Math.abs(x);
     y = Math.abs(y);
  }

  public final void interpolate(Tuple2d t1, Tuple2d t2, double alpha)
  {
       this.x = (1-alpha)*t1.x + alpha*t2.x;
       this.y = (1-alpha)*t1.y + alpha*t2.y;
  }

  public final void interpolate(Tuple2d t1, double alpha) 
  { 
       this.x = (1-alpha)*this.x + alpha*t1.x;
       this.y = (1-alpha)*this.y + alpha*t1.y;

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

}

