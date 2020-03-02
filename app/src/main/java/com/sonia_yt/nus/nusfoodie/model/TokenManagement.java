package com.sonia_yt.nus.nusfoodie.model;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.sonia_yt.nus.nusfoodie.LoginActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TokenManagement {
    private String userID, token;
    private static String currToken;
    private static FirebaseMessaging fm = FirebaseMessaging.getInstance();
    private static final CollectionReference tokenDB = FirebaseFirestore.getInstance().collection("token");

    public TokenManagement() {

    }

    public static String getCurrToken() {
        return currToken;
    }

    public static void setCurrToken(String currToken) {
        TokenManagement.currToken = currToken;
    }

    public TokenManagement(String userID, String token) {
        this.userID = userID;
        this.token = token;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }


    public static void checkTokenExist(String flag) {

        tokenDB.whereEqualTo("token", currToken)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if(task.getResult() != null) {
                                if(task.getResult().size() <= 0) {
                                    addToken(flag);
                                } else {
                                    for(DocumentSnapshot d : task.getResult()) {
                                        if(!d.getString("userID").equals(User.currUser.getEmail())) {
                                            updateToken(d.getId(), flag);
                                        } else {
                                            if(flag.equals("l"))
                                                LoginActivity.afterToken();
                                        }
                                    }
                                }

                            }
                        }
                    }
                });
    }

    public static void updateToken(String tokenID, String flag) {
        Map<String, Object> token = new HashMap<>();
        token.put("userID", User.currUser.getEmail());
        tokenDB.document(tokenID)
                .update(token)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        if(flag.equals("l"))
                            LoginActivity.afterToken();
                    }
                });
    }

    public static void addToken(String flag) {
        Map<String, Object> tokenMap = new HashMap<>();
        tokenMap.put("userID", User.currUser.getEmail());
        tokenMap.put("token", currToken);
        tokenDB.document().set(tokenMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        if(flag.equals("l"))
                            LoginActivity.afterToken();
                    }
                });
    }

    public static void findTokens(String userID, String title, String body) {
        List<String> tokens = new ArrayList<>();
        tokenDB.whereEqualTo("userID", userID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()) {
                            for(DocumentSnapshot d : task.getResult()) {
                                tokens.add(d.getString("token"));
                            }
                            sendNotification(tokens, title, body);
                        }
                    }
                });
    }

    public static void sendNotification(List<String> targetToken, String title, String body) {

    }
}
