package at.meroff.bac.model;

import at.meroff.bac.helper.Cosine;
import com.sun.javafx.geom.Vec2d;
import javafx.util.Pair;

import java.awt.geom.Line2D;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class Field {

    String name;

    Set<Subject> subjects = new HashSet<>();

    Set<Task> tasks = new HashSet<>();

    Set<Transfer> transfers = new HashSet<>();

    public Field(String name, Set<Subject> subjects, Set<Task> tasks) {
        if (name == null || subjects == null || tasks == null || subjects.size() <= 0 || tasks.size() <=0 )
            throw new IllegalArgumentException("Übergebenen Parameter passen nicht");
        this.name = name;
        this.subjects = subjects;
        this.tasks = tasks;

        calculateRelations();
    }

    public Field(String name, Set<Subject> subjects, Set<Task> tasks, Set<Transfer> transfers) {
        this(name, subjects, tasks);
        if (transfers == null || transfers.size() <= 0)
            throw new IllegalArgumentException("Transfer Objekte nicht vorhanden");
        this.transfers = transfers;
    }

    private void calculateRelations() {
        System.out.println("#####################################################################");
        System.out.println("Calculate Relations for field: " + name);
        System.out.println("#####################################################################");

        // copy subjects and tasks
        Set<Subject> localSubjects = subjects;
        Set<Task> localTasks = tasks;

        // initial interpretation run
        while (!localSubjects.isEmpty() && !localTasks.isEmpty()) {
            // Search for the Subject <-> Task relation with the smallest distancePreviousToSubject
            Pair<Subject, Task> firstRelation = findFirstRelation(localSubjects, localTasks);
            System.out.println(firstRelation.getKey().getId() + " --> " + firstRelation.getValue().getId());
            System.out.println("#####################################################################");

            // Search for follow-ups
            Subject currentSubject = firstRelation.getKey();
            Set<Pair<Task, Vec2d>> allVectorsForSubjectInSetOfTasks = getAllVectorsForSubjectInSetOfTasks(currentSubject, localTasks);
            Task currentTask = firstRelation.getValue();

            boolean cont = true;

            // search for valid follow-ups
            while (cont) {
                // get the vector for the current task
                Task finalCurrentTask = currentTask;
                Pair<Task, Vec2d> currentTaskVec = allVectorsForSubjectInSetOfTasks.stream()
                        .filter(taskVec2dPair -> taskVec2dPair.getKey().equals(finalCurrentTask))
                        .findFirst()
                        .get();

                // Remove the current task from the list of possible follow-ups
                allVectorsForSubjectInSetOfTasks.remove(currentTaskVec);

                // search for the best follow-up
                Optional<Task> followUp = getFollowUp(currentSubject, currentTaskVec, allVectorsForSubjectInSetOfTasks);

                if (followUp.isPresent()) {
                    Task task = followUp.get();
                    if (checkIfOtherSubjectIsIntersected(currentSubject, task) == true) {
                        cont = false;
                    } else {
                        localTasks.remove(currentTask);
                        currentTask = task;
                        System.out.println(task);

                    }
                } else {
                    localTasks.remove(currentTask);
                    cont = false;
                }

            }

            localSubjects.remove(firstRelation.getKey());

        }
        System.out.println("Subjects left: " + subjects.size());
        System.out.println("Tasks left: " + tasks.size());
    }

    private boolean checkIfOtherSubjectIsIntersected(Subject currentSubject, Task task) {

        boolean ret = false;

        Line2D inter = new Line2D.Double(currentSubject.getCenter().x, currentSubject.getCenter().y, task.getCenter().x, task.getCenter().y);

        for (Subject subject : subjects) {
            if (!subject.equals(currentSubject)) {
                Line2D l1 = new Line2D.Double(subject.points[0].getX(), subject.points[0].getY(), subject.points[1].getX(), subject.points[1].getY());
                Line2D l2 = new Line2D.Double(subject.points[1].getX(), subject.points[1].getY(), subject.points[2].getX(), subject.points[2].getY());
                Line2D l3 = new Line2D.Double(subject.points[2].getX(), subject.points[2].getY(), subject.points[3].getX(), subject.points[3].getY());
                Line2D l4 = new Line2D.Double(subject.points[3].getX(), subject.points[3].getY(), subject.points[0].getX(), subject.points[0].getY());

                if (l1.intersectsLine(inter) || l2.intersectsLine(inter) || l3.intersectsLine(inter) || l4.intersectsLine(inter)) {
                    ret = true;
                }
            }
        }

        return ret;
    }

    private Optional<Task> getFollowUp(Subject currentSubject, Pair<Task, Vec2d> currentTaskVec, Set<Pair<Task, Vec2d>> allVectorsForSubjectInSetOfTasks) {
        return allVectorsForSubjectInSetOfTasks.stream()
                // get measurements for the task
                .map(taskVec2dPair -> {
                    Measurement measurement = new Measurement();
                    measurement.setVector(taskVec2dPair.getValue());
                    measurement.setDistancePreviousToSubject(Card.getDistance(currentSubject, currentTaskVec.getKey()));
                    measurement.setDistanceCurrentToSubject(Card.getDistance(currentSubject, taskVec2dPair.getKey()));
                    measurement.setDistanceBetweenTasks(Card.getDistance(currentSubject, taskVec2dPair.getKey()) - Card.getDistance(currentSubject, currentTaskVec.getKey()));
                    measurement.setSimilarity(Cosine.similarity(Card.getVector(currentSubject, currentTaskVec.getKey()), Card.getVector(currentSubject, taskVec2dPair.getKey())));
                    System.out.println(taskVec2dPair.getKey().id + " -- " + measurement.similarity);
                    measurement.setDistanceSq(currentTaskVec.getValue().distanceSq(taskVec2dPair.getValue()));
                    return new Pair<>(taskVec2dPair.getKey(), measurement);
                })
                // filter out tasks which are closer to the subject then the current one
                .filter(taskMeasurementPair -> taskMeasurementPair.getValue().getDistanceCurrentToSubject() > taskMeasurementPair.getValue().getDistancePreviousToSubject())
                .filter(taskMeasurementPair -> 0.85 < taskMeasurementPair.getValue().getSimilarity() && taskMeasurementPair.getValue().getSimilarity() <= 1)
                .filter(taskPairPair -> {
                    Vec2d sTo1 = new Vec2d(currentTaskVec.getValue().x - currentSubject.getCenter().x, currentTaskVec.getValue().y - currentSubject.getCenter().y);
                    Vec2d sTo2 = new Vec2d(taskPairPair.getKey().getCenter().x - currentTaskVec.getValue().x, taskPairPair.getKey().getCenter().y - currentTaskVec.getValue().y);
                    double similarity = Cosine.similarity(sTo1, sTo2);
                    return !(-.4 <=  similarity && similarity <= +.5);
                })
                //.sorted((o1, o2) -> Double.compare(o2.getValue().getSimilarity(), o1.getValue().getSimilarity()))
                .sorted(Comparator.comparingDouble(o -> o.getValue().getDistanceSq()))
                .map(taskMeasurementPair -> taskMeasurementPair.getKey())
                .findFirst();
    }

    private Set<Pair<Task, Vec2d>> getAllVectorsForSubject(Subject currentSubject) {

        return tasks.stream()
                .map(task -> new Pair<>(task, Card.getVector(currentSubject, task)))
                .collect(Collectors.toSet());

    }

    private Set<Pair<Task, Vec2d>> getAllVectorsForSubjectInSetOfTasks(Subject subject, Set<Task> tasks) {

        return tasks.stream()
                .map(task -> new Pair<>(task, Card.getVector(subject, task)))
                .collect(Collectors.toSet());

    }

    private Pair<Subject, Task> findFirstRelation(Set<Subject> localSubjects, Set<Task> localTasks) {

        Set<Pair<Subject, Set<Pair<Task, Double>>>> distances =
                findDistancesBetweenSubjectsAndTasks(localSubjects, localTasks);

        return distances.stream()
                .min((o1, o2) -> {
                    Optional<Pair<Task, Double>> min = o1.getValue().stream().min(Comparator.comparingDouble(Pair::getValue));
                    Optional<Pair<Task, Double>> min1 = o2.getValue().stream().min(Comparator.comparingDouble(Pair::getValue));
                    Double value = min.get().getValue();
                    Double value1 = min1.get().getValue();
                    return Double.compare(value, value1);
                }).map(subjectSetPair1 -> new Pair<>(subjectSetPair1.getKey(), subjectSetPair1
                        .getValue().stream()
                        .min(Comparator.comparingDouble(Pair::getValue))
                        .get()
                        .getKey())
                ).get();


    }

    private Set<Pair<Subject, Set<Pair<Task, Double>>>> findDistancesBetweenSubjectsAndTasks(Set<Subject> localSubjects, Set<Task> localTasks) {
        return localSubjects.stream()
                .map(subject -> new Pair<>(subject, localTasks.stream()
                        .map(task -> new Pair<>(task, Card.getDistance(subject, task)))
                        .collect(Collectors.toSet())))
                .collect(Collectors.toSet());
    }

}
