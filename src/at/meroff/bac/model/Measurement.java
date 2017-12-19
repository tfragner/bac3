package at.meroff.bac.model;

import com.sun.javafx.geom.Vec2d;

public class Measurement {
    Vec2d vector;
    double distancePreviousToSubject;
    double distanceCurrentToSubject;
    double distanceBetweenTasks;
    double similarity;
    double distanceSq;

    public Measurement() {
    }

    public Measurement(Vec2d vector, double distancePreviousToSubject, double similarity) {
        this.vector = vector;
        this.distancePreviousToSubject = distancePreviousToSubject;
        this.similarity = similarity;
    }

    public Vec2d getVector() {
        return vector;
    }

    public void setVector(Vec2d vector) {
        this.vector = vector;
    }

    public double getDistancePreviousToSubject() {
        return distancePreviousToSubject;
    }

    public void setDistancePreviousToSubject(double distancePreviousToSubject) {
        this.distancePreviousToSubject = distancePreviousToSubject;
    }

    public double getSimilarity() {
        return similarity;
    }

    public void setSimilarity(double similarity) {
        this.similarity = similarity;
    }

    public double getDistanceCurrentToSubject() {
        return distanceCurrentToSubject;
    }

    public void setDistanceCurrentToSubject(double distanceCurrentToSubject) {
        this.distanceCurrentToSubject = distanceCurrentToSubject;
    }

    public double getDistanceBetweenTasks() {
        return distanceBetweenTasks;
    }

    public void setDistanceBetweenTasks(double distanceBetweenTasks) {
        this.distanceBetweenTasks = distanceBetweenTasks;
    }



    @Override
    public String toString() {
        return "Measurement{" +
                "vector=" + vector +
                ", distancePreviousToSubject=" + distancePreviousToSubject +
                ", distanceCurrentToSubject=" + distanceCurrentToSubject +
                ", distanceBetweenTasks=" + distanceBetweenTasks +
                ", similarity=" + similarity +
                ", distanceSq=" + distanceSq +
                '}';
    }

    public double getDistanceSq() {
        return distanceSq;
    }

    public void setDistanceSq(double distanceSq) {
        this.distanceSq = 1/ similarity * distanceSq;
    }

}
