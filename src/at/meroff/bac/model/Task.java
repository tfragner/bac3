package at.meroff.bac.model;

public class Task extends Card{

    Subject assignedTo;

    public Task(int id, int x1, int y1, int x2, int y2, int x3, int y3, int x4, int y4) {
        super(id, x1, y1, x2, y2, x3, y3, x4, y4);
    }

    public Subject getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(Subject assignedTo) {
        this.assignedTo = assignedTo;
    }

    @Override
    public String toString() {
        return "Task " + super.toString();
    }
}
