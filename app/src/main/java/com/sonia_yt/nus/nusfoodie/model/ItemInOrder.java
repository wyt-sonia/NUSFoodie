package com.sonia_yt.nus.nusfoodie.model;

public class ItemInOrder {
    private String orderID, dishName, dishID, stallID, canteenID, num;

    public ItemInOrder(String orderID, String dishName, String dishID, String stallID, String canteenID, String num) {
        this.orderID = orderID;
        this.dishName = dishName;
        this.dishID = dishID;
        this.stallID = stallID;
        this.canteenID = canteenID;
        this.num = num;
    }

    public String getOrderID() {
        return orderID;
    }

    public void setOrderID(String orderID) {
        this.orderID = orderID;
    }

    public String getDishName() {
        return dishName;
    }

    public void setDishName(String dishName) {
        this.dishName = dishName;
    }

    public String getDishID() {
        return dishID;
    }

    public void setDishID(String dishID) {
        this.dishID = dishID;
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

    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }
}