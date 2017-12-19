package at.meroff.bac.model;

import at.meroff.bac.helper.Cosine;
import com.sun.javafx.geom.Vec2d;
import org.junit.Before;
import org.junit.Test;

import java.awt.*;

import static org.junit.Assert.*;

public class CardTest {

    Card cardSource;
    Card cardTarget;

    @Before
    public void setUp() throws Exception {
        cardSource = new Card(1,0,0,5,0,5,5,0,5);
        cardTarget = new Card(1,0,10,5,10,5,15,0,15);
    }

    @Test
    public void getCenter() throws Exception {
        assertTrue(cardSource.getCenter().x == 2.5 && cardSource.getCenter().y == 2.5);
        assertTrue(cardTarget.getCenter().x == 2.5 && cardTarget.getCenter().y == 12.5);
    }

    @Test
    public void getSmallestX() throws Exception {
        assertTrue(cardSource.getSmallestX() == 0);
        assertTrue(cardTarget.getSmallestX() == 0);
    }

    @Test
    public void getSmallestY() throws Exception {
        assertTrue(cardSource.getSmallestY() == 0);
        assertTrue(cardTarget.getSmallestY() == 10);
    }

    @Test
    public void getBiggestX() throws Exception {
        assertTrue(cardSource.getBiggestX() == 5);
        assertTrue(cardTarget.getBiggestX() == 5);
    }

    @Test
    public void getBiggestY() throws Exception {
        assertTrue(cardSource.getBiggestY() == 5);
        assertTrue(cardTarget.getBiggestY() == 15);
    }

    @Test
    public void getDistance() throws Exception {
        assertTrue(Card.getDistance(cardSource, cardTarget) == 10);
    }

    @Test
    public void getVector() throws Exception {
        assertTrue(Card.getVector(cardSource, cardTarget).equals(new Vec2d(0,10)));
    }

    @Test
    public void getMaxCosineSimilarity() throws Exception {
        Subject subject = new Subject(50, 1067, 275, 1045, 459, 674, 416, 696, 231);
        Task source = new Task(18, 764, 740, 795, 539, 1192, 599, 1161, 800);
        Task target = new Task(9, 667, 1271, 478, 1047, 866, 719, 1055, 943);

        assertTrue(Card.getMaxCosineSimilarity(subject,source) == 0.837243863086018);

    }

}