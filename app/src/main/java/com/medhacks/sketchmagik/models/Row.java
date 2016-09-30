package com.medhacks.sketchmagik.models;

/**
 * Created by Shreya on 25/09/16.
 */
public class Row {

    private String TAG = "Row";
    String date = "NA";
    int sketchNumber = -1;
    int attemptNumber = -1;
    double timeTaken = -1;
    double deviation = -1;
    String imageData = "NA";
    int status = 0;

    public void setDate(String d) {
        date = d;
    }
    public void setSketchNumber(int sn) {
        sketchNumber = sn;
    }
    public void setAttemptNumber(int an) {
        attemptNumber = an;
    }
    public void setTimeTaken(double tt) {
        timeTaken = tt;
    }
    public void setDeviation(double d) {
        deviation = d;
    }
    public void setImage(String img) {
        imageData = img;
    }
    public void setStatus(int s) {
        status = s;
    }

    public String getDate() {
        return date;
    }
    public int getSketchNumber() {
        return sketchNumber;
    }
    public int getAttemptNumber() {
        return attemptNumber;
    }
    public double getTimeTaken() {
        return timeTaken;
    }
    public double getDeviation() {
        return deviation;
    }
    public String getImageData() {
        return imageData;
    }
    public int getStatus() {
        return status;
    }
}
