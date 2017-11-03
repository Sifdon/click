package com.makrand.click;

import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.DrawableRes;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;

import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    ArrayList<Geofence> mGeofenceList= new ArrayList<>();
    ArrayList<Marker> markers = new ArrayList<>();
    private GoogleApiClient mGoogleApiClient;
    PendingIntent mGeofencePendingIntent;
    public static final String TAG = "Error";
    boolean changedView = false;
    FloatingActionButton myLocation;
    FrameLayout body;
    RelativeLayout loader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.maps_activity);

        //getting support for action bar aka toolbar
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.toolbar);

        View view =getSupportActionBar().getCustomView();
        body = findViewById(R.id.body);
        loader = findViewById(R.id.loader);
        body.setVisibility(View.INVISIBLE);
        loader.setVisibility(View.VISIBLE);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.Click);
        mapFragment.getMapAsync(this);
        initGoogleAPIClient();
        TextView title =  findViewById(R.id.appTitle);
        Typeface bold = Typeface.createFromAsset(getAssets(), "fonts/JosefinSans-SemiBold.ttf");
        title.setTypeface(bold);
        ProgressBar spin = findViewById(R.id.spin);

        ImageButton left =  findViewById(R.id.left);
        final ImageButton right = findViewById(R.id.right);
        myLocation = findViewById(R.id.myLocation);
        left.setBackgroundResource(R.drawable.ic_zoom_2);
        right.setBackgroundResource(R.drawable.ic_preferences);
        spin.setVisibility(View.VISIBLE);
        right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu menu = new PopupMenu(getApplicationContext(), right);
                menu.getMenuInflater().inflate(R.menu.map_activity_menu, menu.getMenu());
                menu.show();
                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getTitle().toString()){
                            case "Log In":
                                Intent i = new Intent(getApplicationContext(), logInActivity.class);
                                startActivity(i);
                                break;
                            case "Settings":
                                return false;
                            default:
                                return false;
                        }
                        return false;
                    }
                });
            }
        });
        myLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMap != null){
                    SmartLocation.with(getApplicationContext())
                            .location()
                            .oneFix()
                            .start(new OnLocationUpdatedListener() {
                                @Override
                                public void onLocationUpdated(Location location) {
                                    LatLng latLang = new LatLng(location.getLatitude(), location.getLongitude());
                                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLang));
                                    mMap.animateCamera( CameraUpdateFactory.zoomTo( 17.0f ) );
                                    if(!changedView){
                                        changedView = true;
                                        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                                                new CameraPosition.Builder()
                                                        .target(latLang)
                                                        .tilt(75)
                                                        .zoom(15)
                                                        .build()
                                        ));
                                        myLocation.setImageResource(R.drawable.ic_my_location_24px);
                                    }
                                    else {
                                        changedView = false;
                                        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                                                new CameraPosition.Builder()
                                                        .tilt(0)
                                                        .target(latLang)
                                                        .zoom(15)
                                                        .build()
                                        ));
                                        myLocation.setImageResource(R.drawable.ic_compass_05);
                                    }
                                }
                            });

                }
            }
        });
        runBackgroundCheck();

    }



    @Override
    public void onResume(){
        super.onResume();

    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //mMap.getUiSettings().setMyLocationButtonEnabled(true);
        //MapStyleOptions style = MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style);
        //mMap.setMapStyle(style);

        //TODO : check permissions
        try {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            SmartLocation.with(getApplicationContext())
                    .location()
                    .oneFix()
                    .start(new OnLocationUpdatedListener() {
                        @Override
                        public void onLocationUpdated(Location location) {
                            LatLng latLang = new LatLng(location.getLatitude(), location.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLang));
                            mMap.animateCamera( CameraUpdateFactory.zoomTo( 17.0f ) );
                        }
                    });
        }
        catch (SecurityException se){
            Log.e("Son of bitch","Exception happened");
        }
        body.setVisibility(View.VISIBLE);
        loader.setVisibility(View.GONE);

    }

    void runBackgroundCheck(){
        DatabaseReference db = FirebaseDatabase.getInstance().getReference("ERV/ambulance/");
        db.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Model m = dataSnapshot.getValue(Model.class);
                    int i  = setMarker(m);
                    if(i == 0){
                        Toast.makeText(getApplicationContext(), "Error adding marker", Toast.LENGTH_SHORT).show();
                    }
                    createGeofence(m);
                    addGeofences();
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Model model = dataSnapshot.getValue(Model.class);
                for (Marker m : markers) {
                    Tag i = (Tag) m.getTag();
                    if(model.getId().equals(i.getId())){
                        m.remove();
                        markers.remove(m);
                    }
                }
                removeGeofence("id_"+ model.getId());
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    Model model = snapshot.getValue(Model.class);
                    if(model.getLatitude() != null && model.getLongitude()!= null) {
                        removeGeofence("id_" + model.getId());
                        LatLng start = new LatLng(Double.parseDouble(model.getLatitude()), Double.parseDouble(model.getLongitude()));
                        for (Marker m : markers) {
                            Tag i = (Tag) m.getTag();
                            if(model.getId().equals(i.getId())) {
                                animateMarker(start, start, false, m);
                                createGeofence(model);
                                addGeofences();
                            }
                        }
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    public void removeGeofence(String id) {
        ArrayList<String> idList = new ArrayList<>();
        idList.add(id);
        for (int i = 0; i < mGeofenceList.size(); i++) {
            Geofence g = mGeofenceList.get(i);
            if (g.getRequestId().equals(id)) {
                mGeofenceList.remove(i);
            }
        }
        if (mGoogleApiClient.isConnected()) {
            try {
                LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient, idList);
            }
            catch (Exception e){
                Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
            }
        }
    }
    int setMarker(final Model m){
        if(m.getLatitude() == null || m.getLongitude() == null)
            return  0;
        DatabaseReference profile = FirebaseDatabase.getInstance().getReference().child("ERV/users/ambulance/"+m.getId());
        profile.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                LatLng pos = new LatLng(Float.parseFloat(m.getLatitude()), Float.parseFloat(m.getLongitude()));
                MarkerOptions options = new MarkerOptions()
                        .position(pos)
                        .title(dataSnapshot.child("name").getValue().toString())
                        .icon(getBitmapDescriptor(R.drawable.ic_red_circle_alt));
                Marker marker = mMap.addMarker(options);
                animateMarker(pos, pos, false, marker);
                Tag t = new Tag(m.getId(), dataSnapshot.child("name").getValue().toString(), dataSnapshot.child("licence").getValue().toString());
                marker.setTag(t);
                markers.add(marker);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return 1;
    }
    void createGeofence(Model m){
        String id;
        if(m.getLatitude() == null && m.getLongitude() == null) {
            Log.d("Null", "Null value");
        }
        else{
            id = "id_" + m.getId();
            Geofence fence = new Geofence.Builder()
                    .setRequestId(id)
                    .setLoiteringDelay(5000)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT | Geofence.GEOFENCE_TRANSITION_DWELL)
                    .setCircularRegion(Double.parseDouble(m.getLatitude()), Double.parseDouble(m.getLongitude()), 3000)
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .build();
            mGeofenceList.add(fence);
        }
    }

    private GeofencingRequest geofencingRequest(){
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }

    public void initGoogleAPIClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addOnConnectionFailedListener(connectionFailedListener)
                .build();
        mGoogleApiClient.connect();
    }

        public void addGeofences() {
            if (mGoogleApiClient.isConnected()) {
                try {
                    LocationServices.GeofencingApi.addGeofences(
                            mGoogleApiClient,
                            geofencingRequest(),
                            getGeofencePendingIntent()
                    ).setResultCallback(new ResultCallback<Status>() {

                        @Override
                        public void onResult(Status status) {
                            if (status.isSuccess()) {
                                Log.i(TAG, "Saving Geofence");

                            } else {
                                Log.e(TAG, "Registering geofence failed: " + status.getStatusMessage() +
                                        " : " + status.getStatusCode());
                            }
                        }
                    });

                } catch (SecurityException securityException) {
                    // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
                    Log.e(TAG, "Error");
                }
            }
            else {
                Log.e(TAG, "API client not connected");
            }

    }

    private GoogleApiClient.OnConnectionFailedListener connectionFailedListener =
            new GoogleApiClient.OnConnectionFailedListener() {
                @Override
                public void onConnectionFailed(ConnectionResult connectionResult) {
                    Log.e(TAG, "onConnectionFailed");
                }
            };


    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        return PendingIntent.getService(this, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
    }
    public float calDistance (float lat_a, float lng_a, float lat_b, float lng_b )
    {
        double earthRadius = 3958.75;
        double latDiff = Math.toRadians(lat_b-lat_a);
        double lngDiff = Math.toRadians(lng_b-lng_a);
        double a = Math.sin(latDiff /2) * Math.sin(latDiff /2) +
                Math.cos(Math.toRadians(lat_a)) * Math.cos(Math.toRadians(lat_b)) *
                        Math.sin(lngDiff /2) * Math.sin(lngDiff /2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double distance = earthRadius * c;
        int meterConversion = 1609;
        return new Float(distance * meterConversion).floatValue();
    }

    void doZoom(Location location){
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 12.5f));
    }
    private BitmapDescriptor getBitmapDescriptor(@DrawableRes int id) {
        Drawable vectorDrawable = ResourcesCompat.getDrawable(getResources(), id, null);
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    public void animateMarker(final LatLng startPosition, final LatLng toPosition,
                              final boolean hideMarker, final Marker marker) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();

        final long duration = 1000;
        final Interpolator interpolator = new LinearInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed
                        / duration);
                double lng = t * toPosition.longitude + (1 - t)
                        * startPosition.longitude;
                double lat = t * toPosition.latitude + (1 - t)
                        * startPosition.latitude;

                marker.setPosition(new LatLng(lat, lng));

                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                } else {
                    if (hideMarker) {
                        marker.setVisible(false);
                    } else {
                        marker.setVisible(true);
                    }
                }
            }
        });

    }
}

//TODO : Check google play services
