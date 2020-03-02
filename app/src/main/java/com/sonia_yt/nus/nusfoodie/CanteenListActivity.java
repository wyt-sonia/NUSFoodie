package com.sonia_yt.nus.nusfoodie;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.ViewPropertyAnimatorListener;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.rey.material.widget.ImageButton;
import com.rey.material.widget.ListView;
import com.sonia_yt.nus.nusfoodie.model.Canteen;
import com.sonia_yt.nus.nusfoodie.model.StallType;
import com.sonia_yt.nus.nusfoodie.model.User;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;

import butterknife.ButterKnife;
import iammert.com.view.scalinglib.ScalingLayout;
import iammert.com.view.scalinglib.ScalingLayoutListener;
import iammert.com.view.scalinglib.State;

public class CanteenListActivity extends AppCompatActivity{

    public static boolean canteen_running = false;

    private ListView canteenLv;
    private boolean flag;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private AutoCompleteTextView searchAct;
    private ArrayList<Canteen> canteenList = new ArrayList<>();
    private ArrayList<StallType> stallTypes = new ArrayList<>();
    private ArrayList<String> stallTypeIDs = new ArrayList<>();
    private final String TAG = "CanteenListActivity";
    private ImageButton navProfileBtn, navCartBtn, navCanteenListBtn, navOrderBtn, backBtn;
    private ImageView load_gif;
    private RelativeLayout searchLayout;
    private ScalingLayout scalingLayout;
    private Toolbar toolbar;

    //region LocationParameters
    // location last updated time
    private String mLastUpdateTime;
    private boolean refreshFlag;

    // location updates interval - 10sec
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;

    // fastest updates interval - 2 sec
    // location updates will be received if another app is requesting the locations
    // than your app can handle
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 2000;

    private static final int REQUEST_CHECK_SETTINGS = 100;


    // bunch of location related apis
    private FusedLocationProviderClient mFusedLocationClient;
    private SettingsClient mSettingsClient;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;
    private LocationCallback mLocationCallback;
    private Location mCurrentLocation;
    private double Lat, Long;

    // boolean flag to toggle the ui
    private Boolean mRequestingLocationUpdates;
    private SwipeRefreshLayout mPullToRefreshView;
    //endregion

    //region Distance

    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        setContentView(R.layout.activity_canteen_list);
        ButterKnife.bind(this);
        canteen_running = true;
        flag = true;
        refreshFlag = true;

        canteenLv = (ListView) findViewById(R.id.canteenLv);
        navCanteenListBtn = (ImageButton) findViewById(R.id.navCanteenListBtn);
        navProfileBtn = (ImageButton) findViewById(R.id.navProfileBtn);
        navCartBtn = (ImageButton) findViewById(R.id.navCartBtn);
        navOrderBtn = (ImageButton) findViewById(R.id.navOrderBtn);

        searchAct = (AutoCompleteTextView) findViewById(R.id.searchAct);
        backBtn = (ImageButton) findViewById(R.id.backBtn);
        searchLayout = findViewById(R.id.searchLayout);
        scalingLayout = findViewById(R.id.scalingLayout);
        toolbar = findViewById(R.id.toolbar);
        mPullToRefreshView = findViewById(R.id.pull_to_refresh);

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
                    toolbar.setTitle("Canteen List");
                    collapsingToolbarLayout.setContentScrimColor(Color.parseColor("#37000000"));
                    isShow = true;
                    backBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(searchAct.isFocused()) {
                                searchAct.clearFocus();
                                canteenList = new ArrayList<>(Canteen.getCanteenList().values());
                                if (!searchAct.getText().toString().isEmpty()) {
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
                                canteenList = new ArrayList<>(Canteen.getCanteenList().values());
                                if (!searchAct.getText().toString().isEmpty()) {
                                    searchAct.setText("");
                                    updateUI();
                                }
                            }
                        }
                    });
                }
            }
        });

        load_gif = (ImageView) findViewById(R.id.load_gif);
        Glide.with(getApplicationContext())
                .asGif()
                .load(R.drawable.load_gif)
                .into(load_gif);



        mPullToRefreshView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                flag = true;
                refreshFlag = false;
                canteenList = new ArrayList<>();
                startLocationUpdates();
            }
        });
        // initialize the necessary libraries
        init();

        // restore the values from saved instance state
        restoreValuesFromBundle(savedInstanceState);

        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        mRequestingLocationUpdates = true;
                        startLocationUpdates();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        if (response.isPermanentlyDenied()) {
                            // open device settings when the permission is
                            // denied permanently
                            openSettings();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();

        searchAct.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                canteenList = new ArrayList<Canteen>();
                for(Canteen c : Canteen.getCanteenList().values()) {
                    if(c.getName().toLowerCase().matches(".*"+s.toString().toLowerCase()+".*"))
                        canteenList.add(c);
                }
                updateUI();
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        canteen_running = true;
        // Resuming location updates depending on button state and
        // allowed permissions
        if (mRequestingLocationUpdates && checkPermissions()) {
            startLocationUpdates();
        }

        updateLatLong();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        canteen_running = false;
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (mRequestingLocationUpdates && checkPermissions()) {
            startLocationUpdates();
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        canteen_running = true;
        if (mRequestingLocationUpdates) {
            // pausing location updates
            stopLocationUpdates();
        }
    }

    @SuppressLint("RestrictedApi")
    private void init() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                // location is received
                mCurrentLocation = locationResult.getLastLocation();
                mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());

                updateLatLong();
            }
        };
        mRequestingLocationUpdates = false;

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    /**
     * Restoring values from saved instance state
     */
    private void restoreValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("is_requesting_updates")) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean("is_requesting_updates");
            }

            if (savedInstanceState.containsKey("last_known_location")) {
                mCurrentLocation = savedInstanceState.getParcelable("last_known_location");
            }

            if (savedInstanceState.containsKey("last_updated_on")) {
                mLastUpdateTime = savedInstanceState.getString("last_updated_on");
            }
        }

        updateLatLong();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("is_requesting_updates", mRequestingLocationUpdates);
        outState.putParcelable("last_known_location", mCurrentLocation);
        outState.putString("last_updated_on", mLastUpdateTime);

    }

    /**
     * Starting location updates
     * Check whether location settings are satisfied and then
     * location updates will be requested
     */
    private void startLocationUpdates() {
        mSettingsClient
                .checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        Log.i(TAG, "All location settings are satisfied.");

                        Toast.makeText(getApplicationContext(), "Started location updates!", Toast.LENGTH_SHORT).show();

                        //noinspection MissingPermission
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                                mLocationCallback, Looper.myLooper());

                        updateLatLong();
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                Log.i(TAG, "Location settings are not satisfied. Attempting to upgrade " +
                                        "location settings ");
                                try {
                                    // Show the dialog by calling startResolutionForResult(), and check the
                                    // result in onActivityResult().
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    rae.startResolutionForResult(CanteenListActivity.this, REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException sie) {
                                    Log.i(TAG, "PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be " +
                                        "fixed here. Fix in Settings.";
                                Log.e(TAG, errorMessage);

                                Toast.makeText(CanteenListActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }

                        updateLatLong();
                    }
                });
    }

    public void stopLocationUpdates() {
        // Removing location updates
        mFusedLocationClient
                .removeLocationUpdates(mLocationCallback)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(getApplicationContext(), "Location updates stopped!", Toast.LENGTH_SHORT).show();
                        //toggleButtons();
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.e(TAG, "User agreed to make required location settings changes.");
                        // Nothing to do. startLocationupdates() gets called in onResume again.
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.e(TAG, "User chose not to make required location settings changes.");
                        mRequestingLocationUpdates = false;
                        break;
                }
                break;
        }
    }

    private void openSettings() {
        Intent intent = new Intent();
        intent.setAction(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package",
                BuildConfig.APPLICATION_ID, null);
        intent.setData(uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void updateUI() {
        if(mCurrentLocation != null) {
            CanteenListAdapter adapter = new CanteenListAdapter(this, R.layout.canteen_row, canteenList);
            canteenList.sort(new Comparator<Canteen>() {
                @Override
                public int compare(Canteen o1, Canteen o2) {
                    double diff = o1.getDistance() - o2.getDistance();
                    if( diff > 0) {
                        return 1;
                    } else if (diff == 0) {
                        return 0;
                    } else {
                        return -1;
                    }
                }
            });
            canteenLv.setAdapter(adapter);
            navBarInt();
            // Get the string array
            ArrayList<String> canteenNames = new ArrayList<>();
            for(Canteen c : canteenList) {
                canteenNames.add(c.getName());
            }
            String[] canteenNamesArray = canteenNames.toArray(new String[canteenNames.size()]);
            // Create the adapter and set it to the AutoCompleteTextView
            ArrayAdapter<String> seachAdapter =
                    new ArrayAdapter<String>(this, R.layout.row_spn_dropdown, canteenNamesArray);
            searchAct.setAdapter(seachAdapter);
            if(!refreshFlag) {
                mPullToRefreshView.setRefreshing(refreshFlag);
                refreshFlag = !refreshFlag;
            }
            load_gif.setVisibility(View.GONE);
        } else {
            Toast.makeText(this,"Cannot find your location.", Toast.LENGTH_SHORT).show();
        }
    }

    private void navBarInt() {

        navOrderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), OrderRecordActivity.class));
            }
        });
        navProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                intent.putExtra("actionFlag", "view");
                startActivity(intent);
            }
        });

        if(User.currUser.getType().equals("C")) {
            navCartBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(getApplicationContext(), CartActivity.class));
                }
            });
            navCanteenListBtn.setImageResource(R.drawable.canteen_icon_w);
            navCanteenListBtn.setBackgroundColor(Color.parseColor("#FF8A65"));
            navCanteenListBtn.setClickable(false);
        }
    }

    private void updateLatLong() {
        String msg = "location = null";
        if(mCurrentLocation != null) {
            Lat = mCurrentLocation.getLatitude();
            Long = mCurrentLocation.getLongitude();
            msg = "Current Lat: " + Lat + ", Long: " + Long;
            if(flag) {
                getCanteenInfoFromDB();
                flag = false;
            }
        }
        //Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    private void getCanteenInfoFromDB(){
        db.collection("canteen")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Canteen temp = new Canteen();
                                temp.setCanteenID(document.getId());
                                temp.setOpen(document.getString("open"));
                                temp.setAirCon(document.getString("air-condition"));
                                temp.setNumOfStalls(document.getString("numOfStalls"));
                                temp.setPostCode(document.getString("postcode"));
                                temp.setName(document.getString("name"));
                                temp.setAddress(document.getString("address"));
                                temp.setCanteenID(document.getString("canteenID"));
                                temp.setDaily(document.getString("daily"));
                                temp.setDescription(document.getString("description"));
                                temp.setTerm(document.getString("term"));
                                temp.setClose(document.getString("close"));
                                temp.setPictureID(document.getString("pictureID"));
                                temp.setLat(document.getString("lat"));
                                temp.setLongt(document.getString("long"));
                                temp.setDistance(getDistance(temp));
                                canteenList.add(temp);
                                Canteen.addTocanteenList(temp.getCanteenID(), temp);
                            }
                            getTypes();
                        } else {
                            Log.d(TAG, "Error getting canteen documents: ", task.getException());
                        }
                    }
                });
    }

    private void getTypes(){
        db.collection("stallType")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                StallType temp = new StallType(document.getString("id"), document.getString("name"));
                                stallTypes.add(temp);
                                StallType.addItemToMap(temp);
                            }
                            ArrayList<Canteen> tempList = new ArrayList<>(canteenList);
                            if(!tempList.isEmpty()) {
                                getDesfromDB(0);
                            }
                        }
                    }
                });
    }

    private void getDesfromDB(int count){
        stallTypeIDs = new ArrayList<String>();
        db.collection("canteen")
                .document(canteenList.get(count).getCanteenID())
                .collection("stalls")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String id = document.getString("typeID");
                                if(!stallTypeIDs.contains(id)){
                                    stallTypeIDs.add(document.getString("typeID"));
                                }
                            }
                            String des = "";
                            for(int i = 0; i < stallTypeIDs.size(); i++) {
                                des += StallType.getStallTypesList().get(stallTypeIDs.get(i));
                                if(i < stallTypeIDs.size() - 1) {
                                    des += ", ";
                                }
                            }
                            canteenList.get(count).setDescription(des);
                            if(count < canteenList.size() - 1)
                                getDesfromDB(count + 1);
                            else
                                updateUI();
                        }
                    }
                });
    }

    private double getDistance(Canteen canteen) {

        final int R = 6371; // Radius of the earth

        double lat1 = mCurrentLocation.getLatitude();
        double lat2 = Double.parseDouble(canteen.getLat());
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(Double.parseDouble(canteen.getLongt()) - mCurrentLocation.getLongitude());
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c;

        double height = 0;

        distance = Math.pow(distance, 2) + Math.pow(height, 2);

        double result = Math.round(Math.sqrt(distance) * 100) / 100.0;

        return result;
    }

    @Override
    public void onStart() {
        super.onStart();
        canteen_running = true;
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}
