package com.vinikrish.birdchecklistandroid.models;

import java.util.List;

public class BirdGroup {
    private String letter;
    private List<Bird> birds;
    private boolean isExpanded;

    public BirdGroup(String letter, List<Bird> birds) {
        this.letter = letter;
        this.birds = birds;
        this.isExpanded = false; // Start collapsed
    }

    public String getLetter() {
        return letter;
    }

    public void setLetter(String letter) {
        this.letter = letter;
    }

    public List<Bird> getBirds() {
        return birds;
    }

    public void setBirds(List<Bird> birds) {
        this.birds = birds;
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
    }

    public int getBirdCount() {
        return birds != null ? birds.size() : 0;
    }
}