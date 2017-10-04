package com.makrand.click;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.ContactsContract;
import android.support.annotation.DrawableRes;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.arsy.maps_library.MapRipple;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import io.nlopez.smartlocation.OnGeofencingTransitionListener;
import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;
import io.nlopez.smartlocation.geofencing.model.GeofenceModel;
import io.nlopez.smartlocation.geofencing.utils.TransitionGeofence;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    ArrayList<Marker> markers = new ArrayList<>();

    float distance;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.maps_activity);
        //getting support for action bar aka toolbar
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.toolbar);
        View view =getSupportActionBar().getCustomView();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.Click);
        mapFragment.getMapAsync(this);

        TextView title = (TextView) findViewById(R.id.appTitle);
        Typeface bold = Typeface.createFromAsset(getAssets(), "fonts/JosefinSans-SemiBold.ttf");
        title.setTypeface(bold);
        ImageButton left = (ImageButton) findViewById(R.id.left);
        final ImageButton right = (ImageButton) findViewById(R.id.right);
        left.setBackgroundResource(R.drawable.ic_zoom_2);
        right.setBackgroundResource(R.drawable.ic_menu_18px);

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
                            default:
                                return false;
                        }
                        return false;
                    }
                });
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

        MapStyleOptions style = MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style);
        mMap.setMapStyle(style);

        //TODO : check permissions
        try {
            mMap.setMyLocationEnabled(true);
            //mMap.animateCamera( CameraUpdateFactory.zoomTo( 17.0f ) );
        }
        catch (SecurityException se){
            Log.e("Son of bitch","Exception happened");
        }
        SmartLocation.with(getApplicationContext()).location()
                .oneFix()
                .start(new OnLocationUpdatedListener() {
                    @Override
                    public void onLocationUpdated(Location location) {
                        doZoom(location);
                        SmartLocation.with(getApplicationContext()).location().stop();
                    }
                });

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
                            if(model.getId().equals(i.getId()))
                                animateMarker(start, start, false, m);
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
    void geoFencer(Model m){
        String id = "id_" + m.getId();
        try {
            GeofenceModel Summer = new GeofenceModel.Builder(id)
                    .setTransition(Geofence.GEOFENCE_TRANSITION_ENTER)
                    .setLatitude(Double.parseDouble(m.getLatitude()))
                    .setLongitude(Double.parseDouble(m.getLongitude()))
                    .setRadius(1000)
                    .build();
            SmartLocation.with(this).geofencing()
                    .add(Summer)
                    .start(new OnGeofencingTransitionListener() {
                        @Override
                        public void onGeofenceTransition(TransitionGeofence transitionGeofence) {
                            int i = transitionGeofence.getTransitionType();
                            Log.d("Transition", String.valueOf(i));
                        }
                    });
        }
        catch (Exception e){
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
        }
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
