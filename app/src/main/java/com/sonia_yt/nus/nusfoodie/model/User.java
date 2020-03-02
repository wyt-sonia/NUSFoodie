package com.sonia_yt.nus.nusfoodie.model;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sonia_yt.nus.nusfoodie.LoginActivity;

public class User {

    final static String typeKey = "type";
    final static String emailKey = "email";
    final static String telKey = "tel";
    final static String usernameKey = "username";
    final static String genderKey = "gender";
    final static String ageKey = "age";
    final static String personalIDKey = "personalID";
    public static User currUser;
    private static final String TAG = "LoginActivity";

    private String type;
    private String email;
    private String tel;
    private String username;
    private String gender;
    private String age;
    private String personalID;
    private String orderRecord;
    private double walletBalance;
    private Cart myCart; // For Customer
    private Stall myStall; // For Vendor
    private String canteenID; // For Vendor
    private String stallID; // For Vendor

    public void setTel(String tel) {
        this.tel = tel;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setAge(String age) {
        this.age = age;
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

    public Stall getMyStall() {
        return myStall;
    }

    public void setMyStall(Stall myStall) {
        this.myStall = myStall;
    }

    public double getWalletBalance() {
        return walletBalance;
    }

    public void setWalletBalance(double walletBalance) {
        this.walletBalance = walletBalance;
    }

    public User (String type, String email, String tel, String username, String gender, String age, String personalID, String orderRecord) {
        this.type = type;
        this.age = age;
        this.email = email;
        this.gender = gender;
        this.personalID = personalID;
        this.tel = tel;
        this.username = username;
        this.myCart = null;
        this.orderRecord = orderRecord;
    }

    public static void setCurrUser(String email, final Context context) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .document(email)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                                String type = document.get(typeKey).toString();
                                String email = document.get(emailKey).toString();
                                String tel = document.get(telKey).toString();
                                String username = document.get(usernameKey).toString();
                                String gender = document.get(genderKey).toString();
                                String age = document.get(ageKey).toString();
                                String personalID = document.get(personalIDKey).toString();
                                String orderRecord = document.get("orderRecord").toString();
                                double walletBalance = document.getDouble("walletBalance");
                                currUser = new User(type, email, tel, username, gender, age, personalID, orderRecord);
                                currUser.setWalletBalance(walletBalance);
                                if(currUser.getType().equals("S")) {
                                    currUser.setCanteenID(document.get("canteenID").toString());
                                    currUser.setStallID(document.get("stallID").toString());
                                    Stall.getMyStall();
                                } else {
                                    LoginActivity.afterLogin();
                                }
                            } else {
                                Log.d(TAG, "No such document");
                            }
                        } else {
                            LoginActivity.afterLogin();
                            Log.d(TAG, "get failed with ", task.getException());
                        }
                    }
                });
    }

    public String getType() {
        return type;
    }

    public String getEmail() {
        return email;
    }

    public String getTel() {
        return tel;
    }

    public String getUsername() {
        return username;
    }

    public String getGender() {
        return gender;
    }

    public String getAge() {
        return age;
    }

    public String getPersonalID() {
        return personalID;
    }

    public Cart getMyCart() {
        return myCart;
    }

    public void setMyCart(Cart myCart) {
        this.myCart = myCart;
    }

    public String getOrderRecord() {
        return orderRecord;
    }

    public void setOrderRecord(String orderRecord) {
        this.orderRecord = orderRecord;
    }
}
