package com.makrand.click;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.net.InetAddress;

public class startup extends AppCompatActivity {
    boolean called = false;

    private ConstraintLayout layout;
    TextView text;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);
        ActionBar bar = getSupportActionBar();
        bar.hide();

        Typeface bold = Typeface.createFromAsset(getAssets(), "fonts/JosefinSans-SemiBold.ttf");
        text = findViewById(R.id.brand_name);
        text.setTypeface(bold);
        if(!called) {
            try {
                FirebaseDatabase.getInstance().setPersistenceEnabled(true);
            }
            catch (Exception e){
                Log.d("Excep. for persistance", e.toString());
            }
            called = true;
        }
        uniChecker();
    }

    void uniChecker(){

        if(isGooglePlayServicesAvailable(this, startup.this)){
            FirebaseAuth auth = FirebaseAuth.getInstance();

                if(isLocationServiceEnabled(this)){
                    if(isLoggedIn(auth)){
                        Intent i = new Intent(this, MainActivity.class);
                        startActivity(i);
                        finish();
                    }
                    else {
                        Intent i = new Intent(this, MapsActivity.class);
                        startActivity(i);
                        finish();
                    }
                }
                else {
                    final Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Location service is disabled", Snackbar.LENGTH_INDEFINITE);
                    snackbar.setAction("Enable", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent= new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(intent);
                        }
                    });
                    snackbar.setActionTextColor(Color.YELLOW);
                    snackbar.show();
                }

            if(!isOnline()) {
                final Snackbar snackbar = showSnackbar("No network available", findViewById(android.R.id.content), "Okay", Color.YELLOW);
                final Handler handler = new Handler();
                final int delay = 1000; //milliseconds
                handler.postDelayed(new Runnable(){
                    public void run(){
                        if (isOnline()){
                            snackbar.dismiss();
                            showSnackbar("You are now online", findViewById(android.R.id.content), "Cool !", Color.GREEN);
                            return;
                        }
                        handler.postDelayed(this, delay);
                    }
                }, delay);
                isLocationServiceEnabled(this);
            }
        }
        else{
            Toast.makeText(getApplicationContext(), "Google Play Services not available", Toast.LENGTH_LONG).show();
        }

    }

    Snackbar showSnackbar(String msg, View view, String btnMsg, int color){
        final Snackbar snackbar = Snackbar.make(view, msg, Snackbar.LENGTH_LONG);
        snackbar.setAction(btnMsg, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                snackbar.dismiss();
            }
        });
        snackbar.setActionTextColor(color);
        snackbar.show();
        return snackbar;
    }
    public void onResume(){
        super.onResume();
        uniChecker();
    }
    public boolean isGooglePlayServicesAvailable(Context context, Activity activity){
        Dialog errorDialog = null;
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(resultCode)) {

                if (errorDialog == null) {
                    errorDialog = googleApiAvailability.getErrorDialog(activity, resultCode, 2404);
                    errorDialog.setCancelable(false);
                }

                if (!errorDialog.isShowing())
                    errorDialog.show();

            }
        }
        return resultCode == ConnectionResult.SUCCESS;
    }


    public boolean isLoggedIn(FirebaseAuth auth){
        if(auth.getCurrentUser() != null){
            return true;
        }
        else{
            return false;
        }
    }

    public boolean isOnline() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int     exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        }
        catch (IOException e)          { e.printStackTrace(); }
        catch (InterruptedException e) { e.printStackTrace(); }

        return false;
    }

    public boolean isLocationServiceEnabled(Context context){
        LocationManager locationManager = null;
        boolean gps_enabled= false,network_enabled = false;

        if(locationManager ==null)
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        try{
            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }catch(Exception ex){
            //do nothing...
        }

        try{
            network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        }catch(Exception ex){
            //do nothing...
        }

        return gps_enabled || network_enabled;
    }



}
