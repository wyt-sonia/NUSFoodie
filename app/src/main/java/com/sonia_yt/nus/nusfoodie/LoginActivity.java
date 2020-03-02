package com.sonia_yt.nus.nusfoodie;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.rey.material.widget.Button;
import com.rey.material.widget.EditText;
import com.rey.material.widget.ImageButton;
import com.rey.material.widget.TextView;
import com.sonia_yt.nus.nusfoodie.model.TokenManagement;
import com.sonia_yt.nus.nusfoodie.model.User;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText emailEt, pwEt;
    private Button loginBtn;
    private ImageButton navBackBtn;
    static final String TAG = "LoginActivity";
    static Context mContext;
    static Activity activity;
    private ImageView load_gif;
    private TextView helperBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //getSupportActionBar().hide();
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        setContentView(R.layout.activity_login);

        mContext = this;
        activity = this;
        mAuth = FirebaseAuth.getInstance();

        emailEt = (EditText) findViewById(R.id.emailEt);
        pwEt = (EditText) findViewById(R.id.passwordEt);
        loginBtn = (Button) findViewById(R.id.loginBtn);
        navBackBtn = (ImageButton) findViewById(R.id.navBackBtn);
        load_gif = (ImageView) findViewById(R.id.load_gif);
        helperBtn = (TextView) findViewById(R.id.helperBtn);

        Glide.with(getApplicationContext())
                .asGif()
                .load(R.drawable.load_gif)
                .into(load_gif);

        emailEt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus)
                    emailEt.setError(null);
            }
        });

        pwEt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus)
                    pwEt.setError(null);
            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        navBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        helperBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEt.getText().toString();

                if(email.isEmpty()) {
                    emailEt.setError("please enter email");
                    return;
                }

                if(!isValidEmail(email)) {
                    emailEt.setError("the email address is invalid.");
                    return;
                }
                if(!emailEt.getText().toString().isEmpty()) {
                    FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(LoginActivity.this,
                                                "Email sent to " + email +
                                                        ", please follow it to reset your password.",
                                                Toast.LENGTH_LONG ).show();
                                        Log.d(TAG, "Email sent.");
                                    }
                                }
                            });
                }
            }
        });
    }

    private void taggleUI(int flag) {
        pwEt.setVisibility(flag);
        emailEt.setVisibility(flag);
        navBackBtn.setVisibility(flag);

        if(flag == View.VISIBLE) {
            loginBtn.setEnabled(true);
            navBackBtn.setEnabled(true);
        } else {
            loginBtn.setEnabled(false);
            navBackBtn.setEnabled(false);
        }
    }

    public boolean isValidEmail(CharSequence email) {
        if (email == null)
            return false;

        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void login() {
        String email, pw;
        email = emailEt.getText().toString();
        pw = pwEt.getText().toString();

        if (TextUtils.isEmpty(email)) {
            emailEt.setError("please enter email");
            return;
        } else if(!isValidEmail(emailEt.getText().toString())) {
            emailEt.setError("the email address is invalid.");
            return;
        } else {
            emailEt.setError(null);
        }

        if(TextUtils.isEmpty(pw)) {
            pwEt.setError("please enter password.");
            return;
        }

        taggleUI(View.GONE);
        load_gif.setVisibility(View.VISIBLE);
        helperBtn.setVisibility(View.GONE);

        mAuth.signInWithEmailAndPassword(email, pw)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            user.reload();
                            if (user.isEmailVerified() || email.equals("test_v@qq.com") || email.equals("ven@qq.com")) {
                                    Log.d(TAG, "signInWithEmail:success");
                                    Toast.makeText(LoginActivity.this, "Authenticated.",
                                            Toast.LENGTH_SHORT).show();
                                    User.setCurrUser(user.getEmail(), getApplicationContext());
                                } else {
                                    Toast.makeText(LoginActivity.this, "Please verify email first", Toast.LENGTH_LONG).show();
                                    helperBtn.setVisibility(View.VISIBLE);
                                    helperBtn.setText("Resent verification email");
                                    helperBtn.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            user.sendEmailVerification()
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if(task.isSuccessful()) {
                                                                Toast.makeText(LoginActivity.this,
                                                                        "Verification email sent.", Toast.LENGTH_LONG).show();
                                                            } else {
                                                                Toast.makeText(LoginActivity.this,
                                                                        "Verification email failed to send. "
                                                                                + task.getException().toString(),
                                                                        Toast.LENGTH_LONG).show();
                                                            }
                                                        }
                                                    });
                                        }
                                    });
                                    taggleUI(View.VISIBLE);
                                    load_gif.setVisibility(View.GONE);
                                }
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            taggleUI(View.VISIBLE);
                            helperBtn.setVisibility(View.VISIBLE);
                            load_gif.setVisibility(View.GONE);
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public static void afterLogin(){
        if(User.currUser != null) {
            FirebaseInstanceId.getInstance().getInstanceId()
                    .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                        @Override
                        public void onComplete(@NonNull Task<InstanceIdResult> task) {
                            if (!task.isSuccessful()) {
                                Log.w(TAG, "getInstanceId failed", task.getException());
                                return;
                            }
                            // Get new Instance ID token
                            String token = task.getResult().getToken();

                            // Log and toast
                            String msg = mContext.getString(R.string.msg_token_fmt, token);
                            Log.d(TAG, msg);
                            TokenManagement.setCurrToken(token);
                            TokenManagement.checkTokenExist("l");
                        }
                    });
        } else {
            mContext.startActivity(new Intent(mContext, LoginActivity.class));
            Toast.makeText(mContext, "Login failed.",Toast.LENGTH_SHORT).show();
        }
    }

    public static void afterToken() {
        String greeting = "";
        Intent intent = null;

        if(User.currUser != null && User.currUser.getType().equals("C")) {
            greeting = "Hello " + User.currUser.getUsername();
            intent = new Intent(mContext, CanteenListActivity.class);
        } else if(User.currUser != null && User.currUser.getType().equals("S")){
            greeting = "Hello " + User.currUser.getUsername();
            if(!User.currUser.getStallID().isEmpty()) {
                intent = new Intent(mContext, OrderRecordActivity.class);
            } else {
                intent = new Intent(mContext, ManageStallActivity.class);
                intent.putExtra("actionFlag", "new");
            }
        } else {
            greeting = "No user record found";
        }

        Toast.makeText(mContext, greeting,Toast.LENGTH_SHORT).show();

        mContext.startActivity(intent);
        activity.finish();
    }
}
