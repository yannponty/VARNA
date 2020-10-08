package fr.orsay.lri.varna.models.puzzler;

import rnaDrawing.rnaPuzzler.utils.Vector2D;
import rnaDrawing.rnaPuzzler.utils.VectorMath;

import java.awt.geom.Point2D;

public class HelixRotation {


    public static double angleToRotate(Point2D.Double center,Point2D.Double oldpos,Point2D.Double newpos){
        Vector2D pToVecCen = new Vector2D(center.x,center.y);
        Vector2D pToVecold = new Vector2D(oldpos.x,oldpos.y);
        Vector2D pToVecnew = new Vector2D(newpos.x,newpos.y);
        Vector2D vecCenOld = new Vector2D(pToVecCen,pToVecold);
        Vector2D vecCenNew = new Vector2D(pToVecCen,pToVecnew);
        Vector2D ref = new Vector2D(1.,0.);
        double oldangle = VectorMath.angleBetweenVectors2D(ref,vecCenOld);
        double newangle = VectorMath.angleBetweenVectors2D(ref,vecCenNew);
        newangle -= oldangle;
        if(newpos.y > center.y)
            newangle = 2*Math.PI - newangle;
        return newangle;
    }

    public static double calculateAngleBetweenPoints(Point2D.Double center
            , Point2D.Double fixLoopStem, Point2D.Double rotatedLoopBase){
        double angle = 0.0;
        Vector2D pcen = new Vector2D(center.x,center.y);
        Vector2D pfls = new Vector2D(fixLoopStem.x,fixLoopStem.y);
        Vector2D prlb = new Vector2D(rotatedLoopBase.x,rotatedLoopBase.y);
        Vector2D vCenFl = new Vector2D(pcen,pfls);
        Vector2D vCenRl = new Vector2D(pcen,prlb);
        angle = VectorMath.angleBetweenVectors2D(vCenFl,vCenRl);
        if(vCenRl.values[0]*vCenFl.values[1]- vCenRl.values[1]*vCenFl.values[0]< 0.000000000){
            angle = Math.PI - angle;
        }
        return angle;
    }

    public static boolean isRotationValid(Point2D.Double oldPos, Point2D.Double newPos, Point2D.Double limitPoint,double minDist){
       double distToOld = oldPos.distance(limitPoint);
       double distToNew = newPos.distance(limitPoint);

       return (distToOld > minDist*3 && distToNew > minDist*3) || (distToOld < minDist*3 && distToNew > minDist*3);
    }

    public static double magnetAngle(double angle, double threshold){
        double angle0,angle1,angle2,angle3;
        angle0 = Math.abs(angle);
        angle1 = Math.abs(angle - Math.PI/2.);
        angle2 = Math.abs(angle - Math.PI);
        angle3 = Math.abs(angle - (1.5)*Math.PI);
        if(angle0 < threshold)
            return 0;
        if(angle1< threshold)
            return Math.PI/2.;
        if(angle2 < threshold)
            return Math.PI;
        if(angle3 < threshold)
            return (1.5)*Math.PI;

        return angle;
    }


}
