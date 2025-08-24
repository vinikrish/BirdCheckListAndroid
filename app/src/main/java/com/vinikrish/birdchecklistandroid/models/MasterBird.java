package com.vinikrish.birdchecklistandroid.models;

public class MasterBird {
    private String comName;
    private String sciName;
    private String Country;

    // Empty constructor required for Firebase
    public MasterBird() {}

    public MasterBird(String comName, String sciName, String country) {
        this.comName = comName;
        this.sciName = sciName;
        this.Country = country;
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

    public String getCountry() {
        return Country;
    }

    public void setCountry(String country) {
        this.Country = country;
    }

    @Override
    public String toString() {
        return "MasterBird{" +
                "comName='" + comName + '\'' +
                ", sciName='" + sciName + '\'' +
                ", Country='" + Country + '\'' +
                '}';
    }
}