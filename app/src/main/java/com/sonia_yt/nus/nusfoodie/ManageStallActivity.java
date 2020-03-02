package com.sonia_yt.nus.nusfoodie;

import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rey.material.widget.Button;
import com.rey.material.widget.ImageButton;
import com.rey.material.widget.ImageView;
import com.rey.material.widget.Spinner;
import com.rey.material.widget.TextView;
import com.sonia_yt.nus.nusfoodie.model.Canteen;
import com.sonia_yt.nus.nusfoodie.model.Stall;
import com.sonia_yt.nus.nusfoodie.model.StallType;
import com.sonia_yt.nus.nusfoodie.model.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ManageStallActivity extends AppCompatActivity {
    private StorageReference mStorageRef;

    private String actionFlag;
    private TextView titleTv, imgUrlTv, openTv, closeTv;
    private ImageView stallIv, imgPreview;
    private TextInputEditText nameTv, desTv, prepareTimeTv;
    private Spinner typeSpn, canteenSpn;
    private ImageButton imgBtn, openPickerBtn, closeTimePickerBtn;
    private Button updateBtn;
    private ImageButton navProfileBtn, navCartBtn, navCanteenListBtn, navOrderBtn;
    private ArrayList<StallType> types;
    private ArrayList<String> typeIDs, typeNames,canteenIDs, canteenNames;
    private ArrayList<Canteen> canteens;
    private Stall stall;
    private String imgUrl, stallType;
    private Uri filePath;
    private android.widget.TextView canteenlbl, typelbl;
    private TimePickerDialog.OnTimeSetListener openTimeSetListener, closeTimeSetListener;
    private boolean photoChanged = false;
    private android.widget.ImageView load_gif;
    private Toolbar toolbar;

    private AlertDialog.Builder builder;
    private final int PICK_IMAGE_REQUEST = 111;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseStorage storage = FirebaseStorage.getInstance();
    private final StorageReference storageRef = storage.getReferenceFromUrl("gs://nusfoodie-57713.appspot.com/canteen/");

    private Date canteenClose,canteenOpen;
    private final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        setContentView(R.layout.activity_manage_stall);

        actionFlag = getIntent().getStringExtra("actionFlag");

        if(!actionFlag.equals("new") && User.currUser.getMyStall() == null) {
            Intent intent = new Intent(ManageStallActivity.this, ManageStallActivity.class);
            intent.putExtra("actionFlag", "new");
            Toast.makeText(getApplicationContext(), "Please complete your stall information first.", Toast.LENGTH_LONG).show();
            startActivity(intent);
            finish();
            return;
        }


        this.builder = new AlertDialog.Builder(this);
        types = new ArrayList<>();
        typeIDs = new ArrayList<>();
        canteenIDs = new ArrayList<>();
        typeNames = new ArrayList<>();
        canteenNames = new ArrayList<>();
        canteenNames.add(" ");
        canteens = new ArrayList<>();
        imgUrl = "";
        toolbar = findViewById(R.id.toolbar);

        stall = User.currUser.getMyStall();

        titleTv = (TextView) findViewById(R.id.titleTv);
        imgUrlTv = (TextView) findViewById(R.id.imgUrlTv);
        nameTv = (TextInputEditText) findViewById(R.id.nameTv);
        stallIv = (ImageView) findViewById(R.id.stallIv);
        openTv = (TextView) findViewById(R.id.openTimeTv);
        closeTv = (TextView) findViewById(R.id.closeTimeTv);
        prepareTimeTv = (TextInputEditText) findViewById(R.id.prepareTimeTv);
        desTv = (TextInputEditText) findViewById(R.id.desTv);
        typeSpn = (Spinner) findViewById(R.id.typeSpn);
        imgPreview = (ImageView) findViewById(R.id.imgPreview);
        imgBtn = (ImageButton) findViewById(R.id.imgBtn);
        openPickerBtn = (ImageButton) findViewById(R.id.openPickerBtn);
        closeTimePickerBtn = (ImageButton) findViewById(R.id.closeTimePickerBtn);
        updateBtn = (Button) findViewById(R.id.updateBtn);
        canteenSpn = (Spinner) findViewById(R.id.canteenSpn);
        canteenlbl = (android.widget.TextView) findViewById(R.id.canteenlbl);
        typelbl = (android.widget.TextView) findViewById(R.id.typelbl);
        if(!actionFlag.equals("new")) {
            canteenSpn.setEnabled(false);
            canteenSpn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getApplicationContext(), "uneditable", Toast.LENGTH_SHORT).show();
                }
            });
        }

        openTv.clearFocus();
        navCanteenListBtn = (ImageButton) findViewById(R.id.navCanteenListBtn);
        navProfileBtn = (ImageButton) findViewById(R.id.navProfileBtn);
        navCartBtn = (ImageButton) findViewById(R.id.navCartBtn);
        navOrderBtn = (ImageButton) findViewById(R.id.navOrderBtn);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(User.currUser.getType().equals("S")) {
                    if(!OrderRecordActivity.order_running) {
                        startActivity(new Intent(getApplicationContext(), OrderRecordActivity.class));
                    }
                }
                finish();
            }
        });

        load_gif = (android.widget.ImageView) findViewById(R.id.load_gif);
        Glide.with(getApplicationContext())
                .asGif()
                .load(R.drawable.load_gif)
                .into(load_gif);

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
                    if(actionFlag.equals("view"))
                        toolbar.setTitle(stall.getName());
                    else
                        toolbar.setTitle("Stall Name");
                    collapsingToolbarLayout.setContentScrimColor(Color.parseColor("#37000000"));
                    isShow = true;
                } else if(isShow) {
                    toolbar.setTitle("");
                    isShow = false;
                }
            }
        });


        navBarInt();

        initTypes();

    }

    private void updateUI() {

        if(!actionFlag.equals("new")) {
            titleTv.setText(stall.getName());
            nameTv.setText(stall.getName());
            imgUrlTv.setText(stall.getPictureAddress());
            openTv.setText(stall.getOpen());
            closeTv.setText(stall.getClose());
            desTv.setText(stall.getDescription());
            prepareTimeTv.setText(stall.getPrepareTime());

            mStorageRef = FirebaseStorage.getInstance().getReference("canteen");
            StorageReference path = mStorageRef.child(stall.getPictureAddress());
            path.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri downloadUrl) {
                    Glide.with(getApplicationContext())
                            .load(downloadUrl)
                            .dontAnimate()
                            .into(stallIv);
                    if(actionFlag.equals("update"))
                        Glide.with(getApplicationContext())
                                .load(downloadUrl)
                                .dontAnimate()
                                .into(imgPreview);
                }
            });
            int index = typeIDs.indexOf(stall.getTypeID());
            int canteenInt = canteenIDs.indexOf(stall.getCanteenID()) + 1;
            typeSpn.setSelection(index);
            canteenSpn.setSelection(canteenInt);

        }

        if(actionFlag.equals("view")) {
            typeSpn.setEnabled(false);
            nameTv.setEnabled(false);
            desTv.setEnabled(false);
            openPickerBtn.setEnabled(false);
            closeTimePickerBtn.setEnabled(false);
            prepareTimeTv.setEnabled(false);
            updateBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent intent = new Intent(getApplicationContext(), ManageStallActivity.class);
                            intent.putExtra("actionFlag", "update");
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
                    dialog.setMessage("Do you want to update the stall information?");
                    dialog.show();

                }
            });
        } else {
            typeSpn.setClickable(true);
            imgBtn.setVisibility(View.VISIBLE);
            imgUrlTv.setVisibility(View.VISIBLE);
            imgPreview.setVisibility(View.VISIBLE);
            if(actionFlag.equals("update"))
                updateBtn.setText("SAVE CHANGE");
            else
                updateBtn.setText("SAVE STALL INFORMATION");

            typeSpn.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
                @Override
                public void onItemSelected(Spinner parent, View view, int position, long id) {
                    if(position != 0) {
                        stallType = typeSpn.getSelectedItem().toString();
                        typelbl.setTextColor(Color.parseColor("#757575"));
                    }
                }
            });

            canteenSpn.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
                @Override
                public void onItemSelected(Spinner parent, View view, int position, long id) {
                    if(position != 0) {
                        User.currUser.setCanteenID(canteens.get(position - 1).getCanteenID());
                        canteenlbl.setTextColor(Color.parseColor("#757575"));
                    }
                }
            });

            openTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    String date = hourOfDay + ":" + minute;
                    openTv.setText(date);
                    openTv.setTextColor(Color.parseColor("#757575"));
                }
            };

            closeTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    String date = hourOfDay + ":" + minute;
                    closeTv.setText(date);
                    openTv.setTextColor(Color.parseColor("#757575"));
                }
            };

            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(actionFlag.equals("update")) {
                        Intent intent = new Intent(getApplicationContext(), ManageStallActivity.class);
                        intent.putExtra("actionFlag", "view");
                        startActivity(intent);
                        finish();
                    } else if (actionFlag.equals("new")) {
                        builder.setPositiveButton("Next time", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
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
                        dialog.setMessage("Upload the stall information next time? (you need to fill in the stall information first. )");
                        dialog.show();
                    }
                }
            });

            openTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getTimePicker("Open");
                    openTv.setError(null);
                }
            });

            closeTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getTimePicker("Close");
                    closeTv.setError(null);
                }
            });

            openPickerBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getTimePicker("Open");
                }
            });

            closeTimePickerBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getTimePicker("Close");
                }
            });

            imgUrlTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_PICK);
                    startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
                }
            });

            imgBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_PICK);
                    startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
                }
            });

            updateBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(validation()) {
                        String msg = "Do you confirm to save it?";
                        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if(actionFlag.equals("new")) {
                                    load_gif.setVisibility(View.VISIBLE);
                                    updateBtn.setEnabled(false);

                                    imgUrl = "s_" + User.currUser.getCanteenID() + "_" + nameTv.getText().toString().toLowerCase() + "_photo.jpg";
                                    Map<String, Object> stallMap = new HashMap<>();
                                    stallMap.put("name",nameTv.getText().toString());
                                    stallMap.put("canteenID", User.currUser.getCanteenID());
                                    stallMap.put("open", openTv.getText().toString());
                                    stallMap.put("close", closeTv.getText().toString());
                                    stallMap.put("description", desTv.getText().toString());
                                    stallMap.put("pictureAddress", imgUrl);
                                    stallMap.put("userID", User.currUser.getEmail());
                                    stallMap.put("orderIDs", "");
                                    stallMap.put("prepareTime", prepareTimeTv.getText().toString());
                                    stallMap.put("typeID", types.get(typeSpn.getSelectedItemPosition()).getId());
                                    db.collection("canteen")
                                            .document(User.currUser.getCanteenID())
                                            .collection("stalls")
                                            .document("s_"+User.currUser.getCanteenID()+"_"+User.currUser.getEmail())
                                            .set(stallMap)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Stall myStall = new Stall();
                                                    myStall.setId("s_"+User.currUser.getCanteenID()+"_"+User.currUser.getEmail());
                                                    myStall.setCanteenID(User.currUser.getCanteenID());
                                                    myStall.setUserID(User.currUser.getEmail());
                                                    myStall.setName(nameTv.getText().toString());
                                                    myStall.setOpen(openTv.getText().toString());
                                                    myStall.setClose(closeTv.getText().toString());
                                                    myStall.setDescription(desTv.getText().toString());
                                                    myStall.setTypeID(types.get(typeSpn.getSelectedItemPosition()).getId());
                                                    myStall.setTypeName(types.get(typeSpn.getSelectedItemPosition()).getName());
                                                    myStall.setPrepareTime(prepareTimeTv.getText().toString());
                                                    User.currUser.setMyStall(myStall);
                                                    User.currUser.setStallID(myStall.getId());
                                                    uploadPhoto();
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            load_gif.setVisibility(View.GONE);
                                            updateBtn.setEnabled(true);
                                        }
                                    });
                                } else if(actionFlag.equals("update")) {
                                    load_gif.setVisibility(View.VISIBLE);
                                    updateBtn.setEnabled(false);

                                    imgUrl = "s_" + stall.getCanteenID() + "_" + stall.getName().toLowerCase() + "_photo.jpg";
                                    Map<String, Object> stallMap = new HashMap<>();
                                    stallMap.put("name",nameTv.getText().toString());
                                    stallMap.put("open", openTv.getText().toString());
                                    stallMap.put("close", closeTv.getText().toString());
                                    stallMap.put("description", desTv.getText().toString());
                                    stallMap.put("pictureAddress", imgUrl);
                                    stallMap.put("typeID", types.get(typeSpn.getSelectedItemPosition()).getId());
                                    stallMap.put("prepareTime", prepareTimeTv.getText().toString());
                                    db.collection("canteen")
                                            .document(stall.getCanteenID())
                                            .collection("stalls")
                                            .document(User.currUser.getStallID())
                                            .update(stallMap)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    User.currUser.getMyStall().setName(nameTv.getText().toString());
                                                    User.currUser.getMyStall().setOpen(openTv.getText().toString());
                                                    User.currUser.getMyStall().setClose(closeTv.getText().toString());
                                                    User.currUser.getMyStall().setDescription(desTv.getText().toString());
                                                    User.currUser.getMyStall().setTypeID(types.get(typeSpn.getSelectedItemPosition()).getId());
                                                    User.currUser.getMyStall().setTypeName(types.get(typeSpn.getSelectedItemPosition()).getName());
                                                    User.currUser.getMyStall().setPrepareTime(prepareTimeTv.getText().toString());
                                                    if(photoChanged) {
                                                        uploadPhoto();
                                                    } else {
                                                        User.currUser.getMyStall().setPictureAddress(imgUrl);
                                                        Intent intent = new Intent(getApplicationContext(), ManageStallActivity.class);
                                                        intent.putExtra("actionFlag", "view");
                                                        startActivity(intent);
                                                        Toast.makeText(getApplicationContext(), "Stall " + nameTv.getText().toString() + " saved.", Toast.LENGTH_SHORT ).show();
                                                        finish();
                                                    }
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            String msg = e.getMessage();
                                            load_gif.setVisibility(View.GONE);
                                            updateBtn.setEnabled(true);
                                        }
                                    });
                                }
                            }
                        });
                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User cancelled the dialog
                            }
                        });

                        // Create the AlertDialog
                        AlertDialog dialog = builder.create();
                        dialog.setMessage(msg);
                        dialog.show();
                    }
                }
            });
        }
        load_gif.setVisibility(View.GONE);
        updateBtn.setEnabled(true);
    }

    private void initTypes() {
        db.collection("stallType")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            typeNames.add("");
                            typeIDs.add("");
                            types.add(new StallType("temp", "temp"));
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String id = document.getId();
                                String name = document.get("name").toString();
                                types.add(new StallType(id, name));
                                typeIDs.add(id);
                                typeNames.add(name);
                            }
                            initSpn(typeSpn, typeNames);
                            initCanteens();
                        }
                    }
                });
    }

    private void initCanteens() {
        db.collection("canteen")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                canteenNames.add(document.get("name").toString());
                                String test = document.get("close").toString();
                                canteens.add(new Canteen(document.getId(), document.get("name").toString(),
                                        document.get("open").toString(), document.get("close").toString()));
                                canteenIDs.add(document.getId());
                            }
                            initSpn(canteenSpn, canteenNames);
                            updateUI();
                        }
                    }
                });
    }

    private void initSpn(Spinner spn, ArrayList<String> entries) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.row_spn_dropdown, entries);
        spn.setAdapter(adapter);
    }

    private void getTimePicker(String flag) {
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int min = cal.get(Calendar.MINUTE);
        MyTimePickerDialog dialog;
        if(flag.equals("Open"))     {
           dialog  = new MyTimePickerDialog(ManageStallActivity.this,
                    openTimeSetListener,
                    hour,min,true);
        } else {
            dialog  = new MyTimePickerDialog(ManageStallActivity.this,
                    closeTimeSetListener,
                    hour,min,true);
        }
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setTitle(flag + " at");
        dialog.show();
    }

    private boolean validation() {
        boolean res = true;
        try{
            if(canteenSpn.getSelectedItemPosition() == 0)  {
                canteenSpn.setFocusable(true);
                canteenSpn.setFocusableInTouchMode(true);
                canteenSpn.requestFocus();
                canteenlbl.setTextColor(Color.RED);
                Toast.makeText(this, "Please choose canteen." , Toast.LENGTH_LONG).show();
                return false;
            }

            Canteen temp = canteens.get(canteenSpn.getSelectedItemPosition() - 1);
            Date open_temp = sdf.parse(temp.getOpen());
            Date close_temp = sdf.parse(temp.getClose());

            if(nameTv.getText().toString().isEmpty()) {
                nameTv.setError("Please enter dish's name.");
                nameTv.requestFocus();
                return false;
            }

            if(prepareTimeTv.getText().toString().isEmpty()) {
                prepareTimeTv.setError("Please enter your estimated prepare time.");
                prepareTimeTv.requestFocus();
                return false;
            }

            if(openTv.getText().toString().isEmpty()) {
               openTv.setFocusable(true);
               openTv.setFocusableInTouchMode(true);
               openTv.setError("Please choose your daily opening time.");
               openTv.requestFocus();
               return false;
            }

            Date open = sdf.parse(openTv.getText().toString());

            if(differenceBetween(open, open_temp) < 0) {
                openTv.setFocusable(true);
                openTv.setFocusableInTouchMode(true);
                openTv.setError("Invalid opening time.");
                openTv.requestFocus();
                Toast.makeText(this, "The stall cannot open earlier than the canteen. ("
                        + canteenSpn.getSelectedItem().toString() + " opens at: " + temp.getOpen() + " )", Toast.LENGTH_LONG).show();
                return false;
            }

            if(closeTv.getText().toString().isEmpty()) {
                closeTv.setFocusable(true);
                closeTv.setFocusableInTouchMode(true);
                closeTv.setError("Please choose your daily closing time.");
                closeTv.requestFocus();
                return false;
            }

            Date close = sdf.parse(closeTv.getText().toString());

            if(differenceBetween(close, close_temp) > 0) {
                closeTv.setFocusable(true);
                closeTv.setFocusableInTouchMode(true);
                closeTv.setError("Invalid closing time.");
                closeTv.requestFocus();
                Toast.makeText(this, "The stall cannot close later than the canteen. ("
                        + canteenSpn.getSelectedItem().toString() + " closes at: " + temp.getClose() + " )", Toast.LENGTH_LONG).show();
                return false;
            }

            if(differenceBetween(open, close) > 0) {
                closeTv.setFocusable(true);
                closeTv.setFocusableInTouchMode(true);
                closeTv.setError("Invalid closing time.");
                closeTv.requestFocus();
                Toast.makeText(this, "The closing time cannot be earlier than opening time. ("
                        + canteenSpn.getSelectedItem().toString() + " closes at: " + temp.getClose() + " )", Toast.LENGTH_LONG).show();
                return false;
            }

            if(desTv.getText().toString().isEmpty()) {
                desTv.setError("Please enter stall's description.");
                desTv.requestFocus();
                return false;
            }

            if(typeSpn.getSelectedItemPosition() == 0) {
                typeSpn.setFocusable(true);
                typeSpn.setFocusableInTouchMode(true);
                typeSpn.requestFocus();
                typelbl.setTextColor(Color.RED);
                Toast.makeText(this, "Please choose stall type." , Toast.LENGTH_LONG).show();
                return false;
            }

            if(!photoChanged && actionFlag.equals("new")){
                imgUrlTv.setText("please upload stall's picture.");
                imgUrlTv.setTextColor(Color.RED);
                return false;
            }

        } catch (Exception e) {
            return false;
        }
        return res;
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

        if(User.currUser.getType().equals("S")) {
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
            navCanteenListBtn.setImageResource(R.drawable.home_icon_w);
            navCanteenListBtn.setBackgroundColor(Color.parseColor("#FF8A65"));
            navCanteenListBtn.setClickable(false);
        }
    }

    private void uploadPhoto() {
        if(filePath != null && photoChanged) {

            StorageReference childRef = storageRef.child(imgUrl);

            //uploading the image
            UploadTask uploadTask = childRef.putFile(filePath);

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    User.currUser.getMyStall().setPictureAddress(imgUrl);
                    Map<String, Object> user = new HashMap<>();
                    user.put("canteenID", User.currUser.getCanteenID());
                    user.put("stallID", User.currUser.getStallID());
                    db.collection("users")
                            .document(User.currUser.getEmail())
                            .update(user)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()) {
                                        Intent intent = new Intent(getApplicationContext(), ManageStallActivity.class);
                                        intent.putExtra("actionFlag", "view");
                                        startActivity(intent);
                                        Toast.makeText(getApplicationContext(), "Stall " + nameTv.getText().toString() + " saved.", Toast.LENGTH_SHORT ).show();
                                        finish();
                                    }
                                }
                            });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    load_gif.setVisibility(View.GONE);
                    updateBtn.setEnabled(true);
                    Toast.makeText(ManageStallActivity.this, "Upload Failed -> " + e, Toast.LENGTH_SHORT).show();
                }
            });
        }
        else if (!photoChanged) {
            Intent intent = new Intent(getApplicationContext(), ManageStallActivity.class);
            intent.putExtra("actionFlag", "view");
            startActivity(intent);
            Toast.makeText(getApplicationContext(), "Stall " + nameTv.getText().toString() + " saved.", Toast.LENGTH_SHORT ).show();
            finish();
        } else if (filePath == null){
            load_gif.setVisibility(View.GONE);
            updateBtn.setEnabled(true);
            Toast.makeText(ManageStallActivity.this, "please select the dish photo.", Toast.LENGTH_SHORT).show();
        }
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null)
        {
            filePath = data.getData();
            try {
                //getting image from gallery
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);

                //Setting image to ImageView and change imgUpdated flag
                stallIv.setImageBitmap(bitmap);
                imgPreview.setImageBitmap(bitmap);
                imgUrlTv.setText(filePath.toString());
                imgUrlTv.setTextColor(Color.parseColor("#757575"));
                photoChanged = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if(User.currUser.getType().equals("S")) {
            if(!OrderRecordActivity.order_running) {
                startActivity(new Intent(getApplicationContext(), OrderRecordActivity.class));
            }
        }
        finish();
    }
}
