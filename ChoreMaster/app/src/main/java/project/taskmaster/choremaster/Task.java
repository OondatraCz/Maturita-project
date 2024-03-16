package project.taskmaster.choremaster;


import com.google.firebase.Timestamp;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Task {
    private String id;
    private String title;
    private String category;
    private String description;
    private String repeatingMode;
    private List<Integer> repeatingValue;
    private String assignedTo;
    private String createdBy;
    private Timestamp dueDate;
    private List<Timestamp> lastCompleted;
    private long points;

    @Override
    public String toString() {
        String lastCompletedString = (lastCompleted == null) ? "null" : lastCompleted.toString();
        return "TaskModel{" +
                "id='" + id +'\'' +
                ", title='" + title + '\'' +
                ", category='" + category + '\'' +
                ", description='" + description + '\'' +
                ", repeatingMode='" + repeatingMode + '\'' +
                ", assignedTo='" + assignedTo + '\'' +
                ", createdBy='" + createdBy + '\'' +
                ", repetitionDetails=" + repeatingValue.toString() +
                ", dueDate=" + dueDate +
                ", lastCompleted=" + lastCompletedString +
                ", points=" + points +
                '}';
    }

    public Task(){
        this.lastCompleted = new ArrayList<>();
    }

    public Task(String id, String title, String category, String description, String repeatingMode, List<Integer> repeatingValue, String assignedTo, String createdBy, Timestamp dueDate, long points) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.description = description;
        this.repeatingMode = repeatingMode;
        this.repeatingValue = repeatingValue;
        this.assignedTo = assignedTo;
        this.createdBy = createdBy;
        this.dueDate = dueDate;
        this.points = points;
        this.lastCompleted = new ArrayList<>();
    }

    public Task(String title, String category, String description, String repeatingMode, String assignedTo, String createdBy, List<Integer> repeatingValue, Timestamp dueDate, List<Timestamp> lastCompleted, long points) {
        this.title = title;
        this.category = category;
        this.description = description;
        this.repeatingMode = repeatingMode;
        this.assignedTo = assignedTo;
        this.createdBy = createdBy;
        this.repeatingValue = repeatingValue;
        this.dueDate = dueDate;
        this.lastCompleted = lastCompleted;
        this.points = points;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRepeatingMode() {
        return repeatingMode;
    }

    public void setRepeatingMode(String repeatingMode) {
        this.repeatingMode = repeatingMode;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public List<Integer> getRepeatingValue() {
        return repeatingValue;
    }

    public void setRepeatingValue(List<Integer> repeatingValue) {
        this.repeatingValue = repeatingValue;
    }

    public Timestamp getDueDate() {
        return dueDate;
    }

    public void setDueDate(Timestamp dueDate) {
        this.dueDate = dueDate;
    }

    public List<Timestamp> getLastCompleted() {
        return lastCompleted;
    }

    public void addLastCompleted(Timestamp lastCompleted){
        this.lastCompleted.add(lastCompleted);
    }

    public void setLastCompleted(List<Timestamp> lastCompleted) {
        this.lastCompleted = lastCompleted;
    }
    public long getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }
}