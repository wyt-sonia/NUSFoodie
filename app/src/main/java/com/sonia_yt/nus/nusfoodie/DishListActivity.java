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
import android.widget.Toast;

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
import com.rey.material.widget.Button;
import com.rey.material.widget.ImageButton;
import com.rey.material.widget.ListView;
import com.rey.material.widget.TextView;
import com.sonia_yt.nus.nusfoodie.model.Dish;
import com.sonia_yt.nus.nusfoodie.model.User;

import java.util.ArrayList;
import java.util.HashMap;

import iammert.com.view.scalinglib.ScalingLayout;
import iammert.com.view.scalinglib.ScalingLayoutListener;
import iammert.com.view.scalinglib.State;

public class DishListActivity extends AppCompatActivity {

    private boolean refreshFlag;
    private SwipeRefreshLayout mPullToRefreshView;
    private StorageReference mStorageRef;
    private String stallID, canteenID, pictureAddress, stallClose, stallOpen, stallPrepareTime, canteenName, stallName;
    private ListView dishLv;
    private AutoCompleteTextView searchAct;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ArrayList<Dish> dishList = new ArrayList<>();
    private ArrayList<Dish> dishListTemp = new ArrayList<>();
    private HashMap<String, String> typeMap = new HashMap<>();
    private final String TAG = "DishListActivity";
    private Button addNewBtn;
    private ImageView load_gif, stallIv;
    private TextView titleTv, prepareTimeTv;
    private ImageButton navProfileBtn, navCartBtn, navCanteenListBtn, navOrderBtn, backBtn;
    private RelativeLayout searchLayout;
    private ScalingLayout scalingLayout;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        setContentView(R.layout.activity_dish_list);

        if(User.currUser.getType().equals("S") && User.currUser.getMyStall() == null) {
            Intent intent = new Intent(DishListActivity.this, ManageStallActivity.class);
            intent.putExtra("actionFlag", "new");
            Toast.makeText(getApplicationContext(), "Please complete your stall information first.", Toast.LENGTH_LONG).show();
            startActivity(intent);
            finish();
            return;
        }

        Intent intent = getIntent();

        stallID = getIntent().getStringExtra("stallID");
        canteenID = getIntent().getStringExtra("canteenID");
        canteenName = getIntent().getStringExtra("canteenName");
        stallClose = getIntent().getStringExtra("stallClose");
        stallOpen = getIntent().getStringExtra("stallOpen");
        stallName = getIntent().getStringExtra("stallName");
        stallPrepareTime = getIntent().getStringExtra("stallPrepareTime");
        navCanteenListBtn = (ImageButton) findViewById(R.id.navCanteenListBtn);
        navProfileBtn = (ImageButton) findViewById(R.id.navProfileBtn);
        navCartBtn = (ImageButton) findViewById(R.id.navCartBtn);
        navOrderBtn = (ImageButton) findViewById(R.id.navOrderBtn);
        addNewBtn = (Button) findViewById(R.id.addNewBtn);
        stallIv = (ImageView) findViewById(R.id.stallIv);
        titleTv = (TextView) findViewById(R.id.titleTv);
        prepareTimeTv = (TextView) findViewById(R.id.prepareTimeTv);
        mPullToRefreshView = (SwipeRefreshLayout) findViewById(R.id.pull_to_refresh);
        refreshFlag = true;

        searchAct = (AutoCompleteTextView) findViewById(R.id.searchAct);
        backBtn = (ImageButton) findViewById(R.id.backBtn);

        toolbar = (Toolbar) findViewById(R.id.toolbar);

        if(User.currUser.getType().equals("C")) {
            prepareTimeTv.setText("Order " + stallPrepareTime + "min in advance");
            titleTv.setText(stallName);
        } else {
            titleTv.setText("My Dish List");

        }
        searchLayout = findViewById(R.id.searchLayout);
        scalingLayout = findViewById(R.id.scalingLayout);

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
                    if(User.currUser.getType().equals("C"))
                        toolbar.setTitle(stallName);
                    else
                        toolbar.setTitle("My Dish List");
                    collapsingToolbarLayout.setContentScrimColor(Color.parseColor("#37000000"));
                    isShow = true;

                    backBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(searchAct.isFocused()) {
                                searchAct.clearFocus();
                                dishListTemp = dishList;
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
                                dishListTemp = dishList;
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

        load_gif = (ImageView) findViewById(R.id.load_gif);
        Glide.with(getApplicationContext())
                .asGif()
                .load(R.drawable.load_gif)
                .into(load_gif);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(User.currUser.getType().equals("C")) {
                    if(!CanteenListActivity.canteen_running) {
                        startActivity(new Intent(getApplicationContext(), CanteenListActivity.class));
                    }
                } else {
                    if(!OrderRecordActivity.order_running) {
                        startActivity(new Intent(getApplicationContext(), OrderRecordActivity.class));
                    }
                }
                finish();
            }
        });

        mStorageRef = FirebaseStorage.getInstance().getReference("canteen");
        if(User.currUser.getMyStall() != null && User.currUser.getMyStall().getPictureAddress() != null) {
            pictureAddress = User.currUser.getMyStall().getPictureAddress();
        } else {
            pictureAddress = getIntent().getStringExtra("pictureAddress");
        }
        StorageReference path = mStorageRef.child(pictureAddress);
        path.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri downloadUrl) {
                Glide.with(getApplicationContext())
                        .load(downloadUrl)
                        .dontAnimate()
                        .into(stallIv);
            }
        });

        navBarInt();


        dishLv = (ListView) findViewById(R.id.dishLv);

        if(User.currUser.getType().equals("C")) {
            addNewBtn.setVisibility(View.GONE);
        } else {
            addNewBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), ManageDishActivity.class);
                    intent.putExtra("actionFlag", "new");
                    startActivity(intent);
                }
            });
        }

        searchAct.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                dishListTemp = new ArrayList<Dish>();
                for(Dish d : dishList) {
                    if(d.getName().toLowerCase().matches(".*"+s.toString().toLowerCase()+".*"))
                        dishListTemp.add(d);
                }
                updateUI();
            }
        });

        mPullToRefreshView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshFlag = false;
                dishList = new ArrayList<>();
                getDishInfo();
            }
        });

        //region StallType
        db.collection("dishType")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String tempID = document.getId();
                                String tempName = document.getString("name");
                                if(!tempID.isEmpty() && !tempName.isEmpty())
                                    typeMap.put(tempID,tempName);
                            }
                            getDishInfo();
                        } else {
                            Log.d(TAG, "Error getting dish type documents: ", task.getException());
                        }
                    }
                });
        //endregion
    }

    //region DishInfo
    private void getDishInfo() {
        db.collection("canteen")
                .document(canteenID)
                .collection("stalls")
                .document(stallID)
                .collection("dish")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Dish temp = new Dish();
                                temp.setId(document.getId());
                                temp.setCanteenID(document.getString("canteenID"));
                                temp.setCalorie(document.getString("calorie"));
                                temp.setDescription(document.getString("description"));
                                temp.setName(document.getString("name"));
                                temp.setPictureAddress(document.getString("pictureAddress"));
                                temp.setPrice(document.getString("price"));
                                temp.setStallID(document.getString("stallID"));
                                temp.setTypeID(document.getString("typeID"));
                                if(!typeMap.isEmpty() && !temp.getTypeID().isEmpty()
                                        && !typeMap.get(temp.getTypeID()).isEmpty()) {
                                    temp.setTypeName(typeMap.get(temp.getTypeID()));
                                } else {
                                    temp.setTypeName("Nil");
                                }
                                dishList.add(temp);
                            }
                            dishListTemp = dishList;
                            updateUI();
                        } else {
                            Log.d(TAG, "Error getting dish documents: ", task.getException());
                        }
                    }
                });
    }
    //endregion

    public void updateUI(){
        if(dishLv != null) {
            load_gif.setVisibility(View.GONE);
            DishListAdapter adapter = new DishListAdapter(this, R.layout.dish_row, dishListTemp, stallOpen, stallClose, stallPrepareTime, stallName);
            dishLv.setAdapter(adapter);

            ArrayList<String> dishNames = new ArrayList<>();
            for(Dish d : dishListTemp) {
                dishNames.add(d.getName());
            }
            String[] dishNamesArray = dishNames.toArray(new String[dishNames.size()]);
            // Create the adapter and set it to the AutoCompleteTextView
            ArrayAdapter<String> seachAdapter =
                    new ArrayAdapter<String>(this, R.layout.row_spn_dropdown, dishNamesArray);
            searchAct.setAdapter(seachAdapter);

            if(!refreshFlag) {
                mPullToRefreshView.setRefreshing(refreshFlag);
                refreshFlag = !refreshFlag;
            }
        }
    }


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
        } else if(User.currUser.getType().equals("S")) {
            navCartBtn.setImageResource(R.drawable.canteen_icon_w);
            navCartBtn.setClickable(false);
            navCartBtn.setBackgroundColor(Color.parseColor("#FF8A65"));

            navCanteenListBtn.setImageResource(R.drawable.house);
            navCanteenListBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), ManageStallActivity.class);
                    intent.putExtra("actionFlag", "view");
                    startActivity(intent);
                    finish();
                }
            });
        }
    }
    @Override
    public void onBackPressed() {
        if(User.currUser.getType().equals("C")) {
            if(!CanteenListActivity.canteen_running) {
                startActivity(new Intent(getApplicationContext(), CanteenListActivity.class));
            }
        } else {
            if(!OrderRecordActivity.order_running) {
                startActivity(new Intent(getApplicationContext(), OrderRecordActivity.class));
            }
        }
        finish();
    }
}
