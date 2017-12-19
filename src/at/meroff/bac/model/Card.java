package at.meroff.bac.model;

import at.meroff.bac.helper.Cosine;
import com.sun.javafx.geom.Vec2d;

import java.awt.geom.Point2D;
import java.util.Arrays;

public class Card {

    int id;
    Point2D[] points = new Point2D[4];

    public Card(int id, int x1, int y1, int x2, int y2, int x3, int y3, int x4, int y4) {
        this.id = id;
        this.points[0] = new Point2D.Double(x1, y1);
        this.points[1] = new Point2D.Double(x2, y2);
        this.points[2] = new Point2D.Double(x3, y3);
        this.points[3] = new Point2D.Double(x4, y4);
    }

    int getId() {
        return id;
    }

    /**
     * Method returns the center of the card
     * @return center point
     */
    Point2D.Double getCenter() {
        Point2D pt01 = new Point2D.Double((points[0].getX() + points[2].getX()) / 2, (points[0].getY() + points[2].getY()) / 2);
        Point2D pt02 = new Point2D.Double((points[1].getX() + points[3].getX())/2, (points[1].getY() + points[3].getY()) / 2);
        return new Point2D.Double((pt01.getX() + pt02.getX())/2, (pt01.getY() + pt02.getY()) / 2);
    }

    Double getSmallestX() {
        return Arrays.stream(points)
                .mapToDouble(Point2D::getX)
                .min()
                .orElseThrow(() -> new IllegalStateException("No minimal x coordinate found"));
    }

    Double getSmallestY() {
        return Arrays.stream(points)
                .mapToDouble(Point2D::getY)
                .min()
                .orElseThrow(() -> new IllegalStateException("No minimal x coordinate found"));
    }

    Double getBiggestX() {
        return Arrays.stream(points)
                .mapToDouble(Point2D::getX)
                .max()
                .orElseThrow(() -> new IllegalStateException("No minimal x coordinate found"));
    }

    Double getBiggestY() {
        return Arrays.stream(points)
                .mapToDouble(Point2D::getY)
                .max()
                .orElseThrow(() -> new IllegalStateException("No minimal x coordinate found"));
    }

    static Double getBiggestDiagonale(Card card) {
        double distance1 = card.points[0].distance(card.points[2]);
        double distance2 = card.points[1].distance(card.points[3]);

        if (distance1 > distance2) return distance1; else return distance2;

    }

    static Double getBiggestLength(Card card) {
        double length = 0;
        double l1 = card.points[0].distance(card.points[1]);
        if (l1 > length) length = l1;
        double l2 = card.points[1].distance(card.points[2]);
        if (l2 > length) length = l2;
        double l3 = card.points[2].distance(card.points[3]);
        if (l3 > length) length = l3;
        double l4 = card.points[3].distance(card.points[0]);
        if (l4 > length) length = l4;

        return length;
    }

    static double getDistance(Card source, Card target) {
        return Math.sqrt(
                Math.pow(target.getCenter().x - source.getCenter().x,2) +
                        Math.pow(target.getCenter().y - source.getCenter().y,2)
        );
    }

    static Vec2d getVector(Card source, Card target) {
        return new Vec2d(target.getCenter().x - source.getCenter().x, target.getCenter().y - source.getCenter().y);
    }

    static double getMaxCosineSimilarity(Card subject, Card sourceTask) {

        double x = Card.getBiggestLength(subject) / 2;
        double y = Card.getDistance(subject, sourceTask);
        double alpha = Math.asin(x/y);
        return Math.cos(alpha);

    }

    @Override
    public String toString() {
        return "Card{" +
                "id=" + id +
                ", points=" + Arrays.toString(points) +
                '}';
    }
}
