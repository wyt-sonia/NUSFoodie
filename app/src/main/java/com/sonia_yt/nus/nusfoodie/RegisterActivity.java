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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.rey.material.widget.EditText;
import com.rey.material.widget.ImageButton;
import com.rey.material.widget.LinearLayout;
import com.rey.material.widget.RadioButton;
import com.rey.material.widget.Spinner;
import com.sonia_yt.nus.nusfoodie.model.User;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    private EditText passwordEt, confirmPasswordEt, usernameEt, emailEt, telEt, personalIDEt, ageEt;
    private Spinner typeSpn;
    private LinearLayout genderGroup;
    private RadioButton genderRbF, genderRbM;
    private Button registerBtn;
    private FirebaseAuth mAuth;
    private ImageButton navBackBtn;
    final String TAG = "RegisterActivity";
    final String[] typeSpnEntries = {" ", "Customer", "Vendor"};

    private ImageView load_gif;
    static Context mContext;
    static Activity activity;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        setContentView(R.layout.activity_register);

        mContext = this;
        activity = this;

        mAuth = FirebaseAuth.getInstance();

        typeSpn = (Spinner) findViewById(R.id.typeSpn);
        usernameEt = (EditText) findViewById(R.id.usernameEt);
        emailEt = (EditText) findViewById(R.id.emailEt);
        telEt = (EditText) findViewById(R.id.telEt);
        passwordEt = (EditText) findViewById(R.id.passwordEt);
        confirmPasswordEt = (EditText) findViewById(R.id.cPasswordEt);
        personalIDEt = (EditText) findViewById(R.id.personalIDEt);
        genderGroup = (LinearLayout) findViewById(R.id.genderGroup);
        genderRbF = (RadioButton) findViewById(R.id.genderRbF);
        genderRbM = (RadioButton) findViewById(R.id.genderRbM);
        ageEt = (EditText) findViewById(R.id.ageEt);
        navBackBtn = (ImageButton) findViewById(R.id.navBackBtn);

        load_gif = (ImageView) findViewById(R.id.load_gif);
        Glide.with(getApplicationContext())
                .asGif()
                .load(R.drawable.load_gif)
                .into(load_gif);

        navBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //region EditText

        usernameEt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus)
                    usernameEt.setError(null);
            }
        });

        emailEt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus)
                    emailEt.setError(null);
            }
        });

        telEt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus)
                    telEt.setError(null);
            }
        });

        passwordEt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus)
                    passwordEt.setError(null);
            }
        });

        confirmPasswordEt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus)
                    confirmPasswordEt.setError(null);
            }
        });

        personalIDEt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus)
                    personalIDEt.setError(null);
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
        //endregion

        //region Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.row_spn_w, typeSpnEntries);
        typeSpn.setAdapter(adapter);

        typeSpn.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(Spinner parent, View view, int position, long id) {
                String userType = typeSpn.getSelectedItem().toString();
                updateUI(userType);
            }
        });
        //endregion

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

        registerBtn = (Button) findViewById(R.id.registerBtn);

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerNewUser();
            }
        });
    }

    //region Validation
    public boolean isValidEmail(CharSequence email) {
        if (email == null)
            return false;

        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public boolean isValidTel(CharSequence tel) {
        if (tel == null)
            return false;

        return tel.length() == 8;
    }

    public boolean isValidPersonalID(CharSequence personalID) {
        if (personalID == null)
            return false;

        String regex = typeSpn.getSelectedItem().toString().equals("Customer") ? "^[A-Z]\\d{7}[A-Z]$" : "^[STFG]\\d{7}[A-Z]$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(personalID);
        if (!matcher.find()) {
            return false;
        }
        return true;
    }

    public boolean isValidAge(CharSequence age) {
        if (age == null || age.charAt(0) == '0')
            return false;
        if (Integer.parseInt(age.toString()) > 100 || Integer.parseInt(age.toString()) < 0)
            return false;

        return true;
    }
    //endregion

    private void updateUI(String userType) {

        int visibility = View.GONE;
        String personalIDType = "";
        if(!userType.equals(" ")){
            visibility = View.VISIBLE;
            if(userType.equals("Customer")) {
                personalIDType = "Matriculation Number";
                personalIDEt.setHelper("Enter matriculation number");
            } else {
                personalIDType = "NRIC";
                personalIDEt.setHelper("Enter NRIC");
            }
        }
        usernameEt.setVisibility(visibility);
        emailEt.setVisibility(visibility);
        telEt.setVisibility(visibility);
        passwordEt.setVisibility(visibility);
        confirmPasswordEt.setVisibility(visibility);
        personalIDEt.setHint(personalIDType);
        personalIDEt.setVisibility(visibility);
        genderGroup.setVisibility(visibility);
        ageEt.setVisibility(visibility);
        registerBtn.setVisibility(visibility);

    }

    private void registerNewUser() {
        //get the actual String or text that the user type
        String type, username, email, tel, password, confirmPassword, personalID, age;
        type = typeSpn.getSelectedItem().toString().equals("Customer") ? "C" : "S" ;
        username = usernameEt.getText().toString();
        email = emailEt.getText().toString();
        tel = telEt.getText().toString();
        password = passwordEt.getText().toString();
        confirmPassword = confirmPasswordEt.getText().toString();
        personalID = personalIDEt.getText().toString();
        age = ageEt.getText().toString();

        //region ValidationUIEvent
        if (TextUtils.isEmpty(type)) {
            Toast.makeText(this, "Please choose user type", Toast.LENGTH_LONG).show();
            return;
        }

        if (TextUtils.isEmpty(username)) {
            usernameEt.setError("Please enter username");
            return;
        } else if(username.length() > 20) {
            usernameEt.setError("The maximum length of username is 20.");
            return;
        }

        if (TextUtils.isEmpty(email)) {
            emailEt.setError("please enter email");
            return;
        } else if(!isValidEmail(emailEt.getText().toString())) {
            emailEt.setError("the email address is invalid.");
            return;
        } else {
            emailEt.setError(null);
        }

        if (TextUtils.isEmpty(tel)) {
            telEt.setError( "please enter tel number");
            return;
        } else if(!isValidTel(telEt.getText().toString())) {
            telEt.setError("the tel number is invalid.");
            return;
        } else {
            telEt.setError(null);
        }

        if (TextUtils.isEmpty(password)) {
            passwordEt.setError("please enter both password and confirm password");
            return;
        } else if(passwordEt.getText().toString().length() < 6) {
            passwordEt.setError("please enter at least 6 digits.");
            return;
        } else if (!password.equals(confirmPassword)) {
            passwordEt.setError(null);
            confirmPasswordEt.setError("confirm password is different");
            return;
        } else {
            passwordEt.setError(null);
            confirmPasswordEt.setError(null);
        }

        String IDtype = typeSpn.getSelectedItem().toString().equals("Customer") ? "matriculation number" : "NRIC" ;
        if (TextUtils.isEmpty(personalID)) {
            personalIDEt.setError("please enter " + IDtype);
            return;
        } else if(!isValidPersonalID(personalIDEt.getText().toString())) {
            personalIDEt.setError("the " + IDtype + " is invalid.");
            return;
        } else {
            personalIDEt.setError(null);
        }

        if (TextUtils.isEmpty(age)) {
            ageEt.setError("please enter your age");
            return;
        } else if(!isValidAge(ageEt.getText().toString())) {
            ageEt.setError("the age is invalid.");
            return;
        } else {
            ageEt.setError(null);
        }

        if (!genderRbM.isChecked() && !genderRbF.isChecked()) {
            Toast.makeText(this, "please choose gender", Toast.LENGTH_LONG).show();
            return;
        }



        load_gif.setVisibility(View.VISIBLE);
        navBackBtn.setEnabled(false);
        typeSpn.setEnabled(false);
        usernameEt.setEnabled(false);
        emailEt.setEnabled(false);
        passwordEt.setEnabled(false);
        confirmPasswordEt.setEnabled(false);
        telEt.setEnabled(false);
        ageEt.setEnabled(false);
        personalIDEt.setEnabled(false);
        genderRbF.setEnabled(false);
        genderRbM.setEnabled(false);

        registerBtn.setEnabled(false);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: " + task.toString());
                            sendRegistrationLink();
                        } else {
                            load_gif.setVisibility(View.GONE);
                            registerBtn.setEnabled(true);
                            Log.d(TAG, "onComplete: " + task.toString());
                            Toast.makeText(getApplicationContext(), "Registration failed!" + task.getException().toString(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void sendRegistrationLink() {
        final FirebaseUser user = mAuth.getCurrentUser();
        user.sendEmailVerification()
                .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener() {

                    @Override
                    public void onComplete(@NonNull Task task) {

                        if (task.isSuccessful()) {
                            String type, username, email, tel, personalID, gender, age;
                            type = typeSpn.getSelectedItem().toString().equals("Customer") ? "C" : "S" ;
                            username = usernameEt.getText().toString();
                            email = emailEt.getText().toString();
                            tel = telEt.getText().toString();
                            gender = genderRbM.isChecked() ? "M" : "F" ;
                            personalID = personalIDEt.getText().toString();
                            age = ageEt.getText().toString();
                            User currUser = new User (type, email, tel, username, gender, age, personalID, "");
                            currUser.setWalletBalance(0);
                            if(type.equals("S")) {
                                currUser.setCanteenID("");
                                currUser.setStallID("");
                            }
                            addUser(currUser);
                            Toast.makeText(RegisterActivity.this,"Verification email sent to " + user.getEmail(),Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e(TAG, "sendEmailVerification", task.getException());
                            Toast.makeText(RegisterActivity.this,"Failed to send verification email.", Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }


    public void addUser(User currUser) {
        final String TAG = "RegisterActivity";

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Create a new user
        Map<String, Object> user = new HashMap<>();
        user.put("type", currUser.getType());
        user.put("email", currUser.getEmail().toLowerCase());
        user.put("tel", currUser.getTel());
        user.put("username", currUser.getUsername());
        user.put("gender", currUser.getGender());
        user.put("age", currUser.getAge());
        user.put("personalID", currUser.getPersonalID());
        user.put("walletBalance", currUser.getWalletBalance());
        user.put("orderRecord", "");
        if(currUser.getType().equals("S")) {
            user.put("canteenID", "");
            user.put("stallID", "");
        }

        // Add a new document
        db.collection("users")
                .document(currUser.getEmail().toLowerCase())
                .set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot added");
                        User.currUser = currUser;
                        afterRegister();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                load_gif.setVisibility(View.GONE);
                registerBtn.setEnabled(true);
                Log.w(TAG, "Error adding document", e);
            }
        });
    }

    private void afterRegister(){
        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
    }
}
