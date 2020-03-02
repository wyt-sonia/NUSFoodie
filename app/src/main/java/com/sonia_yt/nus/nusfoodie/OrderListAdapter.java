package com.sonia_yt.nus.nusfoodie;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.rey.material.widget.Button;
import com.rey.material.widget.ImageView;
import com.rey.material.widget.TextView;
import com.sonia_yt.nus.nusfoodie.model.Order;
import com.sonia_yt.nus.nusfoodie.model.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class OrderListAdapter extends ArrayAdapter<Order> {

    private final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM/yyyy");
    private Context mContext;
    private int res;
    private int lastPosition = -1;
    private StorageReference mStorageRef;
    private ArrayList<Order> orders;
    private Order order;

    private static class ViewHolder {
        TextView statusTv, orderedTimeTv, itemInfoTv, collectionTimeTv, priceTv, canteenTv, stallTv;
        Button acceptBtn, rejectBtn;
        ImageView gifIv;
        MaterialCardView orderRecordRow;
    }

    public OrderListAdapter(Context context, int res, ArrayList<Order> list) {
        super(context, res, list);
        this.mContext = context;
        this.res = res;
        this.orders = list;
        this.order = new Order();
        this.mStorageRef = FirebaseStorage.getInstance().getReference("dish");
    }
    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        String status = getItem(position).getStatus().name();
        String orderedTime = sdf.format(getItem(position).getOrderedTime());
        String itemInfo = getItem(position).getItemInfo();
        String collectionTime = sdf.format(getItem(position).getCollectionTime());
        String totalPrice = getItem(position).getTotalPrice();
        String orderID = getItem(position).getId()+"";
        String userID = getItem(position).getUserID();
        String stallName = getItem(position).getStallName();
        String remark = getItem(position).getRemark();
        order.setTotalPrice(totalPrice);
        order.setUserID(userID);
        order.setStallID(getItem(position).getStallID());
        order.setCanteenID(getItem(position).getCanteenID());

        final View result;

        OrderListAdapter.ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(res, parent, false);
            holder = new OrderListAdapter.ViewHolder();
            holder.statusTv = (TextView) convertView.findViewById(R.id.statusTv);
            holder.orderedTimeTv = (TextView) convertView.findViewById(R.id.orderedTimeTv);
            holder.collectionTimeTv = (TextView) convertView.findViewById(R.id.collectionTimeTv);
            holder.itemInfoTv = (TextView) convertView.findViewById(R.id.itemInfoTv);
            holder.gifIv = (ImageView) convertView.findViewById(R.id.gifIv);
            holder.orderRecordRow = (MaterialCardView) convertView.findViewById(R.id.orderRecordRow);
            holder.priceTv = (TextView) convertView.findViewById(R.id.priceTv);
            holder.acceptBtn = (Button) convertView.findViewById(R.id.acceptBtn);
            holder.rejectBtn = (Button) convertView.findViewById(R.id.rejectBtn);
            holder.canteenTv = (TextView) convertView.findViewById(R.id.canteenTv);
            holder.stallTv = (TextView) convertView.findViewById(R.id.stallTv);

            result = convertView;
            convertView.setTag(holder);
        } else {
            holder = (OrderListAdapter.ViewHolder) convertView.getTag();
            result = convertView;
        }

        lastPosition = position;

        holder.orderRecordRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext,  status, Toast.LENGTH_SHORT).show();
            }
        });

        if(User.currUser.getType().equals("S")) {
            holder.canteenTv.setVisibility(View.INVISIBLE);
        } else {
            holder.canteenTv.setText(getItem(position).getCanteenName());
        }

        holder.stallTv.setText(stallName);
        holder.statusTv.setText(status);
        holder.collectionTimeTv.setText(collectionTime);
        holder.priceTv.setText("S$"+totalPrice);
        holder.collectionTimeTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext, "Collection Time", Toast.LENGTH_SHORT).show();
            }
        });
        holder.orderedTimeTv.setText(orderedTime);
        holder.orderedTimeTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext, "Ordered Time", Toast.LENGTH_SHORT).show();
            }
        });

        if(remark != null) {
            itemInfo += "\nRemarks: " + remark;
        } else {
            itemInfo += "\nRemarks: Nil";
        }

        if(User.currUser.getType().equals("S") && (status.equals("PENDING") || status.equals("ACCEPTED"))) {
            if(status.equals("PENDING")) {
                holder.acceptBtn.setVisibility(View.VISIBLE);
                holder.acceptBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FirebaseFirestore.getInstance()
                                .collection("order")
                                .document(orderID)
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if(task.isSuccessful()) {
                                            DocumentSnapshot doc = task.getResult();
                                                if(task.getResult().exists()) {
                                                    String realTimeStatus = doc.get("status").toString();
                                                    if(realTimeStatus.equals(status)) {
                                                        AlertDialog.Builder builder = new AlertDialog.Builder((Activity)mContext);

                                                        builder.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialog, int id) {
                                                                android.widget.ImageView load_gif = ((Activity)mContext).findViewById(R.id.load_gif);
                                                                load_gif.setVisibility(View.VISIBLE);

                                                                //updateDB
                                                                FirebaseFirestore db = FirebaseFirestore.getInstance();
                                                                db.collection("order")
                                                                        .document(orderID)
                                                                        .update("status", "ACCEPTED")
                                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                            @Override
                                                                            public void onSuccess(Void aVoid) {
                                                                                //refresh this activity
                                                                                Intent intent = new Intent(mContext, OrderRecordActivity.class);
                                                                                mContext.startActivity(intent);
                                                                                Toast.makeText(mContext, "Order accepted successfully.", Toast.LENGTH_SHORT).show();
                                                                                ((Activity)mContext).finish();
                                                                            }
                                                                        });
                                                            }
                                                        });
                                                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialog, int id) {

                                                            }
                                                        });

                                                        // Create the AlertDialog
                                                        AlertDialog dialog = builder.create();
                                                        dialog.setMessage("Do you want to accept the order?");
                                                        dialog.show();
                                                    }
                                                    else {
                                                        Toast.makeText(mContext,
                                                                "The order clicked was updated, please pull to refresh the list.",
                                                                Toast.LENGTH_LONG).show();
                                                    }
                                                }
                                            }
                                        }
                                });
                    }
                });
                holder.rejectBtn.setVisibility(View.VISIBLE);
                holder.rejectBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FirebaseFirestore.getInstance()
                                .collection("order")
                               .document(orderID)
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if(task.isSuccessful()) {
                                           DocumentSnapshot doc = task.getResult();
                                                if(doc.exists()) {
                                                    String realTimeStatus = doc.get("status").toString();
                                                    if(realTimeStatus.equals(status)){
                                                        AlertDialog.Builder builder = new AlertDialog.Builder((Activity)mContext);

                                                        builder.setPositiveButton("Reject", new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialog, int id) {
                                                                android.widget.ImageView load_gif = ((Activity)mContext).findViewById(R.id.load_gif);
                                                                load_gif.setVisibility(View.VISIBLE);

                                                                //updateDB
                                                                FirebaseFirestore db = FirebaseFirestore.getInstance();
                                                                db.collection("order")
                                                                        .document(orderID)
                                                                        .update("status", "CANCELED")
                                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                            @Override
                                                                            public void onSuccess(Void aVoid) {
                                                                                toUser(order.getUserID(), "rejected");
                                                                            }
                                                                        });
                                                            }
                                                        });
                                                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialog, int id) {
                                                                // User cancelled the dialog
                                                            }
                                                        });

                                                        // Create the AlertDialog
                                                        AlertDialog dialog = builder.create();
                                                        dialog.setMessage("Do you want to reject the order?");
                                                        dialog.show();
                                                    }
                                                    else {
                                                        Toast.makeText(mContext,
                                                                "The order clicked was updated, please pull to refresh the list.",
                                                                Toast.LENGTH_LONG).show();
                                                    }
                                                }
                                            }
                                    }
                                });
                    }
                });
            } else if(status.equals("ACCEPTED")){
                holder.rejectBtn.setVisibility(View.GONE);
                holder.acceptBtn.setText("READY");
                holder.acceptBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FirebaseFirestore.getInstance()
                                .collection("order")
                                .document(orderID)
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if(task.isSuccessful()) {
                                            DocumentSnapshot doc = task.getResult();
                                                if(doc.exists()) {
                                                    String realTimeStatus = doc.get("status").toString();
                                                    if(realTimeStatus.equals(status)) {
                                                        AlertDialog.Builder builder = new AlertDialog.Builder((Activity)mContext);

                                                        builder.setPositiveButton("READY", new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialog, int id) {
                                                                android.widget.ImageView load_gif = ((Activity)mContext).findViewById(R.id.load_gif);
                                                                load_gif.setVisibility(View.VISIBLE);
                                                                //updateDB
                                                                FirebaseFirestore db = FirebaseFirestore.getInstance();
                                                                db.collection("order")
                                                                        .document(orderID)
                                                                        .update("status", "READY")
                                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                            @Override
                                                                            public void onSuccess(Void aVoid) {
                                                                                //refresh this activity
                                                                                Intent intent = new Intent(mContext, OrderRecordActivity.class);
                                                                                mContext.startActivity(intent);
                                                                                Toast.makeText(mContext, "Order status updated successfully.", Toast.LENGTH_SHORT).show();
                                                                                ((Activity)mContext).finish();
                                                                            }
                                                                        });


                                                            }
                                                        });
                                                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialog, int id) {
                                                                // User cancelled the dialog
                                                            }
                                                        });

                                                        // Create the AlertDialog
                                                        AlertDialog dialog = builder.create();
                                                        dialog.setMessage("Do you want to set the order status to READY?");
                                                        dialog.show();
                                                    }
                                                } else {
                                                    Toast.makeText(mContext,
                                                            "The order clicked was updated, please pull to refresh the list.",
                                                            Toast.LENGTH_LONG).show();
                                                }
                                            }
                                    }
                    });
                }
            });
            }
        } else if(User.currUser.getType().equals("C") && status.equals("PENDING")) {
                holder.acceptBtn.setVisibility(View.VISIBLE);
                holder.rejectBtn.setVisibility(View.GONE);
                holder.acceptBtn.setText("CANCEL");
                holder.acceptBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FirebaseFirestore.getInstance()
                                .collection("order")
                                .document(orderID)
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if(task.isSuccessful()) {
                                            DocumentSnapshot doc = task.getResult();
                                                if(doc.exists()) {
                                                    String realTimeStatus = doc.get("status").toString();
                                                    if(realTimeStatus.equals(status)) {
                                                        AlertDialog.Builder builder = new AlertDialog.Builder((Activity)mContext);

                                                        builder.setPositiveButton("Cancel order", new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialog, int id) {
                                                                android.widget.ImageView load_gif = ((Activity)mContext).findViewById(R.id.load_gif);
                                                                load_gif.setVisibility(View.VISIBLE);

                                                                //updateDB
                                                                FirebaseFirestore db = FirebaseFirestore.getInstance();
                                                                db.collection("order")
                                                                        .document(orderID)
                                                                        .update("status", "CANCELED")
                                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                            @Override
                                                                            public void onSuccess(Void aVoid) {
                                                                                toUser(order.getUserID(), "cancel");
                                                                            }
                                                                        });
                                                            }
                                                        });
                                                        builder.setNegativeButton("Keep it", new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialog, int id) {
                                                                // User cancelled the dialog
                                                            }
                                                        });

                                                        // Create the AlertDialog
                                                        AlertDialog dialog = builder.create();
                                                        dialog.setMessage("Do you want to cancel the order?");
                                                        dialog.show();
                                                    }
                                                    else {
                                                        Toast.makeText(mContext,
                                                                "The order clicked was updated, please pull to refresh the list.",
                                                                Toast.LENGTH_LONG).show();
                                                    }
                                                }
                                            }
                                        }
                                });
                            }
                    });

        } else if(User.currUser.getType().equals("C") && (status.equals("READY") || status.equals("ACCEPTED"))) {
                holder.acceptBtn.setText("COLLECTED");
                holder.rejectBtn.setVisibility(View.GONE);
                holder.acceptBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FirebaseFirestore.getInstance()
                                .collection("order")
                                .document(orderID)
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if(task.isSuccessful()) {
                                            DocumentSnapshot doc = task.getResult();
                                                if(doc.exists()) {
                                                    String realTimeStatus = doc.get("status").toString();
                                                    if(realTimeStatus.equals("ACCEPTED") || realTimeStatus.equals("READY")) {
                                                        AlertDialog.Builder builder = new AlertDialog.Builder((Activity)mContext);

                                                        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialog, int id) {
                                                                android.widget.ImageView load_gif = ((Activity)mContext).findViewById(R.id.load_gif);
                                                                load_gif.setVisibility(View.VISIBLE);

                                                                //updateDB
                                                                FirebaseFirestore db = FirebaseFirestore.getInstance();
                                                                db.collection("order")
                                                                        .document(orderID)
                                                                        .update("status", "FINISHED")
                                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                            @Override
                                                                            public void onSuccess(Void aVoid) {
                                                                                getVenderID("collected");
                                                                            }
                                                                        });
                                                            }
                                                        });
                                                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialog, int id) {

                                                            }
                                                        });

                                                        // Create the AlertDialog
                                                        AlertDialog dialog = builder.create();
                                                        dialog.setMessage("Do you want to confirm the collection?");
                                                        dialog.show();
                                                    }
                                                    else {
                                                        Toast.makeText(mContext,
                                                                "The order clicked was updated, please pull to refresh the list.",
                                                                Toast.LENGTH_LONG).show();
                                                    }
                                                }
                                            }
                                        }
                                });
                    }
                });

        } else {
            holder.rejectBtn.setVisibility(View.GONE);
            holder.acceptBtn.setVisibility(View.GONE);
        }

        holder.itemInfoTv.setText(itemInfo);
        Glide.with(mContext)
                .load(R.drawable.robottt)
                .into(holder.gifIv);

        return convertView;
    }


    private void getVenderID(String flag) {
        FirebaseFirestore.getInstance()
                .collection("canteen")
                .document(order.getCanteenID())
                .collection("stalls")
                .document(order.getStallID())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful() && task.getResult().exists()) {
                            DocumentSnapshot doc = task.getResult();
                            toUser(doc.get("userID").toString(), flag);
                        }
                    }
                });

    }

    private void toUser(String email, String flag) {
        double amount = Double.parseDouble(order.getTotalPrice());
        DocumentReference userAccount = FirebaseFirestore.getInstance()
                .collection("users")
                .document(email);

        userAccount
                .update("walletBalance", FieldValue.increment(amount))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        boolean res = task.isSuccessful();
                        returnFromThirdAccount(flag);
                    }
                });
    }

    private void returnFromThirdAccount(String flag) {
        double amount = Double.parseDouble(order.getTotalPrice());
        DocumentReference thirdPartyAccount = FirebaseFirestore.getInstance()
                .collection("third_party_account")
                .document("account");

        thirdPartyAccount
                .update("balance", FieldValue.increment(-amount))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Intent intent = new Intent(mContext, OrderRecordActivity.class);
                        mContext.startActivity(intent);
                        String msg = "";
                        if(flag.equals("collected"))
                            msg = "Order collected successfully.";
                        else if(flag.equals("cancel"))
                            msg = "Order canceled successfully.";
                        else if(flag.equals("rejected"))
                            msg = "Order rejected successfully.";

                        Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
                        ((Activity)mContext).finish();
                    }
                });
    }

}
