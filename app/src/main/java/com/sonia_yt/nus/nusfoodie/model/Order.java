package com.sonia_yt.nus.nusfoodie.model;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sonia_yt.nus.nusfoodie.CartActivity;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class Order {
    public static String orderString, lastID;
    final String TAG = "OrderObject";
    private static FirebaseFirestore db = FirebaseFirestore.getInstance();
    private int id;
    private String canteenID;
    private String stallID;
    private String userID;
    private String totalPrice;
    private String remark;
    private String itemInfo;
    private String stallName;
    private String canteenName;
    private boolean takeAway;
    private ArrayList<Dish> items;
    private Queue<ItemInOrder> itemsInOrder;
    private Date collectionTime, orderedTime;
    private OrderStatus status;

    public enum OrderStatus {
        PENDING,   //after customer check out
        CANCELED,   //cancel by customer(before accepted), cancel by vendor
        ACCEPTED,   //accepted by vendor
        READY,      //when the item is ready to be collected
        ASKREFUND,  //when the user complain and ask for refund
        FINISHED    //1, order canceled 2, item collected 3, refund handled
    }

    public Order() {

    }

    public Order(String canteenID, String stallID, String userID, String totalPrice, String remark, boolean takeAway, ArrayList<Dish> items, Date collectionTime, Date orderedTime) {
        this.canteenID = canteenID;
        this.stallID = stallID;
        this.userID = userID;
        this.totalPrice = totalPrice;
        this.remark = remark;
        this.takeAway = takeAway;
        this.items = items;
        this.collectionTime = collectionTime;
        this.status = OrderStatus.PENDING;
        this.orderedTime = orderedTime;
        this.itemsInOrder = generateItemInOrder();
        this.itemInfo = itemInfoGenerat();
    }

    public Order(String itemInfo, String takeAway, Date collectionTime, Date orderedTime, String status, String totalPrice) {
        this.itemInfo = itemInfo;
        this.takeAway = Boolean.parseBoolean(takeAway);
        this.collectionTime = collectionTime;
        this.orderedTime = orderedTime;
        this.totalPrice = totalPrice;
        this.status = OrderStatus.valueOf(status);
    }

    public String itemInfoGenerat() {
        String info = "";
        if(this.itemsInOrder.isEmpty()) {
            info = "no item in this order.";
        } else {
            Queue<ItemInOrder> temp = new LinkedList<>();
            while(!this.itemsInOrder.isEmpty()) {
                info += this.itemsInOrder.peek().getDishName() + " * " + this.itemsInOrder.peek().getNum();
                if(this.itemsInOrder.size() != 1) {
                    info += ", \n";
                }
                temp.offer(this.itemsInOrder.poll());
            }
            this.itemsInOrder = temp;
        }
        return info;
    }

    private int getNumOfItem(Dish dish) {
        int counts = 0;
        for (Dish d : this.items) {
            if (d.compareTo(dish) == 0)
                counts++;
        }
        return counts;
    }

    private Queue<ItemInOrder> generateItemInOrder () {
        Queue<ItemInOrder> itemsTemp = new LinkedList<ItemInOrder>();
        ArrayList<Dish> distinctItems = Dish.getDistinctItems(this.items);
        for(Dish d : distinctItems) {
            int count = getNumOfItem(d);
            itemsTemp.offer(new ItemInOrder(this.id+"", d.getName(), d.getId(), d.getStallID(), d.getCanteenID(), count+""));
        }
        return itemsTemp;
    }

    private void addOrderToDBWithID() {
        // Create a new user with a first and last name
        Map<String, Object> order = new HashMap<>();
        order.put("id", this.id);
        order.put("canteenID", this.canteenID);
        order.put("stallID", this.stallID);
        order.put("userID", this.userID);
        order.put("totalPrice", this.totalPrice);
        order.put("remark", this.remark);
        order.put("takeAway", Boolean.toString(this.takeAway));
        order.put("collectionTime", this.collectionTime.toString());
        order.put("status", this.status.toString());
        order.put("itemInfo", this.itemInfo);
        order.put("stallName", this.stallName);
        order.put("orderedTime", this.orderedTime.toString());

        // Add a new document with a generated ID
        db.collection("order")
                .document(this.id+"")
                .set(order)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot added");
                        addItemToDB();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                CartActivity.afterCheckOut(false);
                Log.w(TAG, "Error adding document", e);
            }
        });
    }

    public void addOrderToDB() {

        DocumentReference mlastID = db.collection("order").document("id_counter");

        mlastID.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                id = Integer.parseInt(document.get("lastID").toString()) + 1;
                                mlastID.update("lastID", id + "").addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        addOrderToDBWithID();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        CartActivity.afterCheckOut(false);
                                    }
                                });
                            }
                        }
                    }
                });
    }

    private void addItemToDB() {
        if(!this.itemsInOrder.isEmpty()){
            Queue<ItemInOrder> temp = new LinkedList<>(this.itemsInOrder);
            insertOrderItemToDB(temp);
        }
    }

    private void insertOrderItemToDB(Queue<ItemInOrder> itemsTemp) {

        Map<String, Object> item = new HashMap<>();
        ItemInOrder temp = itemsTemp.poll();
        item.put("orderID", this.id);
        item.put("dishName", temp.getDishName());
        item.put("dishID", temp.getDishID());
        item.put("stallID", temp.getStallID());
        item.put("canteenID", temp.getCanteenID());
        item.put("count", temp.getNum());

        db.collection("order")
                .document(this.id+"")
                .collection("items")
                .document(temp.getDishID())
                .set(item)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        if(!itemsTemp.isEmpty()) {
                            insertOrderItemToDB(itemsTemp);
                        } else {
                            orderString = User.currUser.getOrderRecord();
                            if(orderString.isEmpty())
                                orderString += id + "";
                            else
                                orderString += "," + id;
                            db.collection("users")
                                    .document(User.currUser.getEmail())
                                    .update("orderRecord", orderString)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            updateStallInDB(temp.getCanteenID(), temp.getStallID(), id+"");
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    CartActivity.afterCheckOut(false);
                                    Log.w(TAG, "Error adding document", e);
                                }
                            });
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "Error adding document", e);
            }});
    }

    private void updateStallInDB(String canteenID, String stallID, String id){
        db.collection("canteen")
                .document(canteenID)
                .collection("stalls")
                .document(stallID)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        String orderIDs = documentSnapshot.getString("orderIDs");
                        if(orderIDs.isEmpty()) {
                            orderIDs = id;
                        } else {
                            orderIDs += ","+id;
                        }
                        db.collection("canteen")
                                .document(canteenID)
                                .collection("stalls")
                                .document(stallID)
                                .update("orderIDs", orderIDs)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        User.currUser.setOrderRecord(orderString);
                                        CartActivity.afterCheckOut(true);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                CartActivity.afterCheckOut(false);
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                CartActivity.afterCheckOut(false);
            }
        });

    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getCanteenID() {
        return canteenID;
    }

    public void setCanteenID(String canteenID) {
        this.canteenID = canteenID;
    }

    public String getStallID() {
        return this.stallID;
    }

    public void setStallID(String stallID) {
        this.stallID = stallID;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(String totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public boolean isTakeAway() {
        return takeAway;
    }

    public void setTakeAway(boolean takeAway) {
        this.takeAway = takeAway;
    }

    public ArrayList<Dish> getItems() {
        return items;
    }

    public void setItems(ArrayList<Dish> items) {
        this.items = items;
    }

    public Date getCollectionTime() {
        return collectionTime;
    }

    public void setCollectionTime(Date collectionTime) {
        this.collectionTime = collectionTime;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public Date getOrderedTime() {
        return orderedTime;
    }

    public void setOrderedTime(Date orderedTime) {
        this.orderedTime = orderedTime;
    }

    public String getItemInfo() {
        return itemInfo;
    }

    public String getStallName() {
        return stallName;
    }

    public void setStallName(String stallName) {
        this.stallName = stallName;
    }

    public String getCanteenName() {
        return canteenName;
    }

    public void setCanteenName(String canteenName) {
        this.canteenName = canteenName;
    }
}
