package com.sonia_yt.nus.nusfoodie;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.sonia_yt.nus.nusfoodie.model.Canteen;
import com.sonia_yt.nus.nusfoodie.model.User;

import java.util.ArrayList;

public class CanteenListAdapter extends ArrayAdapter<Canteen> {

    private Context mContext;
    private int res;
    private StorageReference mStorageRef;
    private int lastPosition = -1;

    private static class ViewHolder {
        TextView nameTv, distanceTv, numOfStallsTv, statusTv, desTv;
        ImageView canteenLogoIv, airConLogoIv;
        MaterialCardView canteenRow;
        Chip navBtn;
    }

    public CanteenListAdapter(Context context, int res, ArrayList<Canteen> list) {
        super(context, res, list);
        this.mContext = context;
        this.res = res;
        this.mStorageRef = FirebaseStorage.getInstance().getReference("canteen");
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final String canteenID = getItem(position).getCanteenID();
        String name = getItem(position).getName();
        String distance = getItem(position).getPostCode();
        String numOfStalls = getItem(position).getNumOfStalls();
        String status = getItem(position).getOpen();
        String airCon = getItem(position).getAirCon();
        String lat = getItem(position).getLat();
        String pictureID = getItem(position).getPictureID();
        String close = getItem(position).getClose();
        String des = getItem(position).getDescription();
        String longt = getItem(position).getLongt();
        double dist = getItem(position).getDistance();

        Canteen canteen = new Canteen();
        canteen.setName(name);
        canteen.setPostCode(distance);
        canteen.setNumOfStalls(numOfStalls);
        canteen.setAirCon(airCon);
        canteen.setOpen(status);
        canteen.setClose(close);
        canteen.setLat(lat);
        canteen.setLongt(longt);
        canteen.setCanteenID(canteenID);
        canteen.setDistance(dist);
        canteen.setDescription(des);
        canteen.setPictureID(pictureID);

        String dis = canteen.getDistance()+"km";

        final View result;

        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(res, parent, false);
            holder = new ViewHolder();
            holder.nameTv = (TextView) convertView.findViewById(R.id.nameTv);
            holder.distanceTv = (TextView) convertView.findViewById(R.id.distanceTv);
            holder.statusTv = (TextView) convertView.findViewById(R.id.statusTv);
            holder.airConLogoIv = (ImageView) convertView.findViewById(R.id.airConIv);
            holder.numOfStallsTv = (TextView) convertView.findViewById(R.id.numOfStallTv);
            holder.canteenLogoIv = (ImageView) convertView.findViewById(R.id.canteenLogoIv);
            holder.canteenRow = (MaterialCardView) convertView.findViewById(R.id.canteenRow);
            holder.desTv = (TextView) convertView.findViewById(R.id.desTv);
            holder.navBtn = (Chip) convertView.findViewById(R.id.navBtn);

            result = convertView;
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
            result = convertView;
        }

        lastPosition = position;
        String bisHes = "Business hours: " + canteen.getOpen() + " - " + canteen.getClose();
        holder.nameTv.setText(canteen.getName());
        holder.distanceTv.setText(dis + " from me");
        holder.desTv.setText(canteen.getDescription());
        holder.statusTv.setText(bisHes);
        holder.numOfStallsTv.setText("Num of Stalls: " + canteen.getNumOfStalls());
        holder.canteenRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, StallListActivity.class);
                intent.putExtra("canteenID", canteen.getCanteenID());
                intent.putExtra("canteenPicID", canteen.getPictureID());
                intent.putExtra("canteenBisHrs", bisHes);
                intent.putExtra("canteenName", canteen.getName());
                mContext.startActivity(intent);
            }
        });

        StorageReference path = mStorageRef.child(canteen.getPictureID());
        path.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
        {
            @Override
            public void onSuccess(Uri downloadUrl)
            {
                Glide.with(mContext)
                        .load(downloadUrl)
                        .dontAnimate()
                        .into(holder.canteenLogoIv);
            }
        });

        if(User.currUser.getType().equals("C")) {
            holder.navBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Uri gmmIntentUri = Uri.parse("google.navigation:q="+canteen.getName()+"+"+canteen.getPostCode());
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    if (mapIntent.resolveActivity(mContext.getPackageManager()) != null) {
                        mContext.startActivity(mapIntent);
                    } else {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/dir/?api=1&destination="+canteen.getPostCode()));
                        mContext.startActivity(browserIntent);
                    }

                }
            });
        } else {
            holder.navBtn.setVisibility(View.INVISIBLE);
        }

        if (canteen.getAirCon().equals("0"))
            holder.airConLogoIv.setVisibility(View.GONE);

        return convertView;
    }
}



