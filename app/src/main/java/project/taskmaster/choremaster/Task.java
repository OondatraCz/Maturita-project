package project.taskmaster.choremaster;


import com.google.firebase.Timestamp;

import java.util.Arrays;

public class Task {
    private String title;
    private String category;
    private String description;
    private String repeatingMode;
    private String[] repetitionDetails;
    private String asignedTo;
    private String createdBy;
    private Timestamp dueDate;
    private Timestamp lastCompleted;

    @Override
    public String toString() {
        return "TaskModel{" +
                "title='" + title + '\'' +
                ", category='" + category + '\'' +
                ", description='" + description + '\'' +
                ", repeatingMode='" + repeatingMode + '\'' +
                ", asignedTo='" + asignedTo + '\'' +
                ", createdBy='" + createdBy + '\'' +
                ", repetitionDetails=" + Arrays.toString(repetitionDetails) +
                ", dueDate=" + dueDate +
                ", lastCompleted=" + lastCompleted +
                '}';
    }

    public Task(){}

    public Task(String title, String category, String description, String repeatingMode, String asignedTo, String createdBy, String[] repetitionDetails, Timestamp dueDate, Timestamp lastCompleted) {
        this.title = title;
        this.category = category;
        this.description = description;
        this.repeatingMode = repeatingMode;
        this.asignedTo = asignedTo;
        this.createdBy = createdBy;
        this.repetitionDetails = repetitionDetails;
        this.dueDate = dueDate;
        this.lastCompleted = lastCompleted;
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

    public String getAsignedTo() {
        return asignedTo;
    }

    public void setAsignedTo(String asignedTo) {
        this.asignedTo = asignedTo;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String[] getRepetitionDetails() {
        return repetitionDetails;
    }

    public void setRepetitionDetails(String[] repetitionDetails) {
        this.repetitionDetails = repetitionDetails;
    }

    public Timestamp getDueDate() {
        return dueDate;
    }

    public void setDueDate(Timestamp dueDate) {
        this.dueDate = dueDate;
    }

    public Timestamp getLastCompleted() {
        return lastCompleted;
    }

    public void setLastCompleted(Timestamp lastCompleted) {
        this.lastCompleted = lastCompleted;
    }
}