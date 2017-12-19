package at.meroff.bac.helper;

import com.sun.javafx.geom.Vec2d;

public class Cosine {


    public static double similarity(int vec1[],int vec2[]) {

        int dop=vec1[0]*vec2[0] +  vec1[1]*vec2[1];
        double mag1 =Math.sqrt(Math.pow(vec1[0],2)+Math.pow(vec1[1],2));
        double mag2 =Math.sqrt(Math.pow(vec2[0],2)+Math.pow(vec2[1],2));

        return dop / (mag1 * mag2);

    }


    public static double similarity(double vec1[],double vec2[]) {

        double dop=vec1[0]*vec2[0] +  vec1[1]*vec2[1];
        double mag1=Math.sqrt(Math.pow(vec1[0],2) + Math.pow(vec1[1],2));
        double mag2=Math.sqrt(Math.pow(vec2[0],2) + Math.pow(vec2[1],2));

        return dop/ (mag1 * mag2);

    }

    public static double similarity(Vec2d vec1, Vec2d vec2) {

        double dop=vec1.x*vec2.x +  vec1.y*vec2.y;
        double mag1=Math.sqrt(Math.pow(vec1.x,2) + Math.pow(vec1.y,2));
        double mag2=Math.sqrt(Math.pow(vec2.x,2) + Math.pow(vec2.y,2));

        return dop/ (mag1 * mag2);

    }

}