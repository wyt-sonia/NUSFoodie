package com.sonia_yt.nus.nusfoodie.model;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sonia_yt.nus.nusfoodie.LoginActivity;

public class Stall {

    private String id;
    private String close;
    private String open;
    private String name;
    private String description;
    private String pictureAddress;
    private String typeID;
    private String typeName;
    private String userID;
    private String canteenID;
    private String numInQueue;
    private String prepareTime;
    private static FirebaseFirestore db = FirebaseFirestore.getInstance();

    public String getPrepareTime() {
        return prepareTime;
    }

    public void setPrepareTime(String prepareTime) {
        this.prepareTime = prepareTime;
    }

    public String getNumInQueue() {
        return numInQueue;
    }

    public void setNumInQueue(String numInQueue) {
        this.numInQueue = numInQueue;
    }

    public String getOrderIDs() {
        return orderIDs;
    }

    public void setOrderIDs(String orderIDs) {
        this.orderIDs = orderIDs;
    }

    private String orderIDs;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCanteenID() {
        return canteenID;
    }

    public void setCanteenID(String canteenID) {
        this.canteenID = canteenID;
    }

    public String getClose() {
        return close;
    }

    public void setClose(String close) {
        this.close = close;
    }

    public String getOpen() {
        return open;
    }

    public void setOpen(String open) {
        this.open = open;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPictureAddress() {
        return pictureAddress;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public void setPictureAddress(String pictureID) {
        this.pictureAddress = pictureID;
    }

    public String getTypeID() {
        return typeID;
    }

    public void setTypeID(String typeID) {
        this.typeID = typeID;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public static void getMyStall() {
        Stall temp = new Stall();
        if(!User.currUser.getStallID().isEmpty()) {
            db.collection("canteen")
                    .document(User.currUser.getCanteenID())
                    .collection("stalls")
                    .document(User.currUser.getStallID())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    temp.setId(document.getString("id"));
                                    temp.setCanteenID(document.getString("canteenID"));
                                    temp.setOpen(document.getString("open"));
                                    temp.setClose(document.getString("close"));
                                    temp.setDescription(document.getString("description"));
                                    temp.setName(document.getString("name"));
                                    temp.setPictureAddress(document.getString("pictureAddress"));
                                    temp.setTypeID(document.getString("typeID"));
                                    temp.setOrderIDs(document.getString("orderIDs"));
                                    temp.setUserID(document.getString("userID"));
                                    temp.setPrepareTime(document.get("prepareTime").toString());
                                    User.currUser.setMyStall(temp);
                                    LoginActivity.afterLogin();
                                }
                            }
                        }
                    });
        } else {
            LoginActivity.afterLogin();
        }
    }
}
