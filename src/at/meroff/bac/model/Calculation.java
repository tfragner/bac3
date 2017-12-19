package at.meroff.bac.model;

import at.meroff.bac.helper.Cosine;
import com.sun.javafx.geom.Vec2d;

public class Calculation {

    double distanceSubjectSource;
    double distanceSubjectTarget;
    double distanceSourceTarget;
    double maxSimilarity;
    double similarity;
    double similarityFromSource;
    boolean isValidSimilarity;

    public Calculation(Card subject, Card sourceTask, Card targetTask) {

        this.distanceSubjectSource = Card.getDistance(subject, sourceTask);
        this.distanceSubjectTarget = Card.getDistance(subject, targetTask);
        this.distanceSourceTarget = Card.getDistance(sourceTask, targetTask);
        this.maxSimilarity = Card.getMaxCosineSimilarity(subject, sourceTask);

        Vec2d vSubjectSource = Card.getVector(subject, sourceTask);
        Vec2d vSubjectTarget = Card.getVector(subject, targetTask);
        this.similarity = Cosine.similarity(vSubjectSource, vSubjectTarget);
        isValidSimilarity = this.maxSimilarity < similarity;

        Vec2d vSourceTarget = Card.getVector(sourceTask, targetTask);
        this.similarityFromSource = Cosine.similarity(vSubjectSource, vSourceTarget);

    }

    @Override
    public String toString() {
        return "Calculation{" +
                "maxSimilarity=" + maxSimilarity +
                ", similarity=" + similarity +
                ", isValidSimilarity=" + isValidSimilarity +
                '}';
    }
}
