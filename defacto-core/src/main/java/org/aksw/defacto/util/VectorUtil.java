package org.aksw.defacto.util;

import java.util.Set;

/*************************************************************************
 *  Compilation:  javac VectorUtil.java
 *  Execution:    java VectorUtil
 *
 *  Implementation of a vector of real numbers.
 *
 *  This class is implemented to be immutable: once the client program
 *  initialize a VectorUtil, it cannot change any of its fields
 *  (N or data[i]) either directly or indirectly. Immutability is a
 *  very desirable feature of a data type.
 *
 *
 *  % java VectorUtil
 *  x        =  (1.0, 2.0, 3.0, 4.0)
 *  y        =  (5.0, 2.0, 4.0, 1.0)
 *  x + y    =  (6.0, 4.0, 7.0, 5.0)
 *  10x      =  (10.0, 20.0, 30.0, 40.0)
 *  |x|      =  5.477225575051661
 *  <x, y>   =  25.0
 *  |x - y|  =  5.0990195135927845
 *
 *  Note that java.util.Vector is an unrelated Java library class.
 *
 *  Copyright © 2000–2010, Robert Sedgewick and Kevin Wayne.
 *  Last updated: Wed Feb 9 09:07:43 EST 2011.
 *
 *  Original website : http://introcs.cs.princeton.edu/java/34nbody/VectorUtil.java.html
 *
 *************************************************************************/

public class VectorUtil {

    private final int N;         // length of the vector
    private double[] data;       // array of vector's components

    // create the zero vector of length N
    public VectorUtil(int N) {
        this.N = N;
        this.data = new double[N];
    }

    // create a vector from an array
    public VectorUtil(double[] data) {
        N = data.length;

        // defensive copy so that client can't alter our copy of data[]
        this.data = new double[N];
        for (int i = 0; i < N; i++)
            this.data[i] = data[i];
    }

    // create a vector from an array
    public VectorUtil(int[] data) {
        N = data.length;

        // defensive copy so that client can't alter our copy of data[]
        this.data = new double[N];
        for (int i = 0; i < N; i++)
            this.data[i] = data[i];
    }

    // create a vector from either an array or a vararg list
    // this constructor uses Java's vararg syntax to support
    // a constructor that takes a variable number of arguments, such as
    // VectorUtil x = new VectorUtil(1.0, 2.0, 3.0, 4.0);
    // VectorUtil y = new VectorUtil(5.0, 2.0, 4.0, 1.0);
/*
    public VectorUtil(double... data) {
        N = data.length;

        // defensive copy so that client can't alter our copy of data[]
        this.data = new double[N];
        for (int i = 0; i < N; i++)
            this.data[i] = data[i];
    }
*/
    // return the length of the vector
    public int length() {
        return N;
    }

    // return the inner product of this VectorUtil a and b
    public double dot(VectorUtil that) {
        if (this.N != that.N) throw new RuntimeException("Dimensions don't agree");
        double sum = 0.0;
        for (int i = 0; i < N; i++)
            sum = sum + (this.data[i] * that.data[i]);
        return sum;
    }

    // return the Euclidean norm of this VectorUtil
    public double magnitude() {
        return Math.sqrt(this.dot(this));
    }

    // return the Euclidean distance between this and that
    public double distanceTo(VectorUtil that) {
        if (this.N != that.N) throw new RuntimeException("Dimensions don't agree");
        return this.minus(that).magnitude();
    }

    // return this + that
    public VectorUtil plus(VectorUtil that) {
        if (this.N != that.N) throw new RuntimeException("Dimensions don't agree");
        VectorUtil c = new VectorUtil(N);
        for (int i = 0; i < N; i++)
            c.data[i] = this.data[i] + that.data[i];
        return c;
    }

    // return this - that
    public VectorUtil minus(VectorUtil that) {
        if (this.N != that.N) throw new RuntimeException("Dimensions don't agree");
        VectorUtil c = new VectorUtil(N);
        for (int i = 0; i < N; i++)
            c.data[i] = this.data[i] - that.data[i];
        return c;
    }

    // return the corresponding coordinate
    public double cartesian(int i) {
        return data[i];
    }

    // create and return a new object whose value is (this * factor)
    public VectorUtil times(double factor) {
        VectorUtil c = new VectorUtil(N);
        for (int i = 0; i < N; i++)
            c.data[i] = factor * data[i];
        return c;
    }


    // return the corresponding unit vector
    public VectorUtil direction() {
        if (this.magnitude() == 0.0) throw new RuntimeException("Zero-vector has no direction");
        return this.times(1.0 / this.magnitude());
    }

    // return a string representation of the vector
    public String toString() {
        String s = "(";
        for (int i = 0; i < N; i++) {
            s += data[i];
            if (i < N-1) s+= ", ";
        }
        return s + ")";
    }


    public static double calculateSimilarity(int[] xdata, int[] ydata){

        /*double[] xdata = {1, 100, 0, 1, 8, 9};
        double[] ydata = {1,0,0,1,0,1};*/

        VectorUtil x = new VectorUtil(xdata);
        VectorUtil y = new VectorUtil(ydata);

        double X_dotProduct_Y = x.dot(y); //y.dot(x);
        double magnitudeX = x.magnitude();
        double magnitudeY = y.magnitude();

        return magnitudeX * magnitudeY == 0 ? 0 : X_dotProduct_Y / (magnitudeX * magnitudeY);

    }

    // test client
    public static void main(String[] args) {
        double[] xdata = {1, 100, 0, 1, 8, 9};
        double[] ydata = {1,0,0,1,0,1};

        VectorUtil x = new VectorUtil(xdata);
        VectorUtil y = new VectorUtil(ydata);

        System.out.println("<x, y>   =  " + x.dot(y));
        
        double X_dotProduct_Y = x.dot(y); //y.dot(x);
        double magnitudeX = x.magnitude();
        double magnitudeY = y.magnitude();

        double similarity = X_dotProduct_Y / (magnitudeX * magnitudeY);

        System.out.println(Thread.currentThread().activeCount());


        Set<Thread> threadSet = Thread.getAllStackTraces().keySet();


        System.out.println("Similarity   =  " + similarity);
//        WebpageContentReader rdr = new WebpageContentReader("");
        //rdr.run();
        System.out.println(Thread.currentThread().activeCount());
        
    }
}
