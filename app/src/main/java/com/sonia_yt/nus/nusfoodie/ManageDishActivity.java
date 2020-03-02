package com.sonia_yt.nus.nusfoodie;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
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
import com.sonia_yt.nus.nusfoodie.model.Dish;
import com.sonia_yt.nus.nusfoodie.model.DishType;
import com.sonia_yt.nus.nusfoodie.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ManageDishActivity extends AppCompatActivity {

    private StorageReference mStorageRef;

    private String actionFlag;
    private TextView titleTv, imgUrlTv, typeTv;
    private ImageView dishIv, imgPreview;
    private TextInputEditText nameTv, priceTv, caloriesTv, desTv;
    private Spinner typeSpn;
    private ImageButton imgBtn;
    private Button saveBtn;
    private ArrayList<DishType> types;
    private ArrayList<String> typeNames;
    private String dishType, name, price, calories, des, imgAddress, title;
    private Dish dish;
    private String imgUrl;
    private Uri filePath;
    private ImageButton navProfileBtn, navCartBtn, navCanteenListBtn, navOrderBtn;
    private android.widget.ImageView load_gif;
    private Toolbar toolbar;

    private AlertDialog.Builder builder;
    private final int PICK_IMAGE_REQUEST = 111;
    private boolean photoChanged = false;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseStorage storage = FirebaseStorage.getInstance();
    private final StorageReference storageRef = storage.getReferenceFromUrl("gs://nusfoodie-57713.appspot.com/dish/");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        setContentView(R.layout.activity_manage_dish);

        actionFlag = getIntent().getStringExtra("actionFlag");
        this.builder = new AlertDialog.Builder(this);
        types = new ArrayList<>();
        typeNames = new ArrayList<>();
        imgUrl = "";
        title = "";

        navCanteenListBtn = (ImageButton) findViewById(R.id.navCanteenListBtn);
        navProfileBtn = (ImageButton) findViewById(R.id.navProfileBtn);
        navCartBtn = (ImageButton) findViewById(R.id.navCartBtn);
        navOrderBtn = (ImageButton) findViewById(R.id.navOrderBtn);
        typeTv = (TextView) findViewById(R.id.typeTv);

        titleTv = (TextView) findViewById(R.id.titleTv);
        imgUrlTv = (TextView) findViewById(R.id.imgUrlTv);
        nameTv = (TextInputEditText) findViewById(R.id.nameTv);
        priceTv = (TextInputEditText) findViewById(R.id.priceTv);
        imgPreview = (ImageView) findViewById(R.id.imgPreview);
        caloriesTv = (TextInputEditText) findViewById(R.id.calorieTv);
        desTv = (TextInputEditText) findViewById(R.id.desTv);
        typeSpn = (Spinner) findViewById(R.id.typeSpn);
        imgBtn = (ImageButton) findViewById(R.id.imgBtn);
        dishIv = (ImageView) findViewById(R.id.dishIv);
        toolbar = findViewById(R.id.toolbar);

        load_gif = (android.widget.ImageView) findViewById(R.id.load_gif);

        Glide.with(getApplicationContext())
                .asGif()
                .load(R.drawable.load_gif)
                .into(load_gif);

        saveBtn = (Button) findViewById(R.id.saveBtn);

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

        if(actionFlag.equals("update")) {
            dish = new Dish();
            Intent intent = getIntent();
            dish.setId(intent.getStringExtra("id"));
            dish.setCanteenID(intent.getStringExtra("canteenID"));
            dish.setStallID(intent.getStringExtra("stallID"));
            dish.setTypeName(intent.getStringExtra("type"));
            dish.setTypeID(intent.getStringExtra("typeID"));
            dish.setName(intent.getStringExtra("name"));
            dish.setCalorie(intent.getStringExtra("calorie"));
            dish.setDescription(intent.getStringExtra("description"));
            dish.setPrice(intent.getStringExtra("price"));
            dish.setPictureAddress(intent.getStringExtra("pictureAddress"));

            titleTv.setText(dish.getName());
            title = dish.getName();
        } else {
            title = "Add New Dish";
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

        initTypes();
        imgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_PICK);
                startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
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

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validation()) {
                    String msg = "";
                    if(actionFlag.equals("update")) {
                        msg = "Do you confirm to update the dish?";
                    } else {
                        msg = "Do you confirm to add the dish?";
                    }
                    builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            if(actionFlag.equals("new")) {
                                load_gif.setVisibility(View.VISIBLE);

                                imgUrl = "d_" + User.currUser.getCanteenID() + "_" + User.currUser.getStallID() + "_" + nameTv.getText().toString().toLowerCase() + "_photo.jpg";
                                Map<String, Object> dishMap = new HashMap<>();
                                dishMap.put("name",nameTv.getText().toString());
                                dishMap.put("canteenID", User.currUser.getCanteenID());
                                dishMap.put("stallID", User.currUser.getStallID());
                                dishMap.put("price", priceTv.getText().toString());
                                dishMap.put("description", desTv.getText().toString());
                                dishMap.put("pictureAddress", imgUrl);
                                dishMap.put("calorie", caloriesTv.getText().toString());
                                dishMap.put("typeID", types.get(typeSpn.getSelectedItemPosition()).getId());
                                db.collection("canteen")
                                        .document(User.currUser.getCanteenID())
                                        .collection("stalls")
                                        .document(User.currUser.getStallID())
                                        .collection("dish")
                                        .document()
                                        .set(dishMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                uploadPhoto();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        load_gif.setVisibility(View.GONE);
                                    }
                                });
                            } else if(actionFlag.equals("update")) {
                                load_gif.setVisibility(View.VISIBLE);

                                imgUrl = "d_" + dish.getCanteenID() + "_" + dish.getStallID() + "_" + dish.getName().toLowerCase() + "_photo.jpg";
                                Map<String, Object> dishMap = new HashMap<>();
                                dishMap.put("name",nameTv.getText().toString());
                                dishMap.put("canteenID", dish.getCanteenID());
                                dishMap.put("stallID", dish.getStallID());
                                dishMap.put("price", priceTv.getText().toString());
                                dishMap.put("description", desTv.getText().toString());
                                dishMap.put("pictureAddress", imgUrl);
                                dishMap.put("calorie", caloriesTv.getText().toString());
                                dishMap.put("typeID", types.get(typeSpn.getSelectedItemPosition()).getId());
                                db.collection("canteen")
                                        .document(dish.getCanteenID())
                                        .collection("stalls")
                                        .document(dish.getStallID())
                                        .collection("dish")
                                        .document(dish.getId())
                                        .update(dishMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                if(photoChanged){
                                                    uploadPhoto();
                                                } else {
                                                    Intent intent = new Intent(getApplicationContext(), DishListActivity.class);
                                                    intent.putExtra("stallID", User.currUser.getStallID());
                                                    intent.putExtra("canteenID", User.currUser.getCanteenID());
                                                    startActivity(intent);
                                                    Toast.makeText(getApplicationContext(), "Dish " + nameTv.getText().toString() + " saved.", Toast.LENGTH_SHORT ).show();
                                                    finish();
                                                }
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        load_gif.setVisibility(View.GONE);
                                        String msg = e.getMessage();
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

        navBarInt();

    }

    private void initTypes() {
        db.collection("dishType")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            typeNames.add("");
                            types.add(new DishType("temp", "temp"));
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String id = document.getId();
                                String name = document.get("name").toString();
                                types.add(new DishType(id, name));
                                typeNames.add(name);
                            }
                            initSpn();
                        }
                    }
                });
    }

    private void initSpn() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.row_spn_dropdown, typeNames);
        typeSpn.setAdapter(adapter);

        typeSpn.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(Spinner parent, View view, int position, long id) {
                if(position != 0) {
                    dishType = typeSpn.getSelectedItem().toString();
                    typeTv.setTextColor(Color.parseColor("#757575"));
                }

            }
        });
        if(actionFlag.equals("update")) {
            updateUI();
        } else {
            load_gif.setVisibility(View.GONE);
        }
    }

    private void updateUI() {

        nameTv.setText(dish.getName());
        desTv.setText(dish.getDescription());
        caloriesTv.setText(dish.getCalorie());
        priceTv.setText(dish.getPrice());
        imgUrlTv.setText(dish.getPictureAddress());
        imgUrl = dish.getPictureAddress();
        mStorageRef = FirebaseStorage.getInstance().getReference("dish");
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

                Glide.with(getApplicationContext())
                        .load(downloadUrl)
                        .dontAnimate()
                        .into(imgPreview);
            }
        });
        int index = typeNames.indexOf(dish.getTypeName());
        typeSpn.setSelection(index);
        load_gif.setVisibility(View.GONE);
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

                //Setting image to ImageView
                dishIv.setImageBitmap(bitmap);
                imgUrlTv.setText(filePath.toString());
                imgUrlTv.setTextColor(Color.parseColor("#757575"));
                imgPreview.setImageBitmap(bitmap);
                photoChanged = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private boolean validation() {
        boolean res = true;
        if(nameTv.getText().toString().isEmpty()) {
            nameTv.setError("Please enter dish's name.");
            nameTv.requestFocus();
            return false;
        }
        if(priceTv.getText().toString().isEmpty()) {
            priceTv.setError("Please enter dish's price.");
            priceTv.requestFocus();
            return false;
        }
        if(caloriesTv.getText().toString().isEmpty()) {
            caloriesTv.setError("Please enter the quantity of calories.");
            caloriesTv.requestFocus();
            return false;
        }
        if(desTv.getText().toString().isEmpty()) {
            desTv.setError("Please enter dish's description.");
            desTv.requestFocus();
            return false;
        }
        if(typeSpn.getSelectedItemPosition() == 0) {
            typeSpn.setFocusable(true);
            typeSpn.setFocusableInTouchMode(true);
            typeSpn.requestFocus();
            typeTv.setTextColor(Color.RED);
            Toast.makeText(this, "Please choose dish type." , Toast.LENGTH_LONG).show();
            return false;
        }
        if(actionFlag.equals("new") && !photoChanged){
            imgUrlTv.setText("please upload dish's picture.");
            imgUrlTv.setTextColor(Color.RED);
            return false;
        }
        return res;
    }

    private void uploadPhoto() {
        if(filePath != null && photoChanged) {

            StorageReference childRef = storageRef.child(imgUrl);

            //uploading the image
            UploadTask uploadTask = childRef.putFile(filePath);

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Intent intent = new Intent(getApplicationContext(), DishListActivity.class);
                    intent.putExtra("stallID", User.currUser.getStallID());
                    intent.putExtra("canteenID", User.currUser.getCanteenID());
                    startActivity(intent);
                    Toast.makeText(getApplicationContext(), "Dish " + nameTv.getText().toString() + " saved.", Toast.LENGTH_SHORT ).show();
                    finish();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    load_gif.setVisibility(View.GONE);
                    Toast.makeText(ManageDishActivity.this, "Upload Failed -> " + e, Toast.LENGTH_SHORT).show();
                }
            });
        }
        else if(!photoChanged && actionFlag.equals("new")) {
            Intent intent = new Intent(getApplicationContext(), DishListActivity.class);
            intent.putExtra("stallID", User.currUser.getStallID());
            intent.putExtra("canteenID", User.currUser.getCanteenID());
            startActivity(intent);
            Toast.makeText(getApplicationContext(), "Dish " + nameTv.getText().toString() + " saved.", Toast.LENGTH_SHORT ).show();
            finish();
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
