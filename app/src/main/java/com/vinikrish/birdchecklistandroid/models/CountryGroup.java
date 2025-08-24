package com.vinikrish.birdchecklistandroid.models;

import java.util.List;

public class CountryGroup {
    private String countryName;
    private List<BirdGroup> birdGroups;
    private boolean isExpanded;

    public CountryGroup(String countryName, List<BirdGroup> birdGroups) {
        this.countryName = countryName;
        this.birdGroups = birdGroups;
        this.isExpanded = false; // Start collapsed
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public List<BirdGroup> getBirdGroups() {
        return birdGroups;
    }

    public void setBirdGroups(List<BirdGroup> birdGroups) {
        this.birdGroups = birdGroups;
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    public void setExpanded(boolean expanded) {
        this.isExpanded = expanded;
    }

    public int getTotalBirdCount() {
        int total = 0;
        if (birdGroups != null) {
            for (BirdGroup group : birdGroups) {
                total += group.getBirdCount();
            }
        }
        return total;
    }
}