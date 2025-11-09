package com.vinikrish.birdchecklistandroid.models;

import java.util.List;

public class Bird {
    private String id;
    private String documentId; // For Firestore document ID
    private String comName;
    private String sciName;
    private boolean female;
    private boolean male;
    private boolean saw;
    private boolean photographed;
    private boolean heard;
    
    // Gender-specific observation fields
    private boolean sawMale;
    private boolean photographedMale;
    private boolean heardMale;
    private boolean sawFemale;
    private boolean photographedFemale;
    private boolean heardFemale;
    
    // Additional fields for user and location data
    private String country;
    private String username;
    private String userId;
    private String gender;
    private List<String> countryCodes;

    public Bird() {
    }

    public Bird(String comName, String sciName) {
        this.comName = comName;
        this.sciName = sciName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getComName() {
        return comName;
    }

    public void setComName(String comName) {
        this.comName = comName;
    }

    public String getSciName() {
        return sciName;
    }

    public void setSciName(String sciName) {
        this.sciName = sciName;
    }

    public boolean isFemale() {
        return female;
    }

    public void setFemale(boolean female) {
        this.female = female;
    }

    public boolean isMale() {
        return male;
    }

    public void setMale(boolean male) {
        this.male = male;
    }

    public boolean isSaw() {
        return saw;
    }

    public void setSaw(boolean saw) {
        this.saw = saw;
    }

    public boolean isPhotographed() {
        return photographed;
    }

    public void setPhotographed(boolean photographed) {
        this.photographed = photographed;
    }

    public boolean isHeard() {
        return heard;
    }

    public void setHeard(boolean heard) {
        this.heard = heard;
    }
    
    // Gender-specific observation methods
    public boolean isSawMale() {
        return sawMale;
    }
    
    public void setSawMale(boolean sawMale) {
        this.sawMale = sawMale;
    }
    
    public boolean isPhotographedMale() {
        return photographedMale;
    }
    
    public void setPhotographedMale(boolean photographedMale) {
        this.photographedMale = photographedMale;
    }
    
    public boolean isHeardMale() {
        return heardMale;
    }
    
    public void setHeardMale(boolean heardMale) {
        this.heardMale = heardMale;
    }
    
    public boolean isSawFemale() {
        return sawFemale;
    }
    
    public void setSawFemale(boolean sawFemale) {
        this.sawFemale = sawFemale;
    }
    
    public boolean isPhotographedFemale() {
        return photographedFemale;
    }
    
    public void setPhotographedFemale(boolean photographedFemale) {
        this.photographedFemale = photographedFemale;
    }
    
    public boolean isHeardFemale() {
        return heardFemale;
    }
    
    public void setHeardFemale(boolean heardFemale) {
        this.heardFemale = heardFemale;
    }
    
    // Additional getters and setters
    public String getCountry() {
        return country;
    }
    
    public void setCountry(String country) {
        this.country = country;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getGender() {
        return gender;
    }
    
    public void setGender(String gender) {
        this.gender = gender;
    }
    
    public List<String> getCountryCodes() {
        return countryCodes;
    }
    
    public void setCountryCodes(List<String> countryCodes) {
        this.countryCodes = countryCodes;
    }
}