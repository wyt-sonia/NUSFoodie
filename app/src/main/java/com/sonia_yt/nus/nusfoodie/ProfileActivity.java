package com.sonia_yt.nus.nusfoodie;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.widget.NestedScrollView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.rey.material.widget.Button;
import com.rey.material.widget.EditText;
import com.rey.material.widget.ImageView;
import com.rey.material.widget.RadioButton;
import com.rey.material.widget.TextView;
import com.sonia_yt.nus.nusfoodie.model.User;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private boolean enable = true;
    private User currUser;
    private Toolbar toolbar;
    private String actionFlag;
    private TextView greetingTv, userIDTv;
    private android.widget.TextView balanceTv;
    private RelativeLayout walletPuller, layer;
    private ExpandableLayout bottom_sheet_layout;
    private com.rey.material.widget.ImageButton navProfileBtn, navCartBtn, navCanteenListBtn, navOrderBtn;
    private EditText usernameEt, emailEt, telEt, personalIDEt, ageEt;
    private RadioButton genderRbF, genderRbM;
    private ImageView profileIconIv;
    private ConstraintLayout toolbar_userInfo;
    private android.widget.ImageView load_gif, coin_gif;
    private Button topUpBtn, withdrawBtn, updateBtn, signOutBtn;
    private TextInputLayout amountTL;
    private TextInputEditText amountEt;
    private NestedScrollView scrollView;
    private android.widget.TextView titleTv;
    private AppBarLayout appBarLayout;
    private AlertDialog.Builder builder_pw, builder_pro, builder_signOut;
    private double walletBalance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        setContentView(R.layout.activity_profile);

        this.builder_pro = new AlertDialog.Builder(this);
        this.builder_pw = new AlertDialog.Builder(this);
        this.builder_signOut = new AlertDialog.Builder(this);

        currUser = User.currUser;
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        greetingTv = (TextView) findViewById(R.id.greetingTv);
        userIDTv = (TextView) findViewById(R.id.userIDTv);
        profileIconIv = (ImageView) findViewById(R.id.profileIconIv);
        usernameEt = (EditText) findViewById(R.id.usernameEt);
        emailEt = (EditText) findViewById(R.id.emailEt);
        telEt = (EditText) findViewById(R.id.telEt);
        personalIDEt = (EditText) findViewById(R.id.personalIDEt);
        ageEt = (EditText) findViewById(R.id.ageEt);
        genderRbF = (RadioButton) findViewById(R.id.genderRbF);
        genderRbM = (RadioButton) findViewById(R.id.genderRbM);
        toolbar_userInfo = (ConstraintLayout) findViewById(R.id.toolbar_userInfo);
        walletPuller = (RelativeLayout) findViewById(R.id.walletPuller);
        bottom_sheet_layout = (ExpandableLayout) findViewById(R.id.bottom_sheet_layout);
        layer = (RelativeLayout) findViewById(R.id.layer);
        navCanteenListBtn = (com.rey.material.widget.ImageButton) findViewById(R.id.navCanteenListBtn);
        navProfileBtn = (com.rey.material.widget.ImageButton) findViewById(R.id.navProfileBtn);
        navCartBtn = (com.rey.material.widget.ImageButton) findViewById(R.id.navCartBtn);
        navOrderBtn = (com.rey.material.widget.ImageButton) findViewById(R.id.navOrderBtn);
        topUpBtn = (Button) findViewById(R.id.topUpBtn);
        withdrawBtn = (Button) findViewById(R.id.withdrawBtn);
        amountEt = (TextInputEditText) findViewById(R.id.amountEt);
        amountTL = (TextInputLayout) findViewById(R.id.amountTL);
        balanceTv = (android.widget.TextView) findViewById(R.id.balanceTv);
        scrollView = (NestedScrollView) findViewById(R.id.scrollView);
        updateBtn = (Button) findViewById(R.id.updateBtn);
        signOutBtn = (Button) findViewById(R.id.signOutBtn);
        titleTv = (android.widget.TextView) findViewById(R.id.titleTv);
        appBarLayout = (AppBarLayout) findViewById(R.id.appBarLayout);

        actionFlag = getIntent().getStringExtra("actionFlag");

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
                    toolbar.setTitle("My Profile");
                    toolbar_userInfo.setVisibility(View.INVISIBLE);
                    //collapsingToolbarLayout.setContentScrimColor(Color.parseColor("#37000000"));
                    isShow = true;
                } else if(isShow) {
                    toolbar.setTitle("");
                    toolbar_userInfo.setVisibility(View.VISIBLE);
                    isShow = false;
                }
            }
        });

        load_gif = (android.widget.ImageView) findViewById(R.id.load_gif);
        Glide.with(getApplicationContext())
                .asGif()
                .load(R.drawable.load_gif)
                .into(load_gif);

        coin_gif = (android.widget.ImageView) findViewById(R.id.coin_gif);

        scrollView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return bottom_sheet_layout.isExpanded();
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

        //region RadioButtons
        CompoundButton.OnCheckedChangeListener listener = new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    genderRbF.setChecked(genderRbF == buttonView);
                    genderRbM.setChecked(genderRbM == buttonView);
                }
            }
        };

        genderRbF.setOnCheckedChangeListener(listener);
        genderRbM.setOnCheckedChangeListener(listener);
        //endregion

        appBarLayout.setExpanded(!bottom_sheet_layout.isExpanded());

        balanceTv.setText("Wallet balance: S$ ...");

        updateUI();
        navBarInt();

        getWalletBalance();
    }

    private void refreshWalletLayout(String flag) {
        walletPuller.setEnabled(true);
        amountTL.setError(null);
        amountEt.setText("");

        if(flag.equals("pw")) {
            bottom_sheet_layout.setExpanded(false);
            appBarLayout.setExpanded(!bottom_sheet_layout.isExpanded());
            amountTL.setVisibility(View.GONE);
            topUpBtn.setVisibility(View.VISIBLE);
            topUpBtn.setEnabled(true);
            topUpBtn.setText("Update");
            withdrawBtn.setVisibility(View.GONE);
            balanceTv.setText("Email will be sent to update password.");
            titleTv.setText("= Update Password =");

            int sizeInDp = 24;
            int dpAsPixels = (int) (sizeInDp * getResources().getDisplayMetrics().density + 0.5f);
            walletPuller.setPadding(dpAsPixels, 0, dpAsPixels, 0);
            coin_gif.setImageResource(R.drawable.password);

            walletPuller.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!bottom_sheet_layout.isExpanded()) {
                        int sizeInDp = 12;
                        int dpAsPixels = (int) (sizeInDp * getResources().getDisplayMetrics().density + 0.5f);
                        walletPuller.setPadding(dpAsPixels, 0, dpAsPixels, 0);
                        coin_gif.setImageResource(R.drawable.close);
                        usernameEt.setEnabled(false);
                        ageEt.setEnabled(false);
                        telEt.setEnabled(false);
                        genderRbF.setEnabled(false);
                        genderRbM.setEnabled(false);
                    } else {
                        int sizeInDp = 24;
                        int dpAsPixels = (int) (sizeInDp * getResources().getDisplayMetrics().density + 0.5f);
                        walletPuller.setPadding(dpAsPixels, 0, dpAsPixels, 0);
                        coin_gif.setImageResource(R.drawable.password);
                        usernameEt.setEnabled(true);
                        ageEt.setEnabled(true);
                        telEt.setEnabled(true);
                        genderRbF.setEnabled(true);
                        genderRbM.setEnabled(true);
                    }
                    bottom_sheet_layout.toggle();
                    appBarLayout.setExpanded(!bottom_sheet_layout.isExpanded());
                }
            });

            topUpBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                        builder_pw.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                topUpBtn.setEnabled(false);
                                walletPuller.setEnabled(false);
                                load_gif.setVisibility(View.VISIBLE);
                                bottom_sheet_layout.setExpanded(false);
                                appBarLayout.setExpanded(true);
                                updatePw();
                            }
                        });

                        builder_pw.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        AlertDialog dialog = builder_pw.create();
                        dialog.setMessage("Do you confirm to update your password?");
                        dialog.show();
                    }
            });
        }

        if(flag.equals("payment")) {
            topUpBtn.setVisibility(View.VISIBLE);
            withdrawBtn.setVisibility(View.VISIBLE);
            topUpBtn.setEnabled(true);
            withdrawBtn.setEnabled(true);
            amountTL.setVisibility(View.GONE);
            titleTv.setText("= My Wallet =");
            balanceTv.setText("My wallet balance: S$ " + walletBalance);

            int sizeInDp = 24;
            int dpAsPixels = (int) (sizeInDp * getResources().getDisplayMetrics().density + 0.5f);
            walletPuller.setPadding(dpAsPixels, 0, dpAsPixels, 0);

            Glide.with(getApplicationContext())
                    .asGif()
                    .load(R.drawable.coin_2)
                    .into(coin_gif);

            walletPuller.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bottom_sheet_layout.toggle();
                    appBarLayout.setExpanded(!bottom_sheet_layout.isExpanded());
                }
            });

            topUpBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    amountTL.setVisibility(View.VISIBLE);
                    withdrawBtn.setVisibility(View.GONE);
                    coin_gif.setImageResource(R.drawable.close);
                    int sizeInDp = 12;
                    int dpAsPixels = (int) (sizeInDp * getResources().getDisplayMetrics().density + 0.5f);
                    walletPuller.setPadding(dpAsPixels, 0, dpAsPixels, 0);
                    walletPuller.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            getWalletBalance();
                        }
                    });
                    topUpBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(validate_w("T")) {
                                load_gif.setVisibility(View.VISIBLE);
                                walletPuller.setEnabled(false);
                                topUpBtn.setEnabled(false);
                                updateWalletBalance("T", amountEt.getText().toString());
                            }
                        }
                    });
                }
            });

            withdrawBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    amountTL.setVisibility(View.VISIBLE);
                    topUpBtn.setVisibility(View.GONE);
                    coin_gif.setImageResource(R.drawable.close);
                    int sizeInDp = 12;
                    int dpAsPixels = (int) (sizeInDp * getResources().getDisplayMetrics().density + 0.5f);
                    walletPuller.setPadding(dpAsPixels, 0, dpAsPixels, 0);
                    walletPuller.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                           getWalletBalance();
                        }
                    });
                    withdrawBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(validate_w("W")) {
                                load_gif.setVisibility(View.VISIBLE);
                                withdrawBtn.setEnabled(false);
                                walletPuller.setEnabled(false);
                                updateWalletBalance("W", amountEt.getText().toString());
                            }
                        }
                    });
                }
            });
        }

    }

    private void updateWalletBalance(String flag, String amountS) {

        double amount = Double.parseDouble(amountS);
        if(flag.equals("W")) {
            amount = -amount;
        }
        double newBalance = walletBalance + amount;
        bottom_sheet_layout.setExpanded(false);

        Map<String, Object> newBalanceMap = new HashMap<>();
        newBalanceMap.put("walletBalance", newBalance);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .document(User.currUser.getEmail())
                .update(newBalanceMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        User.currUser.setWalletBalance(newBalance);
                        refreshWalletLayout("payment");
                        balanceTv.setText("My wallet balance: S$ " + newBalance);
                        walletBalance = newBalance;
                        load_gif.setVisibility(View.GONE);
                        bottom_sheet_layout.setExpanded(true);
                    }
                });
    }

    private void updatePw() {
        FirebaseAuth auth = FirebaseAuth.getInstance();

        auth.sendPasswordResetEmail(User.currUser.getEmail())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "Email sent, please follow the email to update your password.", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                            intent.putExtra("actionFlag", "view");
                            startActivity(intent);
                        }
                    }
                });
    }

    private boolean validate_w(String flag) {
        boolean res = true;
        if(amountEt.getText().toString().isEmpty()) {
            amountTL.setError("Please enter amount.");
            res = false;
        } else {
            double amount = Double.parseDouble(amountEt.getText().toString());
            double balance = walletBalance;

            if(flag.equals("W")) {
                if(amount > balance) {
                    amountTL.setError("Please enter valid amount.");
                    res = false;
                }
            }
        }

        return res;
    }

    private void updateUI () {

        if(currUser.getGender().equals("M")){
            profileIconIv.setImageResource(R.drawable.pro_m_w);
        }
        else {
            profileIconIv.setImageResource(R.drawable.pro_f_w);
        }

        boolean flag = true;
        if(actionFlag.equals("view")) {
            flag = false;
        }

        usernameEt.setEnabled(flag);
        emailEt.setEnabled(false);
        telEt.setEnabled(flag);
        personalIDEt.setEnabled(false);
        ageEt.setEnabled(flag);
        genderRbM.setEnabled(flag);
        genderRbF.setEnabled(flag);
        greetingTv.setText("Hello, " + currUser.getUsername());

        userIDTv.setText(currUser.getEmail());
        usernameEt.setText(currUser.getUsername());
        personalIDEt.setText(currUser.getPersonalID());
        emailEt.setText(currUser.getEmail());
        telEt.setText(currUser.getTel());
        ageEt.setText(currUser.getAge());

        if(currUser.getGender().equals("M")){
            genderRbM.setChecked(true);
            genderRbF.setChecked(false);
            if(actionFlag.equals("view"))
                genderRbM.setTextColor(Color.parseColor("#757575"));
        } else {
            genderRbM.setChecked(false);
            genderRbF.setChecked(true);
            if(actionFlag.equals("view"))
                genderRbF.setTextColor(Color.parseColor("#757575"));
        }

        if(actionFlag.equals("view")) {
            usernameEt.setTextColor(Color.parseColor("#757575"));
            emailEt.setTextColor(Color.parseColor("#757575"));
            telEt.setTextColor(Color.parseColor("#757575"));
            personalIDEt.setTextColor(Color.parseColor("#757575"));
            ageEt.setTextColor(Color.parseColor("#757575"));
            updateBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                    intent.putExtra("actionFlag", "update");
                    startActivity(intent);
                    finish();
                }
            });

            signOutBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    builder_signOut.setPositiveButton("Sign out", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            FirebaseAuth.getInstance().signOut();
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        }
                    });

                    builder_signOut.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });

                    AlertDialog dialog = builder_signOut.create();
                    dialog.setMessage("Do you want to sign out?");
                    dialog.show();
                }
            });
        }

        if(actionFlag.equals("update")) {
            usernameEt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if(hasFocus)
                        usernameEt.setError(null);
                }
            });

            telEt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if(hasFocus)
                        telEt.setError(null);
                }
            });

            ageEt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if(hasFocus)
                        ageEt.setError(null);
                }
            });

            updateBtn.setText("Save");
            updateBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(validateProfile()) {

                        builder_pro.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                load_gif.setVisibility(View.VISIBLE);
                                updateBtn.setEnabled(false);
                                signOutBtn.setEnabled(false);
                                walletPuller.setEnabled(false);
                                final String username, tel, gender, age;
                                username = usernameEt.getText().toString();
                                tel = telEt.getText().toString();
                                gender = genderRbM.isChecked() ? "M" : "F" ;
                                age = ageEt.getText().toString();

                                Map<String, Object> user = new HashMap<>();
                                user.put("username", username);
                                user.put("tel", tel);
                                user.put("gender", gender);
                                user.put("age",age);

                                FirebaseFirestore db = FirebaseFirestore.getInstance();
                                db.collection("users")
                                        .document(User.currUser.getEmail())
                                        .update(user)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                User.currUser.setGender(gender);
                                                User.currUser.setAge(age);
                                                User.currUser.setTel(tel);
                                                User.currUser.setUsername(username);
                                                load_gif.setVisibility(View.GONE);
                                                Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                                                intent.putExtra("actionFlag", "view");
                                                startActivity(intent);
                                            }
                                        });
                            }
                        });
                        builder_pro.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        AlertDialog dialog = builder_pro.create();
                        dialog.setMessage("Do you confirm to update your profile?");
                        dialog.show();
                    }
                }
            });

            signOutBtn.setText("Cancel");
            signOutBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                    intent.putExtra("actionFlag", "view");
                    startActivity(intent);
                    finish();
                }
            });

        }
    }

    private boolean validateProfile() {
        boolean res = true;
        final String username, tel, gender, age;
        username = usernameEt.getText().toString();
        tel = telEt.getText().toString();
        age = ageEt.getText().toString();

        if (TextUtils.isEmpty(username)) {
            usernameEt.setError("please enter username");
            res = false;
        } else if(username.length() > 20) {
            usernameEt.setError("The maximum length of username is 20.");
            res = false;
        }

        if (TextUtils.isEmpty(tel)) {
            telEt.setError( "please enter tel number");
            res = false;
        } else if(!isValidTel(telEt.getText().toString())) {
            telEt.setError("the tel number is invalid.");
            res = false;
        } else {
            telEt.setError(null);
        }

        if (!genderRbM.isChecked() && !genderRbF.isChecked()) {
            Toast.makeText(this, "please choose gender", Toast.LENGTH_LONG).show();
            res = false;
        }

        if (TextUtils.isEmpty(age)) {
            ageEt.setError("please enter your age");
            res = false;
        } else if(!isValidAge(ageEt.getText().toString())) {
            ageEt.setError("the age is invalid.");
            res = false;
        } else {
            ageEt.setError(null);
        }

        return res;
    }

    public boolean isValidTel(CharSequence tel) {
        if (tel == null)
            return false;

        return tel.length() == 8;
    }

    public boolean isValidAge(CharSequence age) {
        if (age == null || age.charAt(0) == '0')
            return false;
        if (Integer.parseInt(age.toString()) > 100 || Integer.parseInt(age.toString()) < 0)
            return false;

        return true;
    }

    private void navBarInt() {
        navOrderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), OrderRecordActivity.class));
                finish();
            }
        });

        navProfileBtn.setEnabled(false);
        navProfileBtn.setImageResource(R.drawable.profile_icon_w);
        navProfileBtn.setBackgroundColor(Color.parseColor("#FF8A65"));

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
                    if(!CanteenListActivity.canteen_running)
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
                            if(actionFlag.equals("view"))
                                refreshWalletLayout("payment");
                            else
                                refreshWalletLayout("pw");
                        }
                    }
                });
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
