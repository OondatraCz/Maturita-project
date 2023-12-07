package com.taskmaster.choremaster;

public class TaskModel {
    private String title;
    private String category;
    private String description;
    private String repeatingMode;
    private int repeatingValue;
    private int year;
    private int month;
    private int day;
    private int hour;
    private int minute;
    private int groupID;
    private int userID;

    public TaskModel(){}


    public TaskModel(String name, String category, String description, String repeatingMode, int repeatingValue, int year, int month, int day, int hour, int minute) {
        this.title = name;
        this.category = category;
        this.description = description;
        this.repeatingMode = repeatingMode;
        this.repeatingValue = repeatingValue;
        this.year = year;
        this.month = month;
        this.day = day;
        this.hour = hour;
        this.minute = minute;
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
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getRepeatingMode() {
        return repeatingMode;
    }

    public void setRepeatingMode(String repeatingMode) {
        this.repeatingMode = repeatingMode;
    }

    public int getRepeatingValue() {
        return repeatingValue;
    }

    public void setRepeatingValue(int repeatingValue) {
        this.repeatingValue = repeatingValue;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public int getGroupID() { return groupID; }

    public void setGroupID(int groupID) { this.groupID = groupID; }

    public int getUserID() { return userID; }

    public void setUserID(int userID) { this.userID = userID; }

    @Override
    public String toString() {
        return "TaskModel{" +
                "name='" + title + '\'' +
                ", category='" + category + '\'' +
                ", repeatingMode=" + repeatingMode +
                ", repeatingValue=" + repeatingValue +
                ", year=" + year +
                ", month=" + month +
                ", day=" + day +
                ", hour=" + hour +
                ", minute=" + minute +
                '}';
    }
}
