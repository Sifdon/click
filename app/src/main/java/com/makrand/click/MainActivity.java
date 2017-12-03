package com.makrand.click;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.location.Location;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;
import pl.bclogic.pulsator4droid.library.PulsatorLayout;

public class MainActivity extends AppCompatActivity {
    DatabaseReference db;

    Boolean play = false;
    PulsatorLayout pulsator;

    private FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle(null);
        Toolbar tb = findViewById(R.id.main_bar);
        setSupportActionBar(tb);
        pulsator = (PulsatorLayout) findViewById(R.id.pulsator);
        checkLogIn();
        try {
            db = FirebaseDatabase.getInstance().getReference("ERV/ambulance/");
        }
        catch (Exception e)
        {
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
        }


        final ImageButton start = findViewById(R.id.start);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!play) {
                    play = true;
                    pulsator.start();
                    start.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_pause_24px));
                    SmartLocation
                            .with(getApplicationContext())
                            .location()
                            .start(new OnLocationUpdatedListener() {
                                @Override
                                public void onLocationUpdated(Location location) {
                                    Map<String, Model> ervLocation = new HashMap<>();
                                    ervLocation.put(auth.getCurrentUser().getUid(), new Model(
                                            String.valueOf(location.getLatitude()),
                                            String.valueOf(location.getLongitude()),
                                            String.valueOf(auth.getCurrentUser().getUid())
                                    ));
                                    db.setValue(ervLocation);
                                }
                            });
                }
                else{
                    play = false;
                    pulsator.stop();
                    start.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_play_arrow_24px));
                    SmartLocation.with(getApplicationContext()).location().stop();
                    db.child(auth.getCurrentUser().getUid()).removeValue();
                }

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.settings:
                return false;
            case R.id.logOut:
                if(logOut()){
                    startActivity(new Intent(getApplicationContext(), MapsActivity.class));
                    finish();
                }
        }
        return true;
    }
    private boolean logOut(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setCancelable(false)
                .setTitle("Please Wait")
                .setMessage("Logging Out");
        final AlertDialog ad = builder.create();
        try {
            ad.show();
            FirebaseAuth.getInstance().signOut();
            SmartLocation.with(getApplicationContext()).location().stop();
            ad.dismiss();
            return true;
        }
        catch (Exception ex){
            Toast.makeText(getApplicationContext(), ex.toString(), Toast.LENGTH_LONG).show();
            return false;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        SmartLocation.with(getApplicationContext()).location().stop();

    }
    @Override
    public void onResume() {
        super.onResume();
        checkLogIn();
    }

    void checkLogIn(){
        auth = FirebaseAuth.getInstance();
        if(auth.getCurrentUser()==null){
            Toast.makeText(this, "Access is denied", Toast.LENGTH_LONG).show();
            Intent i = new Intent(this, MapsActivity.class);
            startActivity(i);
            finish();
        }
    }


}
// TODO: 30-09-2017 remove firebase data node upon activity stop
// TODO: 30-09-2017 check for permissions and ask for them
