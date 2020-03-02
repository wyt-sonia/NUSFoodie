package com.sonia_yt.nus.nusfoodie.model;

import java.util.ArrayList;

public class Cart {

    private String canteenID, stallID, stallOpen, stallClose, stallPrepareTime, stallName;
    private ArrayList<Dish> dishInCart;
    private ArrayList<ItemInOrder> itemInorder;

    public Cart (String canteenID, String stallID) {
        this.dishInCart = new ArrayList<>();
        this.itemInorder = new ArrayList<>();
        this.canteenID = canteenID;
        this.stallID = stallID;
    }

    public String getStallName() {
        return stallName;
    }

    public void setStallName(String stallName) {
        this.stallName = stallName;
    }

    public String getStallOpen() {
        return stallOpen;
    }

    public void setStallOpen(String stallOpen) {
        this.stallOpen = stallOpen;
    }

    public String getStallClose() {
        return stallClose;
    }

    public void setStallClose(String stallClose) {
        this.stallClose = stallClose;
    }

    public String getStallPrepareTime() {
        return stallPrepareTime;
    }

    public void setStallPrepareTime(String stallPrepareTime) {
        this.stallPrepareTime = stallPrepareTime;
    }

    public ArrayList<ItemInOrder> getItemInorder() {
        return itemInorder;
    }

    public void setItemInorder(ArrayList<ItemInOrder> itemInorder) {
        this.itemInorder = itemInorder;
    }

    public void updateMyCart(Dish dish, int option) {
        if(option == 1) {
            dishInCart.add(dish);
        } else {
            dishInCart.remove(dish);
        }
    }

    public void addItem(ArrayList<Dish> items) {
        this.dishInCart.addAll(items);
    }

    public double getTotalPrice() {
        double total = 0.0;
        for(Dish d : this.dishInCart) {
            total += Double.parseDouble(d.getPrice());
        }
        return total;
    }

    public int getNumOfItems() {
        return this.dishInCart.size();
    }

    public String getCanteenID() {
        return canteenID;
    }

    public void setCanteenID(String canteenID) {
        this.canteenID = canteenID;
    }

    public String getStallID() {
        return stallID;
    }

    public void setStallID(String stallID) {
        this.stallID = stallID;
    }

    public ArrayList<Dish> getDishInCart() {
        return dishInCart;
    }

    public void setDishInCart(ArrayList<Dish> dishInCart) {
        this.dishInCart = dishInCart;
    }
}
