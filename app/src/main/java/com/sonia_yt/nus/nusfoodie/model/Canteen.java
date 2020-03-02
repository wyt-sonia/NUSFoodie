package com.sonia_yt.nus.nusfoodie.model;

import java.util.HashMap;

public class Canteen implements Comparable<Canteen>{

    private String canteenID;
    private String address;
    private String name;
    private String postCode;
    private String description;
    private String open;
    private String close;
    private String term;
    private String daily;
    private String airCon;
    private String pictureID;
    private String numOfStalls;
    private String lat;
    private double distance;
    private static HashMap<String, Canteen> canteenList = new HashMap<>();

    @Override
    public int compareTo(Canteen another) {
        if(this.distance > another.distance){
            return 1;
        } else if (this.distance == another.distance) {
            return 0;
        } else {
            return -1;
        }
    }

    public Canteen(String canteenID, String name, String open, String close) {
        this.canteenID = canteenID;
        this.name = name;
        this.open = open;
        this.close = close;
    }

    public Canteen() {

    }

    public static HashMap<String, Canteen> getCanteenList() {
        return canteenList;
    }

    public static void addTocanteenList(String id, Canteen can) {
        Canteen.canteenList.put(id, can);
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLongt() {
        return longt;
    }

    public void setLongt(String longt) {
        this.longt = longt;
    }

    private String longt;

    public String getCanteenID() {
        return canteenID;
    }

    public void setCanteenID(String canteenID) {
        this.canteenID = canteenID;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPostCode(String postCode) {
        this.postCode = postCode;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setOpen(String open) {
        this.open = open;
    }

    public void setClose(String close) {
        this.close = close;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public void setDaily(String daily) {
        this.daily = daily;
    }

    public void setAirCon(String airCon) {
        this.airCon = airCon;
    }

    public void setPictureID(String pictureID) {
        this.pictureID = pictureID;
    }

    public void setNumOfStalls(String numOfStalls) {
        this.numOfStalls = numOfStalls;
    }

    public String getNumOfStalls() {
        return numOfStalls;
    }

    public String getAddress() {
        return address;
    }

    public String getName() {
        return name;
    }

    public String getPostCode() {
        return postCode;
    }

    public String getDescription() {
        return description;
    }

    public String getOpen() {
        return open;
    }

    public String getClose() {
        return close;
    }

    public String getTerm() {
        return term;
    }

    public String getDaily() {
        return daily;
    }

    public String getAirCon() {
        return airCon;
    }

    public String getPictureID() {
        return pictureID;
    }
}
