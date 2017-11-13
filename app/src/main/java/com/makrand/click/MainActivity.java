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

import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;

public class MainActivity extends AppCompatActivity {
    DatabaseReference db;

    Boolean play = false;

    private FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar tb = (Toolbar) findViewById(R.layout.toolbar);
        setSupportActionBar(tb);

        checkLogIn();
        try {
            db = FirebaseDatabase.getInstance().getReference("ERV/ambulance/" + auth.getCurrentUser().getUid());
        }
        catch (Exception e)
        {
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
        }

        ImageButton left = (ImageButton) findViewById(R.id.left);
        final ImageButton right = (ImageButton) findViewById(R.id.right);
        final FloatingActionButton start = findViewById(R.id.start);
        TextView title =  findViewById(R.id.appTitle);
        Typeface bold = Typeface.createFromAsset(getAssets(), "fonts/JosefinSans-SemiBold.ttf");
        title.setTypeface(bold);
        right.setBackgroundResource(R.drawable.ic_settings_gear_63);
        right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setCancelable(false)
                        .setTitle("Please Wait")
                        .setMessage("Logging Out");
                final AlertDialog ad = builder.create();
                PopupMenu menu = new PopupMenu(getApplicationContext(), right);
                menu.getMenuInflater().inflate(R.menu.main_activity_menu, menu.getMenu());
                menu.show();
                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getTitle().toString()){
                            case "Log Out":
                                try {
                                    ad.show();
                                    FirebaseAuth.getInstance().signOut();
                                    SmartLocation.with(getApplicationContext()).location().stop();
                                    Intent i = new Intent(getApplicationContext(), MapsActivity.class);
                                    ad.dismiss();
                                    startActivity(i);
                                    finish();
                                }
                                catch (Exception ex){
                                    Toast.makeText(getApplicationContext(), ex.toString(), Toast.LENGTH_LONG).show();
                                }
                                break;
                            default:
                                return false;
                        }
                        return false;
                    }
                });
            }
        });
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!play) {
                    play = true;

                    start.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_pause_24px));
                    SmartLocation
                            .with(getApplicationContext())
                            .location()
                            .start(new OnLocationUpdatedListener() {
                                @Override
                                public void onLocationUpdated(Location location) {
                                    db.child("latitude").setValue(String.valueOf(location.getLatitude()));
                                    db.child("longitude").setValue(String.valueOf(location.getLongitude()));
                                    db.child("id").setValue(String.valueOf(auth.getCurrentUser().getUid()));
                                }
                            });
                }
                else{
                    play = false;
                    start.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_play_arrow_24px));
                    SmartLocation.with(getApplicationContext()).location().stop();
                    db.child(auth.getCurrentUser().getUid()).removeValue();
                }

            }
        });
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
