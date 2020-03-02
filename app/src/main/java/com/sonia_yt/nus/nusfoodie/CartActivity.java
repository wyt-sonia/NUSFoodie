package com.sonia_yt.nus.nusfoodie;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.rey.material.widget.Button;
import com.rey.material.widget.ListView;
import com.rey.material.widget.TextView;
import com.sonia_yt.nus.nusfoodie.model.Cart;
import com.sonia_yt.nus.nusfoodie.model.Dish;
import com.sonia_yt.nus.nusfoodie.model.Order;
import com.sonia_yt.nus.nusfoodie.model.User;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class CartActivity extends AppCompatActivity {

    private double walletBalance;
    private Date collectionTime;
    private ListView cartItemLv;
    private Cart myCart;
    private TextView emptyCartTv, dateTimeTv, balanceTv;
    private RelativeLayout checkOutPuller;
    private TextInputEditText remarksEv;
    private ImageButton dateTimePickerBtn, navBackBtn;
    private Button checkOutBtn;
    private AlertDialog.Builder builder_sec, builder_fail, builder_wb;
    private ConstraintLayout userActionBar;
    private ImageView load_gif, coin_gif, pullerIcon;
    private TimePickerDialog.OnTimeSetListener mTimeSetListener;
    private ExpandableLayout bottom_sheet_layout;
    private com.rey.material.widget.ImageButton navProfileBtn, navCartBtn, navCanteenListBtn, navOrderBtn;
    public static boolean cart_running;

    private final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

    private String stallOpen, stallClose, stallPrepareTime, stallName;

    static Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        setContentView(R.layout.activity_cart);

        this.myCart = User.currUser.getMyCart();

        this.builder_sec = new AlertDialog.Builder(this);
        this.builder_fail = new AlertDialog.Builder(this);
        this.builder_wb = new AlertDialog.Builder(this);
        cart_running = true;

        mContext = this;

        if(this.myCart != null) {
            stallPrepareTime = User.currUser.getMyCart().getStallPrepareTime();
            stallOpen = User.currUser.getMyCart().getStallOpen();
            stallClose = User.currUser.getMyCart().getStallClose();
            stallName = User.currUser.getMyCart().getStallName();
        }

        navCanteenListBtn = (com.rey.material.widget.ImageButton) findViewById(R.id.navCanteenListBtn);
        navProfileBtn = (com.rey.material.widget.ImageButton) findViewById(R.id.navProfileBtn);
        navCartBtn = (com.rey.material.widget.ImageButton) findViewById(R.id.navCartBtn);
        navOrderBtn = (com.rey.material.widget.ImageButton) findViewById(R.id.navOrderBtn);
        bottom_sheet_layout = (ExpandableLayout) findViewById(R.id.bottom_sheet_layout);
        checkOutPuller = (RelativeLayout) findViewById(R.id.checkOutPuller);
        pullerIcon = (ImageView) findViewById(R.id.pullerIcon);
        balanceTv = (TextView) findViewById(R.id.balanceTv);
        coin_gif = (ImageView) findViewById(R.id.coin_gif);

        load_gif = (ImageView) findViewById(R.id.load_gif);

        Glide.with(getApplicationContext())
                .asGif()
                .load(R.drawable.load_gif)
                .into(load_gif);

        Glide.with(getApplicationContext())
                .asGif()
                .load(R.drawable.coin_2)
                .into(coin_gif);

        navBarInt();

        emptyCartTv = (TextView) findViewById(R.id.emptyCartTv);
        cartItemLv = (ListView) findViewById(R.id.cartItemLv);

        dateTimeTv = (TextView) findViewById(R.id.dateTimeTv);
        remarksEv = (TextInputEditText) findViewById(R.id.remarksEv);

        dateTimePickerBtn = (ImageButton) findViewById(R.id.dateTimePickerBtn);
        navBackBtn = (ImageButton) findViewById(R.id.navBackBtn);
        checkOutBtn = (Button) findViewById(R.id.checkOutBtn);


        checkOutPuller.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottom_sheet_layout.toggle();
                if(bottom_sheet_layout.isExpanded()) {
                    pullerIcon.setImageResource(R.drawable.down);
                } else {
                    pullerIcon.setImageResource(R.drawable.up);
                }
            }
        });

        dateTimePickerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getTimePicker();
            }
        });

        dateTimeTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getTimePicker();
            }
        });

        navBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!CanteenListActivity.canteen_running) {
                    startActivity(new Intent(getApplicationContext(), CanteenListActivity.class));
                }
                cart_running = false;
                finish();
            }
        });

        checkOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String msg = "Do you want to proceed to make the payment?";
                if(myCart != null && myCart.getDishInCart().size() > 0 && collectionTime != null
                        && validateTime() && validateBalance()) {
                    builder_sec.setPositiveButton("Check Out", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            load_gif.setVisibility(View.VISIBLE);
                            bottom_sheet_layout.setExpanded(false);
                            navBackBtn.setEnabled(false);
                            checkOutBtn.setEnabled(false);
                            updateWalletBalance();
                        }
                    });

                    builder_sec.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    });
                    AlertDialog dialog = builder_sec.create();
                    dialog.setMessage(msg);
                    dialog.show();
                } else {
                    if (myCart == null || myCart.getDishInCart().size() == 0){
                        msg = "The cart is empty.";
                        builder_fail.setNegativeButton("Back", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User cancelled the dialog
                            }
                        });

                        AlertDialog dialog = builder_fail.create();
                        dialog.setTitle("Invalid order information");
                        dialog.setMessage(msg);
                        dialog.show();
                    } else if (!validateBalance()) {
                        msg = "Insufficient wallet balance. Top up your wallet now?";
                        builder_wb.setPositiveButton("Top up", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(CartActivity.this, ProfileActivity.class);
                                intent.putExtra("actionFlag", "view");
                                startActivity(intent);
                                cart_running = false;
                                finish();
                            }
                        });

                        builder_wb.setNegativeButton("Back", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User cancelled the dialog
                            }
                        });

                        AlertDialog dialog = builder_wb.create();
                        dialog.setTitle("Insufficient Balance");
                        dialog.setMessage(msg);
                        dialog.show();
                    } else if (collectionTime == null) {
                        msg = "Please set your collection time.";
                        builder_fail.setNegativeButton("Back", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User cancelled the dialog
                            }
                        });

                        AlertDialog dialog = builder_fail.create();
                        dialog.setTitle("Invalid order information");
                        dialog.setMessage(msg);
                        dialog.show();
                    } else if(!validateTime()){
                        msg = "The stall's operating hour is " + stallOpen + " to " + stallClose
                                + " \nand the order should be made " + stallPrepareTime +" minutes in advance.";
                        builder_fail.setNegativeButton("Back", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User cancelled the dialog
                            }
                        });

                        AlertDialog dialog = builder_fail.create();
                        dialog.setTitle("Invalid order information");
                        dialog.setMessage(msg);
                        dialog.show();
                    }
                }

            }
        });

        mTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                String flag = "am";
                int temp = 0;
                if(hourOfDay >= 12) {
                    temp = 12;
                    flag = "pm";
                }
                String date = hourOfDay - temp + flag + " " + minute + "min";
                dateTimeTv.setText(date);

                try {
                    Calendar calendar = Calendar.getInstance();
                    String dateString = minute + "/" + hourOfDay + "/" + calendar.get(Calendar.DAY_OF_MONTH) + "/"
                            + (calendar.get(Calendar.MONTH) + 1) + "/" + calendar.get(Calendar.YEAR);
                    SimpleDateFormat sdf = new SimpleDateFormat("mm/HH/dd/MM/yyyy");
                    collectionTime  = sdf.parse(dateString);
                    String q = "";
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        };
        getWalletBalance();
        balanceTv.setText("Wallet balance: S$ ...");
    }

    private void updateWalletBalance() {
        double amount = myCart.getTotalPrice();
        double newBalance = walletBalance - amount;

        Map<String, Object> newBalanceMap = new HashMap<>();
        newBalanceMap.put("walletBalance", newBalance);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .document(User.currUser.getEmail())
                .update(newBalanceMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        updateThirdPartyAccount();
                    }
                });
    }

    private void updateThirdPartyAccount() {
        double amount = myCart.getTotalPrice();
        DocumentReference thirdPartyAccount = FirebaseFirestore.getInstance()
                .collection("third_party_account")
                .document("account");

        thirdPartyAccount
                .update("balance", FieldValue.increment(amount))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                String remark = remarksEv.getText().toString();
                if (TextUtils.isEmpty(remark))
                    remark = "Nil";
                Date now = Calendar.getInstance().getTime();
                Order myOrder = new Order(myCart.getCanteenID(), myCart.getStallID(),
                        User.currUser.getEmail(), myCart.getTotalPrice() + "",
                        remark, false, myCart.getDishInCart(), collectionTime, now);
                myOrder.setStallName(stallName);
                myOrder.addOrderToDB();
            }
        });
    }

    private void getTimePicker() {
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int min = cal.get(Calendar.MINUTE);

        MyTimePickerDialog dialog = new MyTimePickerDialog(CartActivity.this,
                mTimeSetListener,
                hour,min,true);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setTitle("Set Collection Time");
        dialog.show();
    }

    private void getCartInfo() {
        if( myCart == null || myCart.getDishInCart().size() == 0) {
            cartItemLv.setVisibility(View.GONE);
            emptyCartTv.setVisibility(View.VISIBLE);
            Toast.makeText(this,"No item is in cart now.", Toast.LENGTH_SHORT).show();

        } else {
            ArrayList<Dish> diffItems = new ArrayList<>();
            diffItems.add(myCart.getDishInCart().get(0));
            emptyCartTv.setVisibility(View.GONE);
            cartItemLv.setVisibility(View.VISIBLE);
            for(Dish d : myCart.getDishInCart()) {
                if(!diffItems.contains(d))
                    diffItems.add(d);
            }
            CartItemListAdapter adapter = new CartItemListAdapter(this, R.layout.cart_item_row, diffItems);
            cartItemLv.setAdapter(adapter);
        }
    }

    private void navBarInt() {
        navOrderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), OrderRecordActivity.class));
                cart_running = false;
                finish();
            }
        });
        navCartBtn.setEnabled(false);
        navCartBtn.setImageResource(R.drawable.cart_icon_w);
        navCartBtn.setBackgroundColor(Color.parseColor("#FF8A65"));
        navCanteenListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), CanteenListActivity.class));
                cart_running = false;
                finish();
            }
        });
        navProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                intent.putExtra("actionFlag", "view");
                startActivity(intent);
                cart_running = false;
                finish();
            }
        });
    }

    public static void afterCheckOut(boolean res) {
        if(res) {
            User.currUser.setMyCart(null);
            mContext.startActivity(new Intent(mContext, OrderRecordActivity.class));
            cart_running = false;
            ((Activity)mContext).finish();
        } else {
            ((Activity)mContext).findViewById(R.id.load_gif).setVisibility(View.GONE);
            ((Activity)mContext).findViewById(R.id.checkOutBtn).setEnabled(true);
        }
    }

    private boolean validateTime() {
        boolean res = true;
        try{
            Date open = sdf.parse(stallOpen);
            Date close = sdf.parse(stallClose);
            Date now = Calendar.getInstance().getTime();
            Calendar cal = Calendar.getInstance();

            if(collectionTime.before(now)) {
                res = false;
            } else if(differenceBetween(now, open) < 0) {
                cal.setTime(open);
                cal.add(Calendar.MINUTE, Integer.parseInt(stallPrepareTime));
                if(differenceBetween(collectionTime, cal.getTime()) < 0) {
                    res = false;
                }
            } else if(differenceBetween(now, open) > 0) {
                cal.setTime(now);
                cal.add(Calendar.MINUTE, Integer.parseInt(stallPrepareTime));
                if(differenceBetween(collectionTime, cal.getTime()) < 0){
                    res = false;
                }
            }

            cal.setTime(collectionTime);
            if(differenceBetween(cal.getTime(), close) > 0)
                res = false;

        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
        return res;
    }

    private boolean validateBalance() {
        return (walletBalance - myCart.getTotalPrice()) >= 0;
    }

    private long differenceBetween(Date currentTime, Date timeToRun)
    {
        Calendar currentCal = Calendar.getInstance();
        currentCal.setTime(currentTime);

        Calendar runCal = Calendar.getInstance();
        runCal.setTime(timeToRun);
        runCal.set(Calendar.DAY_OF_MONTH, currentCal.get(Calendar.DAY_OF_MONTH));
        runCal.set(Calendar.MONTH, currentCal.get(Calendar.MONTH));
        runCal.set(Calendar.YEAR, currentCal.get(Calendar.YEAR));

        return currentCal.getTimeInMillis() - runCal.getTimeInMillis();
    }

    @Override
    public void onBackPressed() {
        if(!CanteenListActivity.canteen_running) {
            startActivity(new Intent(getApplicationContext(), CanteenListActivity.class));
        }
        cart_running = false;
        finish();
    }

    private void getWalletBalance() {
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(User.currUser.getEmail())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()) {
                            walletBalance = task.getResult().getDouble("walletBalance");
                            balanceTv.setText("Wallet balance: S$ " + walletBalance);
                            getCartInfo();
                        }
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cart_running = false;
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        cart_running = true;
    }
}
