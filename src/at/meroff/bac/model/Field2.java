package at.meroff.bac.model;

import at.meroff.bac.helper.Statistics;
import javafx.util.Pair;

import java.awt.geom.Line2D;
import java.util.*;
import java.util.stream.Collectors;

public class Field2 {

    String name;

    Set<Subject> subjects = new HashSet<>();

    Set<Task> tasks = new HashSet<>();

    Set<Transfer> transfers = new HashSet<>();

    Set<Pair<Subject, Set<Pair<Task, Set<Pair<Task, Calculation>>>>>> preCalculatedValues;

    public Field2(String name, Set<Subject> subjects, Set<Task> tasks) {
        if (name == null || subjects == null || tasks == null || subjects.size() <= 0 || tasks.size() <=0 )
            throw new IllegalArgumentException("Übergebenen Parameter passen nicht");
        this.name = name;
        this.subjects = subjects;
        this.tasks = tasks;

        calculateRelations();
    }

    public Field2(String name, Set<Subject> subjects, Set<Task> tasks, Set<Transfer> transfers) {
        this(name, subjects, tasks);
        if (transfers == null || transfers.size() <= 0)
            throw new IllegalArgumentException("Transfer Objekte nicht vorhanden");
        this.transfers = transfers;
        calculateConnections();
    }

    private void calculateRelations() {

        if (checkForStarLayout()) return;

        List<Subject> localSubjects = new LinkedList<>();
        localSubjects.addAll(subjects);
        List<Task> localTasks = new LinkedList<>();
        localTasks.addAll(tasks);

        List<Subject> savedSubjects = new LinkedList<>();
        savedSubjects.addAll(subjects);
        List<Task> savedTasks = new LinkedList<>();
        savedTasks.addAll(tasks);

        // calculate values for checks
        preCalculateValues();

        while(!localSubjects.isEmpty() && !localTasks.isEmpty()) {
            // find pair of subject <-> task to start with
            Pair<Subject, Task> firstSubjectTaskRelation = findFirstSubjectTaskRelation(localSubjects, localTasks);

            //System.out.println(firstSubjectTaskRelation.getKey());
            //System.out.println(firstSubjectTaskRelation.getValue());

            boolean test = true;
            while (firstSubjectTaskRelation != null) {
                // find follow-up tasks
                Set<Pair<Task, Calculation>> followUpTasks = findFollowUpTask(firstSubjectTaskRelation);
                followUpTasks = followUpTasks.stream()
                        .filter(taskCalculationPair -> savedTasks.get(savedTasks.indexOf(taskCalculationPair.getKey())).assignedTo == null)
                        .collect(Collectors.toSet());

                // filter follow-ups
                Pair<Subject, Task> finalFirstSubjectTaskRelation = firstSubjectTaskRelation;
                Optional<Pair<Task, Calculation>> followUp = followUpTasks.stream()
                        .filter(taskCalculationPair -> taskCalculationPair.getValue().isValidSimilarity)
                        .filter(taskCalculationPair -> taskCalculationPair.getValue().distanceSubjectTarget - taskCalculationPair.getValue().distanceSubjectSource > 0)
                        .peek(taskCalculationPair -> {
                            if (taskCalculationPair.getValue().similarityFromSource < 0.5) System.out.println("filtered because its not in line: " + taskCalculationPair.getKey()) ;
                        })
                        .filter(taskCalculationPair -> taskCalculationPair.getValue().similarityFromSource > 0.5)
                        .filter(taskCalculationPair -> !checkForIntersectionWithSubject(finalFirstSubjectTaskRelation.getKey(), taskCalculationPair.getKey()))
                        .sorted(Comparator.comparingDouble(o -> o.getValue().distanceSubjectTarget))
                        .findFirst();

                savedSubjects.get(savedSubjects.indexOf(firstSubjectTaskRelation.getKey())).addTask(firstSubjectTaskRelation.getValue());
                savedTasks.get(savedTasks.indexOf(firstSubjectTaskRelation.getValue())).setAssignedTo(firstSubjectTaskRelation.getKey());
                localTasks.remove(firstSubjectTaskRelation.getValue());

                if (followUp.isPresent()) {
                    Pair<Task, Calculation> taskCalculationPair = followUp.get();
                    firstSubjectTaskRelation = new Pair<>(firstSubjectTaskRelation.getKey(), taskCalculationPair.getKey());
                } else {
                    localSubjects.remove(firstSubjectTaskRelation.getKey());
                    firstSubjectTaskRelation = null;
                }

            }
        }

        if (localSubjects.size() > 0) {
            // there are subjects without assigned tasks
            System.out.println("Fixing Subjects");

            // reassign tasks for unused subjects
            localSubjects
                    .forEach(subject -> {
                        Pair<Subject, Task> firstSubjectTaskRelation = findFirstSubjectTaskRelation(localSubjects.stream().filter(subject1 -> subject1.equals(subject)).collect(Collectors.toSet()), tasks);
                        System.out.println(firstSubjectTaskRelation.getKey().id + " --> " + firstSubjectTaskRelation.getValue().id);


                        Task taskToMove = firstSubjectTaskRelation.getValue();
                        Subject oldSubject = savedTasks.get(savedTasks.indexOf(taskToMove)).assignedTo;
                        Subject moveToSubject = firstSubjectTaskRelation.getKey();


                        savedTasks.get(savedTasks.indexOf(taskToMove)).assignedTo = moveToSubject;
                        savedSubjects.get(savedSubjects.indexOf(oldSubject)).assignedTasks.remove(taskToMove);
                        savedSubjects.get(savedSubjects.indexOf(moveToSubject)).assignedTasks.add(taskToMove);
                    });

            for (int i = 0; i < 100; i++) {
                checkInReverseOrder(savedSubjects, savedTasks);
            }

        }

        System.out.println("-------------------------------------------------------------");

        savedSubjects.stream()
                .sorted(Comparator.comparingInt(o -> o.id))
                .forEach(subject -> {
                    System.out.print(subject.id);
                    subject.assignedTasks.stream()
                            .forEach(task -> System.out.print(" --> " + task.getId()));
                    System.out.println();
                });

        System.out.println("übrige Subjects: " + localSubjects.size());
        System.out.println("übrige Tasks: " + localTasks.size());

    }

    private boolean checkForStarLayout() {

        Set<Pair<Subject, Set<Task>>> summary = subjects.stream()
                .map(subject -> {
                    return new Pair<>(subject, tasks.stream()
                            .map(task -> {

                                return new Pair<>(task, subjects.stream()
                                        .map(subject1 -> {
                                            return new Pair<>(subject1, Card.getDistance(task, subject1));
                                        })
                                        .min(Comparator.comparingDouble(Pair::getValue))
                                        .map(Pair::getKey)
                                        .orElseThrow(() -> new IllegalStateException("blabla")));

                            }).filter(taskSubjectPair -> taskSubjectPair.getValue().equals(subject))
                            .map(Pair::getKey)
                            .collect(Collectors.toSet()));
                }).collect(Collectors.toSet());

        summary.stream()
                .forEach(subjectSetPair -> {
                    System.out.print(subjectSetPair.getKey().id + " --> ");
                    subjectSetPair.getValue().stream().forEach(task -> System.out.print(task.id + " "));
                    System.out.println();
                });

        OptionalDouble average = summary.stream()
                .filter(subjectSetPair -> subjectSetPair.getValue().size() > 1)
                .map(subjectSetPair -> {
                    double[] doubles = subjectSetPair.getValue().stream()
                            .mapToDouble(value -> Card.getDistance(subjectSetPair.getKey(), value)).toArray();
                    Statistics statistics = new Statistics(doubles);
                    return statistics.bbb();
                }).mapToDouble(value -> value)
                .average();
        System.out.println( average);

        if (average.isPresent()) {
            return average.getAsDouble() < 0.3;
        } else {
            return true;
        }




    }

    private void checkInReverseOrder(List<Subject> savedSubjects, List<Task> savedTasks) {

        if (savedSubjects.size() >= savedTasks.size()) return;

        savedSubjects.stream()
                .forEach(subject -> {
                    /*System.out.println("###########################");
                    System.out.println(subject.id);
                    System.out.println("###########################");*/

                    Set<Pair<Task, Calculation>> followUpTasks = findFollowUpTask(new Pair<>(subject, subject.assignedTasks.get(subject.assignedTasks.size() - 1)));

                    Optional<Pair<Task, Calculation>> followUp = followUpTasks.stream()
                            .filter(taskCalculationPair -> taskCalculationPair.getValue().isValidSimilarity)
                            .filter(taskCalculationPair -> taskCalculationPair.getValue().distanceSubjectTarget - taskCalculationPair.getValue().distanceSubjectSource > 0)
                            .peek(taskCalculationPair -> {
                                if (taskCalculationPair.getValue().similarityFromSource < 0.5)
                                    System.out.println("filtered because its not in line: " + taskCalculationPair.getKey());
                            })
                            .filter(taskCalculationPair -> taskCalculationPair.getValue().similarityFromSource > 0.5)
                            .filter(taskCalculationPair -> !checkForIntersectionWithSubject(subject, taskCalculationPair.getKey()))
                            .sorted(Comparator.comparingDouble(o -> o.getValue().distanceSubjectTarget))
                            .findFirst();

                    if (followUp.isPresent()) {
                        // Task which should be checked
                        Task contested = followUp.get().getKey();
                        Subject currentlyAssignedSubject = savedTasks.get(savedTasks.indexOf(contested)).assignedTo;
                        //System.out.println("\tTask " + contested.id + " currently assigned to: " + currentlyAssignedSubject.id);

                        if (savedSubjects.get(savedSubjects.indexOf(currentlyAssignedSubject)).assignedTasks.size() == 1) {
                            //System.out.println("\t!!! cannot move task because it is the only task for its current subject");
                        } else if ( savedSubjects.get(savedSubjects.indexOf(currentlyAssignedSubject)).assignedTasks.indexOf(contested) == 0) {
                            //System.out.println("\t!!! cannot move task because it is the only task for its the starting task for its subject");
                        } else if ( savedSubjects.get(savedSubjects.indexOf(currentlyAssignedSubject)).assignedTasks.indexOf(contested) == savedSubjects.get(savedSubjects.indexOf(currentlyAssignedSubject)).assignedTasks.size()-1){
                            //System.out.println("\tcontesting last task");

                            double distanceOriginal = Card.getDistance(savedSubjects.get(savedSubjects.indexOf(currentlyAssignedSubject)).assignedTasks.get(savedSubjects.get(savedSubjects.indexOf(currentlyAssignedSubject)).assignedTasks.indexOf(contested) - 1), contested);
                            double contender = Card.getDistance(subject.assignedTasks.get(subject.assignedTasks.size() - 1), contested);

                            if (distanceOriginal < contender) {
                                // Do nothing
                            } else {
                                savedTasks.get(savedTasks.indexOf(contested)).assignedTo = subject;
                                savedSubjects.get(savedSubjects.indexOf(currentlyAssignedSubject)).assignedTasks.remove(contested);
                                savedSubjects.get(savedSubjects.indexOf(subject)).assignedTasks.add(contested);
                            }

                        }

                    }
                });



    }

    private boolean checkForIntersectionWithSubject(Subject baseSubject, Task task) {
        Line2D inter = new Line2D.Double(baseSubject.getCenter().x, baseSubject.getCenter().y, task.getCenter().x, task.getCenter().y);

        for (Subject subject : subjects) {
            if (!subject.equals(baseSubject)) {

                Line2D l1 = new Line2D.Double(subject.points[0].getX(), subject.points[0].getY(), subject.points[1].getX(), subject.points[1].getY());
                Line2D l2 = new Line2D.Double(subject.points[1].getX(), subject.points[1].getY(), subject.points[2].getX(), subject.points[2].getY());
                Line2D l3 = new Line2D.Double(subject.points[2].getX(), subject.points[2].getY(), subject.points[3].getX(), subject.points[3].getY());
                Line2D l4 = new Line2D.Double(subject.points[3].getX(), subject.points[3].getY(), subject.points[0].getX(), subject.points[0].getY());

                if (l1.intersectsLine(inter) || l2.intersectsLine(inter) || l3.intersectsLine(inter) || l4.intersectsLine(inter)) {
                    return true;
                }
            }
        }

        return false;
    }

    private Set<Pair<Task, Calculation>> findFollowUpTask(Pair<Subject, Task> subjectTaskRelation) {
        return preCalculatedValues.stream()
                .filter(subjectSetPair -> subjectSetPair.getKey().equals(subjectTaskRelation.getKey()))
                .map(subjectSetPair -> {
                    return subjectSetPair
                            .getValue()
                            .stream()
                            .filter(taskSetPair -> taskSetPair.getKey().equals(subjectTaskRelation.getValue()))
                            .findFirst()
                            .map(Pair::getValue)
                            .orElseThrow(() -> new IllegalStateException("did not found the requested task"));
                })
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("did not found the requested task"));
    }

    private void preCalculateValues() {
        this.preCalculatedValues = subjects.stream()
                .map(subject -> new Pair<>(subject, tasks.stream()
                        .map(sourceTask -> new Pair<>(sourceTask, tasks.stream()
                                .map(targetTask -> new Pair<>(targetTask, new Calculation(subject, sourceTask, targetTask)))
                                .collect(Collectors.toSet()))
                        ).collect(Collectors.toSet())))
                .collect(Collectors.toSet());
    }

    private Pair<Subject, Task> findFirstSubjectTaskRelation(Collection<Subject> subjects, Collection<Task> tasks) {
        return subjects.stream()
                .map(subject -> new Pair<>(subject,tasks.stream()
                        .map(task -> new Pair<>(task,Card.getDistance(subject, task)))
                        .min(Comparator.comparingDouble(Pair::getValue))
                        .orElseThrow(() -> new IllegalStateException("no minimum found"))))
                .sorted(Comparator.comparingDouble(o -> o.getValue().getValue()))
                .map(subjectPairPair -> new Pair<>(subjectPairPair.getKey(), subjectPairPair.getValue().getKey()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("no subject found"));
    }

    private void calculateConnections() {

    }

}
