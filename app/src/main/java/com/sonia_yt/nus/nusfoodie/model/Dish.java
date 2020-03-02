package com.sonia_yt.nus.nusfoodie.model;

import java.util.ArrayList;

public class Dish implements Comparable<Dish> {
    private String id, stallID, canteenID, pictureAddress, name, calorie, description, price, typeID, typeName;

    @Override
    public int compareTo(Dish another) {
        if(this.id == another.getId() && this.getCanteenID() == another.getCanteenID() && this.getStallID() == getStallID()) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public boolean equals(Object another) {
        boolean result = false;
        if(this.compareTo((Dish)another) == 0)
            result = true;
        return result;
    }

    public static ArrayList<Dish> getDistinctItems(ArrayList<Dish> list) {

        ArrayList<Dish> result = new ArrayList<>();
        for (Dish d : list) {
            if (!result.contains(d)) {
                result.add(d);
            }
        }
        return result;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStallID() {
        return stallID;
    }

    public void setStallID(String stallID) {
        this.stallID = stallID;
    }

    public String getCanteenID() {
        return canteenID;
    }

    public void setCanteenID(String canteenID) {
        this.canteenID = canteenID;
    }

    public String getPictureAddress() {
        return pictureAddress;
    }

    public void setPictureAddress(String pictureAddress) {
        this.pictureAddress = pictureAddress;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCalorie() {
        return calorie;
    }

    public void setCalorie(String calorie) {
        this.calorie = calorie;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getTypeID() {
        return typeID;
    }

    public void setTypeID(String typeID) {
        this.typeID = typeID;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }
}
