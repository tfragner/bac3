package at.meroff.bac.model;

public class Transfer extends Card {

    public Transfer(int id, int x1, int y1, int x2, int y2, int x3, int y3, int x4, int y4) {
        super(id, x1, y1, x2, y2, x3, y3, x4, y4);
    }

    @Override
    public String toString() {
        return "Transfer " + super.toString();
    }
}
