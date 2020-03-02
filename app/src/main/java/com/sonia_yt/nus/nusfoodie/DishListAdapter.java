package com.sonia_yt.nus.nusfoodie;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;

import com.google.android.material.card.MaterialCardView;
import com.rey.material.widget.TextView;
import com.sonia_yt.nus.nusfoodie.model.Dish;

import java.util.ArrayList;


public class DishListAdapter extends ArrayAdapter<Dish> {

    private Context mContext;
    private int res;
    private int lastPosition = -1;
    private String stallOpen, stallCloase, stallPrepareTime, stallName;

    private static class ViewHolder {
        TextView nameTv, desTv, priceTv, typeTv, calorieTv;
        MaterialCardView dishRow;
    }

    public DishListAdapter(Context context, int res, ArrayList<Dish> list, String stallOpen, String stallClose, String stallPrepareTime, String stallName) {
        super(context, res, list);
        this.mContext = context;
        this.res = res;
        this.stallOpen = stallOpen;
        this.stallCloase = stallClose;
        this.stallPrepareTime = stallPrepareTime;
        this.stallName = stallName;
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
        String calorie = getItem(position).getCalorie();
        String typeID = getItem(position).getTypeID();
        String typeName = getItem(position).getTypeName();

        Dish dish = new Dish();
        dish.setName(name);
        dish.setDescription(description);
        dish.setId(id);
        dish.setCanteenID(canteenID);
        dish.setPictureAddress(pictureAddress);
        dish.setPrice(price);
        dish.setStallID(stallID);
        dish.setCalorie(calorie);
        dish.setTypeID(typeID);
        dish.setTypeName(typeName);

        final View result;

        DishListAdapter.ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(res, parent, false);
            holder = new DishListAdapter.ViewHolder();
            holder.nameTv = (TextView) convertView.findViewById(R.id.nameTv);
            holder.typeTv = (TextView) convertView.findViewById(R.id.typeTv);
            holder.desTv = (TextView) convertView.findViewById(R.id.desTv);
            holder.priceTv = (TextView) convertView.findViewById(R.id.priceTv);
            holder.calorieTv = (TextView) convertView.findViewById(R.id.calorieTv);
            holder.dishRow = (MaterialCardView) convertView.findViewById(R.id.dishRow);

            result = convertView;
            convertView.setTag(holder);
        } else {
            holder = (DishListAdapter.ViewHolder) convertView.getTag();

            result = convertView;
        }

        lastPosition = position;

        holder.dishRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, DishDetailActivity.class);
                intent.putExtra("id", dish.getId());
                intent.putExtra("stallID", dish.getStallID());
                intent.putExtra("canteenID", dish.getCanteenID());
                intent.putExtra("name", dish.getName());
                intent.putExtra("description", dish.getDescription());
                intent.putExtra("pictureAddress", dish.getPictureAddress());
                intent.putExtra("calorie", dish.getCalorie());
                intent.putExtra("price", dish.getPrice());
                intent.putExtra("type", dish.getTypeName());
                intent.putExtra("typeID", dish.getTypeID());
                intent.putExtra("stallOpen", stallOpen);
                intent.putExtra("stallClose", stallCloase);
                intent.putExtra("stallName", stallName);
                intent.putExtra("stallPrepareTime", stallPrepareTime);
                mContext.startActivity(intent);

            }
        });

        holder.nameTv.setText(dish.getName());
        holder.desTv.setText(dish.getDescription());
        holder.priceTv.setText("S$" + dish.getPrice());
        holder.typeTv.setText(dish.getTypeName());
        holder.calorieTv.setText("calories: " + dish.getCalorie() + "kcal");

        return convertView;
    }


}
