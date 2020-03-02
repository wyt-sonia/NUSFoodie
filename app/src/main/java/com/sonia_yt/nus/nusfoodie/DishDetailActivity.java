package com.sonia_yt.nus.nusfoodie;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.rey.material.widget.ImageButton;
import com.rey.material.widget.ImageView;
import com.rey.material.widget.TextView;
import com.sonia_yt.nus.nusfoodie.model.Cart;
import com.sonia_yt.nus.nusfoodie.model.Dish;
import com.sonia_yt.nus.nusfoodie.model.User;

import java.util.ArrayList;

public class DishDetailActivity extends AppCompatActivity {

    private StorageReference mStorageRef;

    private static Dish dish;
    private static double totalPrice;
    private static int numOfItems;
    private String stallOpen, stallClose, stallPrepareTime, stallName, title;
    private ImageView dishIv;
    private TextView nameTv, typeTv, calorieTv, descTv, priceTv, totalTv, numOfItemsTv, numLbl;
    private ImageButton plusBtn, minusBtn;
    private Button addToCartBtn;
    private ImageButton navProfileBtn, navCartBtn, navCanteenListBtn, navOrderBtn;
    private AlertDialog.Builder builder;
    private android.widget.ImageView load_gif, hi_gif;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        setContentView(R.layout.activity_dish_detail);

        mStorageRef = FirebaseStorage.getInstance().getReference("dish");
        this.builder = new AlertDialog.Builder(this);

        Intent intent = getIntent();

        totalPrice = 0.00;
        numOfItems = 0;

        title = "";
        dish = new Dish();

        dish.setId(intent.getStringExtra("id"));
        dish.setCanteenID(intent.getStringExtra("canteenID"));
        dish.setStallID(intent.getStringExtra("stallID"));
        dish.setTypeName(intent.getStringExtra("type"));
        dish.setTypeID(intent.getStringExtra("typeID"));
        dish.setName(intent.getStringExtra("name"));
        title = dish.getName();
        dish.setCalorie(intent.getStringExtra("calorie"));
        dish.setDescription(intent.getStringExtra("description"));
        dish.setPrice(intent.getStringExtra("price"));
        dish.setPictureAddress(intent.getStringExtra("pictureAddress"));

        stallClose = getIntent().getStringExtra("stallClose");
        stallOpen = getIntent().getStringExtra("stallOpen");
        stallPrepareTime = getIntent().getStringExtra("stallPrepareTime");
        stallName = getIntent().getStringExtra("stallName");

        dishIv = (ImageView) findViewById(R.id.dishIv);
        nameTv = (TextView) findViewById(R.id.nameTv);
        typeTv = (TextView) findViewById(R.id.typeTv);
        calorieTv = (TextView) findViewById(R.id.calorieTv);
        descTv = (TextView) findViewById(R.id.desTv);
        priceTv = (TextView) findViewById(R.id.unitPriceTv);
        plusBtn = (ImageButton) findViewById(R.id.plusBtn);
        minusBtn = (ImageButton) findViewById(R.id.minusBtn);
        numOfItemsTv = (TextView) findViewById(R.id.numOfItemsTv);
        totalTv = (TextView) findViewById(R.id.totalPriceTv);
        addToCartBtn = (Button) findViewById(R.id.addToCartBtn);
        navCanteenListBtn = (ImageButton) findViewById(R.id.navCanteenListBtn);
        navProfileBtn = (ImageButton) findViewById(R.id.navProfileBtn);
        navCartBtn = (ImageButton) findViewById(R.id.navCartBtn);
        navOrderBtn = (ImageButton) findViewById(R.id.navOrderBtn);
        numLbl = (TextView) findViewById(R.id.numLbl);
        toolbar = (Toolbar) findViewById(R.id.toolbar);

        hi_gif = (android.widget.ImageView) findViewById(R.id.hi_gif);
        Glide.with(getApplicationContext())
                .asGif()
                .load(R.drawable.hi_bear_dribbble)
                .into(hi_gif);

        load_gif = (android.widget.ImageView) findViewById(R.id.load_gif);
        Glide.with(getApplicationContext())
                .asGif()
                .load(R.drawable.load_gif)
                .into(load_gif);

        nameTv.setText(dish.getName());
        typeTv.setText(dish.getTypeName());
        calorieTv.setText("Energy: " + dish.getCalorie() + "kcal");
        descTv.setText(dish.getDescription());
        priceTv.setText("Unit Price: S$" + dish.getPrice());
        numOfItemsTv.setText(numOfItems + "");
        totalTv.setText("Total Price: S$" + totalPrice);

        navBarInt();

        StorageReference path = mStorageRef.child(dish.getPictureAddress());
        path.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
        {
            @Override
            public void onSuccess(Uri downloadUrl)
            {
                Glide.with(getApplicationContext())
                        .load(downloadUrl)
                        .dontAnimate()
                        .into(dishIv);
            }
        });

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

        if (User.currUser.getType().equals("C")) {
            plusBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int curr = Integer.parseInt(numOfItemsTv.getText().toString());
                    numOfItems = ++curr;
                    totalPrice = numOfItems * Double.parseDouble(dish.getPrice());
                    numOfItemsTv.setText(numOfItems + "");
                    totalTv.setText("Total Price: S$" + totalPrice);
                }
            });

            minusBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int curr = Integer.parseInt(numOfItemsTv.getText().toString());
                    if(curr <= 0) {
                        Toast.makeText(getApplicationContext(), "The minimum number of item is 0.", Toast.LENGTH_SHORT).show();
                    } else {
                        numOfItems = --curr;
                        numOfItemsTv.setText(numOfItems + "");
                        totalPrice = numOfItems * Double.parseDouble(dish.getPrice());
                        totalTv.setText("Total Price: S$" + totalPrice);
                    }
                }
            });

            addToCartBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int numOfItem = Integer.parseInt(numOfItemsTv.getText().toString());
                    if(numOfItem == 0) {
                        Toast.makeText(getApplicationContext(), "Number of items cannot be 0.", Toast.LENGTH_SHORT).show();
                        return;
                    } else {
                        ArrayList<Dish> items = new ArrayList<>();
                        Cart myCart;
                        while (numOfItem != 0) {
                            items.add(dish);
                            numOfItem--;
                        }
                        if(User.currUser.getMyCart() == null) {
                            myCart = new Cart(dish.getCanteenID(), dish.getStallID());
                            myCart.setStallOpen(stallOpen);
                            myCart.setStallClose(stallClose);
                            myCart.setStallPrepareTime(stallPrepareTime);
                            myCart.setStallName(stallName);
                            User.currUser.setMyCart(myCart);
                            User.currUser.getMyCart().addItem(items);
                            Toast.makeText(getApplicationContext(),"Items added successfully!", Toast.LENGTH_SHORT).show();
                            updateUI();
                        } else {
                            myCart = User.currUser.getMyCart();
                            if(dish.getCanteenID().equals(myCart.getCanteenID()) && dish.getStallID().equals(myCart.getStallID())) {
                                myCart.addItem(items);
                                User.currUser.setMyCart(myCart);
                                updateUI();
                            } else {
                                builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        Cart myCart = new Cart(dish.getCanteenID(), dish.getStallID());
                                        myCart.setStallOpen(stallOpen);
                                        myCart.setStallClose(stallClose);
                                        myCart.setStallPrepareTime(stallPrepareTime);
                                        myCart.setStallName(stallName);
                                        myCart.addItem(items);
                                        User.currUser.setMyCart(myCart);
                                        Toast.makeText(getApplicationContext(),"Items added successfully!", Toast.LENGTH_SHORT).show();
                                        updateUI();
                                    }
                                });
                                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        // User cancelled the dialog
                                    }
                                });

                                // Create the AlertDialog
                                AlertDialog dialog = builder.create();
                                dialog.setTitle("Item from a different stall/canteen");
                                dialog.setMessage("The items you select are from a different stall/canteen than the items in your cart." +
                                        " Adding them will clear what you have added. Do you still want to add them?");
                                dialog.show();
                            }
                        }
                    }
                }
            });
        } else {
            plusBtn.setVisibility(View.GONE);
            minusBtn.setVisibility(View.GONE);
            numOfItemsTv.setVisibility(View.GONE);
            numLbl.setVisibility(View.GONE);
            totalTv.setVisibility(View.GONE);

            addToCartBtn.setText("Update");
            addToCartBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent intent = new Intent(getApplicationContext(), ManageDishActivity.class);
                            intent.putExtra("actionFlag", "update");
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
                            startActivity(intent);
                            finish();
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    });

                    // Create the AlertDialog
                    AlertDialog dialog = builder.create();
                    dialog.setMessage("Do you want to update : " + dish.getName() + "?");
                    dialog.show();

                }
            });
        }

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

        load_gif.setVisibility(View.GONE);
        addToCartBtn.setVisibility(View.VISIBLE);
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
            navCartBtn.setImageResource(R.drawable.canteen_icon_2);
            navCartBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), DishListActivity.class);
                    intent.putExtra("stallID", User.currUser.getStallID());
                    intent.putExtra("canteenID", User.currUser.getCanteenID());
                    startActivity(intent);
                    finish();
                }
            });
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

    public void updateUI() {
        totalPrice = 0.00;
        numOfItems = 0;
        totalTv.setText("Total Price: S$" + totalPrice);
        numOfItemsTv.setText(numOfItems + "");
        Intent intent = new Intent(this, CartActivity.class);
        this.startActivity(intent);
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
