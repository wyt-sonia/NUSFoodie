package com.sonia_yt.nus.nusfoodie;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;
import androidx.core.view.ViewPropertyAnimatorListener;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.rey.material.widget.ImageButton;
import com.rey.material.widget.ListView;
import com.rey.material.widget.TextView;
import com.sonia_yt.nus.nusfoodie.model.Stall;
import com.sonia_yt.nus.nusfoodie.model.StallType;
import com.sonia_yt.nus.nusfoodie.model.User;

import java.util.ArrayList;
import java.util.HashMap;

import iammert.com.view.scalinglib.ScalingLayout;
import iammert.com.view.scalinglib.ScalingLayoutListener;
import iammert.com.view.scalinglib.State;

public class StallListActivity extends AppCompatActivity {

    String canteenID, canteenName, canteenPicID, canteenBisHrs;
    private StorageReference mStorageRef;
    private TextView titleTv, operationHrsTv;
    private ListView stallLv;
    private AutoCompleteTextView searchAct;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ArrayList<Stall> stallList = new ArrayList<>();
    private ArrayList<Stall> stallListTemp = new ArrayList<>();
    private ArrayList<StallType> types = new ArrayList<>();
    private HashMap<String, String> typeMap = new HashMap<>();
    private final String TAG = "StallListActivity";
    private ImageButton navProfileBtn, navCartBtn, navCanteenListBtn, navOrderBtn, backBtn;
    private ImageView load_gif, canteenIv;
    private Toolbar toolbar;
    private RelativeLayout searchLayout;
    private ScalingLayout scalingLayout;
    private boolean refreshFlag;
    private SwipeRefreshLayout mPullToRefreshView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        setContentView(R.layout.activity_stall_list);

        Intent intent = getIntent();

        canteenID = getIntent().getStringExtra("canteenID");
        canteenName = getIntent().getStringExtra("canteenName");
        canteenPicID = getIntent().getStringExtra("canteenPicID");
        canteenBisHrs = getIntent().getStringExtra("canteenBisHrs");

        this.mStorageRef = FirebaseStorage.getInstance().getReference("canteen");

        stallLv = (ListView) findViewById(R.id.stallLv);
        navCanteenListBtn = (ImageButton) findViewById(R.id.navCanteenListBtn);
        navProfileBtn = (ImageButton) findViewById(R.id.navProfileBtn);
        navCartBtn = (ImageButton) findViewById(R.id.navCartBtn);
        navOrderBtn = (ImageButton) findViewById(R.id.navOrderBtn);
        canteenIv = (ImageView) findViewById(R.id.canteenIv);
        titleTv = (TextView) findViewById(R.id.titleTv);
        operationHrsTv = (TextView) findViewById(R.id.operationHrsTv);
        searchLayout = findViewById(R.id.searchLayout);
        scalingLayout = findViewById(R.id.scalingLayout);

        searchAct = (AutoCompleteTextView) findViewById(R.id.searchAct);
        backBtn = (ImageButton) findViewById(R.id.backBtn);
        mPullToRefreshView = (SwipeRefreshLayout) findViewById(R.id.pull_to_refresh);

        toolbar = (Toolbar) findViewById(R.id.toolbar);

        titleTv.setText(canteenName);
        operationHrsTv.setText(canteenBisHrs);

        scalingLayout.setListener(new ScalingLayoutListener() {
            @Override
            public void onCollapsed() {
                ViewCompat.animate(searchLayout).alpha(0).setDuration(150).setListener(new ViewPropertyAnimatorListener() {
                    @Override
                    public void onAnimationStart(View view) {

                    }

                    @Override
                    public void onAnimationEnd(View view) {
                        searchLayout.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onAnimationCancel(View view) {

                    }
                }).start();
            }

            @Override
            public void onExpanded() {
                ViewCompat.animate(searchLayout).alpha(1).setDuration(200).setListener(new ViewPropertyAnimatorListener() {
                    @Override
                    public void onAnimationStart(View view) {
                        searchLayout.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationEnd(View view) {

                    }

                    @Override
                    public void onAnimationCancel(View view) {

                    }
                }).start();
            }

            @Override
            public void onProgress(float progress) {
            }
        });
        scalingLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (scalingLayout.getState() == State.COLLAPSED) {
                    scalingLayout.expand();
                }
            }
        });

        findViewById(R.id.rootLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (scalingLayout.getState() == State.EXPANDED) {
                    scalingLayout.collapse();
                }
            }
        });

        final CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsingToolbarLayout);
        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.appBarLayout);
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = true;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset < 200) {
                    toolbar.setTitle(canteenName);
                    collapsingToolbarLayout.setContentScrimColor(Color.parseColor("#37000000"));
                    isShow = true;
                    backBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(searchAct.isFocused()) {
                                searchAct.clearFocus();
                                stallListTemp = stallList;
                                if(!searchAct.getText().toString().isEmpty()) {
                                    searchAct.setText("");
                                    updateUI();
                                }
                            }
                        }
                    });
                } else if(isShow) {
                    toolbar.setTitle("");
                    isShow = false;
                    backBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            scalingLayout.collapse();
                            if(searchAct.isFocused()) {
                                searchAct.clearFocus();
                                stallListTemp = stallList;
                                if(!searchAct.getText().toString().isEmpty()) {
                                    searchAct.setText("");
                                    updateUI();
                                }
                            }
                        }
                    });
                }
            }
        });

        StorageReference path = mStorageRef.child(canteenPicID);
        path.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
        {
            @Override
            public void onSuccess(Uri downloadUrl)
            {
                Glide.with(getApplicationContext())
                        .load(downloadUrl)
                        .dontAnimate()
                        .into(canteenIv);
            }
        });

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(User.currUser.getType().equals("C")) {
                    if(!CanteenListActivity.canteen_running) {
                        startActivity(new Intent(getApplicationContext(), CanteenListActivity.class));
                    }
                }
                finish();
            }
        });

        load_gif = (ImageView) findViewById(R.id.load_gif);
        Glide.with(getApplicationContext())
                .asGif()
                .load(R.drawable.load_gif)
                .into(load_gif);

        navBarInt();

        searchAct.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                stallListTemp = new ArrayList<Stall>();
                for(Stall st : stallList) {
                    if(st.getName().toLowerCase().matches(".*"+s.toString().toLowerCase()+".*"))
                        stallListTemp.add(st);
                }
                updateUI();
            }
        });

        mPullToRefreshView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshFlag = false;
                stallList = new ArrayList<>();
                getStallInfo();
            }
        });

        //region StallType
        db.collection("stallType")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String tempID = document.getString("id");
                                String tempName = document.getString("name");
                                if(!tempID.isEmpty() && !tempName.isEmpty())
                                    typeMap.put(tempID,tempName);
                            }
                            getStallInfo();
                        } else {
                            Log.d(TAG, "Error getting stall type documents: ", task.getException());
                        }
                    }
                });
        //endregion
    }

    //region StallInfo
    private void getStallInfo() {
        db.collection("canteen")
                .document(canteenID)
                .collection("stalls")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Stall temp = new Stall();
                                temp.setId(document.getId());
                                temp.setCanteenID(document.getString("canteenID"));
                                temp.setOpen(document.getString("open"));
                                temp.setClose(document.getString("close"));
                                temp.setDescription(document.getString("description"));
                                temp.setName(document.getString("name"));
                                temp.setPictureAddress(document.getString("pictureAddress"));
                                temp.setTypeID(document.getString("typeID"));
                                temp.setOrderIDs(document.getString("orderIDs"));
                                temp.setPrepareTime(document.get("prepareTime").toString());
                                if(!typeMap.isEmpty() && !temp.getTypeID().isEmpty()
                                    && !typeMap.get(temp.getTypeID()).isEmpty()) {
                                    temp.setTypeName(typeMap.get(temp.getTypeID()));
                                } else {
                                    temp.setTypeName("Nil");
                                }
                                temp.setUserID(document.getString("userID"));
                                stallList.add(temp);
                            }
                            stallListTemp = stallList;
                            updateUI();
                        } else {
                            Log.d(TAG, "Error getting stall documents: ", task.getException());
                        }
                    }
                });
    }
    //endregion

    private void navBarInt() {
        navOrderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), OrderRecordActivity.class));
                finish();
            }
        });
        navProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                intent.putExtra("actionFlag", "view");
                startActivity(intent);
                finish();
            }
        });

        if(User.currUser.getType().equals("C")) {
            navCartBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(getApplicationContext(), CartActivity.class));
                    finish();
                }
            });
            navCanteenListBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(getApplicationContext(), CanteenListActivity.class));
                    finish();
                }
            });
        }
    }

    public void updateUI(){
        if(stallLv != null) {
            load_gif.setVisibility(View.GONE);
            StallListAdapter adapter = new StallListAdapter(this, R.layout.stall_row, stallListTemp, canteenName);
            stallLv.setAdapter(adapter);

            ArrayList<String> stallNames = new ArrayList<>();
            for(Stall s : stallListTemp) {
                stallNames.add(s.getName());
            }
            String[] stallNamesArray = stallNames.toArray(new String[stallNames.size()]);
            // Create the adapter and set it to the AutoCompleteTextView
            ArrayAdapter<String> seachAdapter =
                    new ArrayAdapter<String>(this, R.layout.row_spn_dropdown, stallNamesArray);
            searchAct.setAdapter(seachAdapter);

            if(!refreshFlag) {
                mPullToRefreshView.setRefreshing(refreshFlag);
                refreshFlag = !refreshFlag;
            }
        }
    }

    @Override
    public void onBackPressed() {
        if(User.currUser.getType().equals("C")) {
            if(!CanteenListActivity.canteen_running) {
                startActivity(new Intent(getApplicationContext(), CanteenListActivity.class));
            }
        }
        finish();
    }

}
