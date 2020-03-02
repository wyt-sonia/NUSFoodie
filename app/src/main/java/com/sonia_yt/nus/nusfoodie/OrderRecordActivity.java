package com.sonia_yt.nus.nusfoodie;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.rey.material.widget.ImageButton;
import com.rey.material.widget.ListView;
import com.rey.material.widget.TextView;
import com.sonia_yt.nus.nusfoodie.model.Canteen;
import com.sonia_yt.nus.nusfoodie.model.Order;
import com.sonia_yt.nus.nusfoodie.model.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;


public class OrderRecordActivity extends AppCompatActivity {

    public static boolean order_running = false;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ListView currOrderLv, orderHistLv;
    private ArrayList<Order>  currOrders, orderHist;
    private final String TAG = "OrderRecordActivity";
    private final SimpleDateFormat sdf_fromDB = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
    private final SimpleDateFormat sdf = new SimpleDateFormat("mm/HH/dd/MM/yyyy");
    private ImageButton navProfileBtn, navCartBtn, navCanteenListBtn, navOrderBtn;
    private ImageView load_gif;
    private MaterialButton currBtn, histBtn;
    private Toolbar toolbar;
    private TextView titleTv;
    private String title = "Current Order Record";
    private boolean refreshFlag;
    private SwipeRefreshLayout mPullToRefreshView;
    public static boolean update;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        setContentView(R.layout.activity_order_record);
        order_running = true;
        refreshFlag = true;
        update = false;

        if(User.currUser.getType().equals("S") && User.currUser.getMyStall() == null) {
            Intent intent = new Intent(OrderRecordActivity.this, ManageStallActivity.class);
            intent.putExtra("actionFlag", "new");
            Toast.makeText(getApplicationContext(), "Please complete your stall information first.", Toast.LENGTH_LONG).show();
            startActivity(intent);
            finish();
            return;
        }


        currOrders = new ArrayList<>();
        orderHist = new ArrayList<>();

        currOrderLv = (ListView) findViewById(R.id.currOrderLv);
        orderHistLv = (ListView) findViewById(R.id.orderHistLv);

        navCanteenListBtn = (ImageButton) findViewById(R.id.navCanteenListBtn);
        navProfileBtn = (ImageButton) findViewById(R.id.navProfileBtn);
        navCartBtn = (ImageButton) findViewById(R.id.navCartBtn);
        navOrderBtn = (ImageButton) findViewById(R.id.navOrderBtn);
        currBtn = (MaterialButton) findViewById(R.id.currBtn);
        histBtn = (MaterialButton) findViewById(R.id.histBtn);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        titleTv = (TextView) findViewById(R.id.titleTv);
        mPullToRefreshView = (SwipeRefreshLayout) findViewById(R.id.pull_to_refresh);

        currBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                titleTv.setText("Current Order");
                title = "Current Order Record";
                currOrderLv.setVisibility(View.VISIBLE);
                orderHistLv.setVisibility(View.INVISIBLE);
            }
        });

        histBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                titleTv.setText("Order History");
                title = "Order History Record";
                orderHistLv.setVisibility(View.VISIBLE);
                currOrderLv.setVisibility(View.INVISIBLE);
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
                    toolbar.setTitle(title);
                    collapsingToolbarLayout.setContentScrimColor(Color.parseColor("#37000000"));
                    isShow = true;
                } else if(isShow) {
                    toolbar.setTitle("");
                    isShow = false;
                }
            }
        });

        load_gif = (ImageView) findViewById(R.id.load_gif);
        Glide.with(getApplicationContext())
                .asGif()
                .load(R.drawable.load_gif)
                .into(load_gif);

        navBarInt();

        mPullToRefreshView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshFlag = false;
                currOrders = new ArrayList<>();
                orderHist = new ArrayList<>();
                getOrderFromDB();
            }
        });


        getOrderFromDB();

    }

    private void navBarInt() {
        navOrderBtn.setClickable(false);
        navOrderBtn.setBackgroundColor(Color.parseColor("#FF8A65"));
        navOrderBtn.setImageResource(R.drawable.order_icon_w);

        navProfileBtn.clearFocus();
        navProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                intent.putExtra("actionFlag", "view");
                startActivity(intent);
                if(User.currUser.getType().equals("C"))
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
            navCartBtn.setImageResource(R.drawable.canteen_icon_2);
            navCartBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), DishListActivity.class);
                    intent.putExtra("stallID", User.currUser.getStallID());
                    intent.putExtra("canteenID", User.currUser.getCanteenID());
                    startActivity(intent);
                }
            });
            navCanteenListBtn.setImageResource(R.drawable.house);
            navCanteenListBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), ManageStallActivity.class);
                    intent.putExtra("actionFlag", "view");
                    startActivity(intent);
                }
            });
        }
    }

    //change to whereEqualto
    private void getOrderFromDB() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if(User.currUser.getType().equals("C")) {
            db.collection("order")
                    .whereEqualTo("userID", User.currUser.getEmail())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if(task.isSuccessful()){
                                for(DocumentSnapshot document : task.getResult()) {
                                    if(document.exists()) {
                                        try {
                                            String itemInfo = document.get("itemInfo").toString();
                                            String takeAway = document.get("takeAway").toString();
                                            Date collectionTime = sdf_fromDB.parse(document.get("collectionTime").toString());
                                            Date orderedTime = sdf_fromDB.parse(document.get("orderedTime").toString());
                                            String status = document.get("status").toString();
                                            String remark = document.get("remark").toString();
                                            String totalPrice = document.get("totalPrice").toString();
                                            String id = document.getId();
                                            String email = document.get("userID").toString();
                                            Order temp = new Order(itemInfo, takeAway, collectionTime, orderedTime, status, totalPrice);
                                            temp.setId(Integer.parseInt(id));
                                            temp.setUserID(email);
                                            temp.setRemark(remark);
                                            temp.setCanteenID(document.get("canteenID").toString());
                                            temp.setStallID(document.get("stallID").toString());
                                            temp.setCanteenName(Canteen.getCanteenList().get(document.get("canteenID").toString()).getName());
                                            temp.setStallName(document.get("stallName").toString());
                                            if(status.equals("FINISHED") || status.equals("COLLECTED") || status.equals("CANCELED")) {
                                                orderHist.add(temp);
                                            } else {
                                                currOrders.add(temp);
                                            }
                                        } catch (Exception e) {
                                            String msg = e.getMessage();
                                        }
                                    }
                                }
                                updateUI();
                            }
                        }
                    });
        } else {
            db.collection("order")
                    .whereEqualTo("stallID", User.currUser.getStallID())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                List<DocumentSnapshot> documents = task.getResult().getDocuments();
                                for(DocumentSnapshot document : documents) {
                                    if (document.exists()) {
                                        try {
                                            String itemInfo = document.get("itemInfo").toString();
                                            String remark = document.get("remark").toString();
                                            String takeAway = document.get("takeAway").toString();
                                            Date collectionTime = sdf_fromDB.parse(document.get("collectionTime").toString());
                                            Date orderedTime = sdf_fromDB.parse(document.get("orderedTime").toString());
                                            String status = document.get("status").toString();
                                            String totalPrice = document.get("totalPrice").toString();
                                            String id = document.getId();
                                            Order temp = new Order(itemInfo, takeAway, collectionTime, orderedTime, status, totalPrice);
                                            temp.setRemark(remark);
                                            temp.setStallName(document.get("stallName").toString());
                                            temp.setId(Integer.parseInt(id));
                                            if(status.equals("FINISHED") || status.equals("COLLECTED") || status.equals("CANCELED")) {
                                                orderHist.add(temp);
                                            } else {
                                                currOrders.add(temp);
                                            }
                                        } catch (Exception e) {
                                            String msg = e.getMessage();
                                        }
                                    }
                                }
                                updateUI();
                            }
                        }
                    });
        }
    }

    public void updateUI(){
        if(currOrders != null) {
            currOrders.sort(new Comparator<Order>() {
                @Override
                public int compare(Order o1, Order o2) {
                    if(o1.getCollectionTime().equals(o2.getCollectionTime())){
                        return o1.getOrderedTime().compareTo(o2.getOrderedTime());
                    } else {
                        return o1.getCollectionTime().compareTo(o2.getCollectionTime());
                    }
                }
            });
            OrderListAdapter adapter = new OrderListAdapter(this, R.layout.order_record_row, currOrders);
            currOrderLv.setAdapter(adapter);
        }

        if(orderHist != null) {
            orderHist.sort(new Comparator<Order>() {
                @Override
                public int compare(Order o1, Order o2) {
                    if(o1.getCollectionTime().equals(o2.getCollectionTime())){
                        return o1.getOrderedTime().compareTo(o2.getOrderedTime());
                    } else {
                        return o1.getCollectionTime().compareTo(o2.getCollectionTime());
                    }
                }
            });
            OrderListAdapter adapter = new OrderListAdapter(this, R.layout.order_record_row, orderHist);
            orderHistLv.setAdapter(adapter);
            load_gif.setVisibility(View.GONE);
        }

        if(!refreshFlag) {
            mPullToRefreshView.setRefreshing(refreshFlag);
            refreshFlag = !refreshFlag;
        }
    }

    @Override
    public void onBackPressed() {
        if(User.currUser.getType().equals("C")) {
            if(!CanteenListActivity.canteen_running) {
                startActivity(new Intent(getApplicationContext(), CanteenListActivity.class));
            }
            finish();
        } else {
            moveTaskToBack(true);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        order_running = true;
        if(update) {
          getOrderFromDB();
          update = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        order_running = true;
        if(update) {
            getOrderFromDB();
            update = false;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        order_running = false;
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        order_running = true;
    }
}
