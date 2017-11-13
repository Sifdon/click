package com.makrand.click;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.DrawableRes;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Api;
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
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;

import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    ArrayList<Marker> markers = new ArrayList<>();
    public static final String TAG = "Error";
    boolean changedView = false;
    FloatingActionButton myLocation;
    RelativeLayout body;
    RelativeLayout loader;
    Location currentLocation;
    View bottomsheet;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.maps_activity);
        //getting support for action bar aka toolbar
        Toolbar tb = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(tb);

        body = findViewById(R.id.body);
        loader = findViewById(R.id.loader);
        body.setVisibility(View.INVISIBLE);
        loader.setVisibility(View.VISIBLE);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.Click);
        mapFragment.getMapAsync(this);
        TextView title =  findViewById(R.id.appTitle);
        Typeface bold = Typeface.createFromAsset(getAssets(), "fonts/JosefinSans-SemiBold.ttf");
        title.setTypeface(bold);
        ProgressBar spin = findViewById(R.id.spin);
        bottomsheet = getLayoutInflater().inflate(R.layout.dialog_layout, null);
        ImageButton left =  findViewById(R.id.left);
        final ImageButton right = findViewById(R.id.right);
        myLocation = findViewById(R.id.myLocation);
        left.setBackgroundResource(R.drawable.ic_search_24px);
        right.setBackgroundResource(R.drawable.ic_settings_gear_63);
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
        SmartLocation.with(this)
                .location()
                .start(new OnLocationUpdatedListener() {
                    @Override
                    public void onLocationUpdated(Location location) {
                        currentLocation = location;
                    }
                });


        runBackgroundCheck();
        showBottomSheet(this, bottomsheet);
    }

    void showBottomSheet(Context context, View view){
        try {
            BottomSheetDialog dialog = new BottomSheetDialog(context);
            dialog.setContentView(view);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(false);
        }
        catch (Exception e){
            Log.e("Bottomsheet error", e.toString());
        }
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
                        LatLng start = new LatLng(Double.parseDouble(model.getLatitude()), Double.parseDouble(model.getLongitude()));
                        for (Marker m : markers) {
                            Tag i = (Tag) m.getTag();
                            if(model.getId().equals(i.getId())) {
                                animateMarker(start, start, false, m);
                                if(calcDistance(model) <= 3000 && !i.getEntered()){
                                    i.setEntered(true);
                                    showNotification("Ambulance Detected", "Ambulance detected in proximity. Heads up!");
                                }
                                else if(calcDistance(model) > 3000){
                                    i.setEntered(false);
                                }
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

    public float calDistance (Model model)
    {
        Location ambLocation = new Location("changedLocation");
        Location myLocation = new Location("myLocation");
        ambLocation.setLatitude(Double.parseDouble(model.getLatitude()));
        ambLocation.setLongitude(Double.parseDouble(model.getLongitude()));
        myLocation.setLatitude(currentLocation.getLatitude());
        myLocation.setLongitude(currentLocation.getLongitude());
        return myLocation.distanceTo(ambLocation);
    }

    public double calcDistance(Model m){
        LatLng ambLoc = new LatLng(Double.parseDouble(m.getLatitude()), Double.parseDouble(m.getLongitude()));
        LatLng myLoc = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        return SphericalUtil.computeDistanceBetween(myLoc, ambLoc);
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

    public void showNotification(String text, String bigText) {

        // 1. Create a NotificationManager
        NotificationManager notificationManager =
                (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        // 2. Create a PendingIntent for AllGeofencesActivity
        Intent intent = new Intent(this, MapsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingNotificationIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // 3. Create and send a notification
        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(text)
                .setContentText(bigText)
                .setContentIntent(pendingNotificationIntent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(bigText))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setVibrate(new long[] { 1000, 1000})
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .build();

        notificationManager.notify(0, notification);
    }
}

//TODO : Check google play services
