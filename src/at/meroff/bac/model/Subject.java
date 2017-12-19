package at.meroff.bac.model;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class Subject extends Card {

    List<Task> assignedTasks = new LinkedList<>();

    public Subject(int id, int x1, int y1, int x2, int y2, int x3, int y3, int x4, int y4) {
        super(id, x1, y1, x2, y2, x3, y3, x4, y4);
    }

    public void addTask(Task task) {
        assignedTasks.add(task);
    }

    public List<Task> getAssignedTasks() {
        return assignedTasks;
    }

    public void setAssignedTasks(List<Task> assignedTasks) {
        this.assignedTasks = assignedTasks;
    }

    @Override
    public String toString() {
        return "Subject " + super.toString();
    }
}
