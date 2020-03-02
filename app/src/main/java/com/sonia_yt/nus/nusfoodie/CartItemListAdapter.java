package com.sonia_yt.nus.nusfoodie;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.rey.material.widget.ImageButton;
import com.rey.material.widget.TextView;
import com.rey.material.widget.ImageView;
import com.sonia_yt.nus.nusfoodie.model.Dish;
import com.sonia_yt.nus.nusfoodie.model.User;

import java.util.ArrayList;

public class CartItemListAdapter extends ArrayAdapter<Dish> {
    private Context mContext;
    private int res;
    private int lastPosition = -1;
    private StorageReference mStorageRef;
    private ArrayList<Dish> items;

    private static class ViewHolder {
        TextView nameTv, desTv, priceTv, numOfItemsTv;
        ImageButton plusBtn, minusBtn, deletBtn;
        ImageView dishIv;
        MaterialCardView cartItemRow;
    }

    public CartItemListAdapter(Context context, int res, ArrayList<Dish> list) {
        super(context, res, list);
        this.mContext = context;
        this.res = res;
        this.items = list;
        this.mStorageRef = FirebaseStorage.getInstance().getReference("dish");
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String id = getItem(position).getId();
        String name = getItem(position).getName();
        String description = getItem(position).getDescription();
        String pictureAddress = getItem(position).getPictureAddress();
        String canteenID = getItem(position).getCanteenID();
        String stallID = getItem(position).getStallID();
        String price = getItem(position).getPrice();

        Dish dish = new Dish();
        dish.setName(name);
        dish.setDescription(description);
        dish.setId(id);
        dish.setCanteenID(canteenID);
        dish.setPictureAddress(pictureAddress);
        dish.setPrice(price);
        dish.setStallID(stallID);

        final View result;

        CartItemListAdapter.ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(res, parent, false);
            holder = new CartItemListAdapter.ViewHolder();
            holder.nameTv = (TextView) convertView.findViewById(R.id.nameTv);
            holder.desTv = (TextView) convertView.findViewById(R.id.desTv);
            holder.numOfItemsTv = (TextView) convertView.findViewById(R.id.numOfItemsTv);
            holder.dishIv = (ImageView) convertView.findViewById(R.id.dishIv);
            holder.priceTv = (TextView) convertView.findViewById(R.id.priceTv);
            holder.cartItemRow = (MaterialCardView) convertView.findViewById(R.id.cartItemRow);
            holder.plusBtn = (ImageButton) convertView.findViewById(R.id.plusBtn);
            holder.minusBtn = (ImageButton) convertView.findViewById(R.id.minusBtn);

            holder.deletBtn = (ImageButton) convertView.findViewById(R.id.deletBtn);


            result = convertView;
            convertView.setTag(holder);
        } else {
            holder = (CartItemListAdapter.ViewHolder) convertView.getTag();
            result = convertView;
        }

        lastPosition = position;

        int num = getNumOfItem(User.currUser.getMyCart().getDishInCart(), dish);

        if(CartActivity.cart_running) {
            holder.cartItemRow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(mContext,  dish.getName() + " is clicked", Toast.LENGTH_SHORT).show();
                }
            });

            holder.plusBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int curr = Integer.parseInt(holder.numOfItemsTv.getText().toString());
                    int numOfItems = ++curr;
                    double totalPrice = numOfItems * Double.parseDouble(dish.getPrice());
                    holder.numOfItemsTv.setText(numOfItems + "");
                    holder.priceTv.setText("S$" + totalPrice);
                    User.currUser.getMyCart().updateMyCart(dish, 1);
                }
            });

            holder.minusBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int curr = Integer.parseInt(holder.numOfItemsTv.getText().toString());
                    if(curr <= 0) {
                        Toast.makeText(mContext, "The minimum number of item is 0.", Toast.LENGTH_SHORT).show();
                    } else {
                        int numOfItems = --curr;
                        holder.numOfItemsTv.setText(numOfItems + "");
                        double totalPrice = numOfItems * Double.parseDouble(dish.getPrice());
                        holder.priceTv.setText("S$" + totalPrice);
                        User.currUser.getMyCart().updateMyCart(dish, 0);
                    }
                }
            });

            holder.deletBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder((Activity)mContext);

                    builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            ArrayList<Dish> newItems = (ArrayList<Dish>)User.currUser.getMyCart().getDishInCart().clone();
                            for (Dish d : User.currUser.getMyCart().getDishInCart()) {
                                if(d.compareTo(dish) == 0)
                                    newItems.remove(d);
                            }
                            User.currUser.getMyCart().setDishInCart(newItems);
                            Intent intent = new Intent(mContext, CartActivity.class);
                            mContext.startActivity(intent);
                            Toast.makeText(mContext, "Item deleted", Toast.LENGTH_SHORT).show();
                            ((Activity)mContext).finish();
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    });

                    // Create the AlertDialog
                    AlertDialog dialog = builder.create();
                    dialog.setMessage("Do you want to delete item : " + dish.getName() + "?");
                    dialog.show();

                }
            });

            holder.nameTv.setText(dish.getName());
            holder.desTv.setText(dish.getDescription());
            holder.priceTv.setText("S$" + Double.parseDouble(dish.getPrice()) * num);
            holder.numOfItemsTv.setText(num + "");

            StorageReference path = mStorageRef.child(dish.getPictureAddress());
            path.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
            {
                @Override
                public void onSuccess(Uri downloadUrl)
                {
                    if(CartActivity.cart_running)
                        Glide.with(mContext)
                                .load(downloadUrl)
                                .dontAnimate()
                                .into(holder.dishIv);
                }
            });
        }

        return convertView;
    }

    private int getNumOfItem(ArrayList<Dish> items, Dish dish) {
        int counts = 0;
        for (Dish d : items) {
            if(d.compareTo(dish) == 0)
                counts++;
        }
        return counts;
    }
}
