package com.sonia_yt.nus.nusfoodie;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.rey.material.widget.TextView;
import com.sonia_yt.nus.nusfoodie.model.Stall;

import java.util.ArrayList;

public class StallListAdapter extends ArrayAdapter<Stall> {

    private Context mContext;
    private int res;
    private int lastPosition = -1;
    private StorageReference mStorageRef;
    private String canteenName;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private static class ViewHolder {
        TextView nameTv, desTv, queueTv;
        Chip prepareTimeTv;
        ImageView stallPicIv;
        MaterialCardView stallRow;
    }

    public StallListAdapter(Context context, int res, ArrayList<Stall> list, String canteenName) {
        super(context, res, list);
        this.mContext = context;
        this.res = res;
        this.canteenName = canteenName;
        this.mStorageRef = FirebaseStorage.getInstance().getReference("canteen");
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String id = getItem(position).getId();
        String name = getItem(position).getName();
        String description = getItem(position).getDescription();
        String pic = getItem(position).getPictureAddress();
        String canteenID = getItem(position).getCanteenID();
        String typeName = getItem(position).getTypeName();
                //String queue = getItem(position).getNumInQueue();

        Stall stall = new Stall();
        stall.setName(name);
        stall.setDescription(description);
        stall.setId(id);
        stall.setCanteenID(canteenID);
        stall.setPictureAddress(pic);
        stall.setTypeName(typeName);
        stall.setOpen(getItem(position).getOpen());
        stall.setClose(getItem(position).getClose());
        stall.setPrepareTime(getItem(position).getPrepareTime());
        //stall.setNumInQueue(queue);

        final View result;

        StallListAdapter.ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(res, parent, false);
            holder = new StallListAdapter.ViewHolder();
            holder.nameTv = (TextView) convertView.findViewById(R.id.nameTv);
            holder.stallPicIv = (ImageView) convertView.findViewById(R.id.stallIv);
            holder.desTv = (TextView) convertView.findViewById(R.id.desTv);
            holder.queueTv = (TextView) convertView.findViewById(R.id.numInQueueTv);
            holder.prepareTimeTv = (Chip) convertView.findViewById(R.id.prepareTimeTv);
            holder.stallRow = (MaterialCardView) convertView.findViewById(R.id.stallRowCL);

            result = convertView;
            convertView.setTag(holder);
        } else {
            holder = (StallListAdapter.ViewHolder) convertView.getTag();
            result = convertView;
        }

        lastPosition = position;

        holder.nameTv.setText(stall.getName());
        holder.desTv.setText(stall.getDescription());
        holder.prepareTimeTv.setText(stall.getPrepareTime()+"min");
        holder.prepareTimeTv.setTextAppearance(R.style.myChipTextStyle);
        holder.queueTv.setText("Stall Type: "+stall.getTypeName());

        holder.stallRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, DishListActivity.class);
                intent.putExtra("stallID", stall.getId());
                intent.putExtra("pictureAddress", stall.getPictureAddress());
                intent.putExtra("canteenID", stall.getCanteenID());
                intent.putExtra("stallOpen", stall.getOpen());
                intent.putExtra("stallClose", stall.getClose());
                intent.putExtra("canteenName", canteenName);
                intent.putExtra("stallPrepareTime", stall.getPrepareTime());
                intent.putExtra("stallName", stall.getName());
                mContext.startActivity(intent);
            }
        });

        if(stall.getPictureAddress().isEmpty()) {
            holder.stallPicIv.setImageResource(R.drawable.salmon_photo);
        } else {
            StorageReference path = mStorageRef.child(stall.getPictureAddress());
            path.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
            {
                @Override
                public void onSuccess(Uri downloadUrl)
                {
                    Glide.with(mContext)
                            .load(downloadUrl)
                            .dontAnimate()
                            .into(holder.stallPicIv);
                }
            });

        }

        return convertView;
    }
}
