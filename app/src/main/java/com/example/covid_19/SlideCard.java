package com.example.covid_19;

public class SlideCard {
    public String title;
    public ActionType actionType;
    public String actionData;
    public int imageResId;

    public SlideCard(String title, ActionType actionType, String actionData, int imageResId) {
        this.title = title;
        this.actionType = actionType;
        this.actionData = actionData;
        this.imageResId = imageResId;
    }
}