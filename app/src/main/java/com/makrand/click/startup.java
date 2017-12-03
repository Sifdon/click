package com.makrand.click;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.io.IOException;
import java.util.ArrayList;

public class startup extends AppCompatActivity {
    boolean called = false;
    Boolean permissionGranted = false;
    Boolean paused = false;
    private ConstraintLayout layout;
    TextView text;
    Button retry_btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.primaryColor));
        }
        text = findViewById(R.id.brand_name);
        retry_btn = findViewById(R.id.retry_btn);
        retry_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkServiceAvailable();
            }
        });
        if(!called) {
            try {
                FirebaseDatabase.getInstance().setPersistenceEnabled(true);
            }
            catch (Exception e){
                Log.d("Excep. for persistence", e.toString());
            }
            called = true;
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermissionGranted(this);
        }
        else{
            permissionGranted = true;
            checkServiceAvailable();
        }
    }
    void checkPermissionGranted(Context context){
        if(TedPermission.isGranted(context, Manifest.permission.ACCESS_FINE_LOCATION)){
            permissionGranted = true;
            checkServiceAvailable();
        }
        else {
            grantPermissions(this);
        }
    }
    void grantPermissions(final Context context){
        TedPermission.with(context)
                .setPermissionListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted() {
                        permissionGranted = true;
                    }
                    @Override
                    public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                        permissionGranted = false;
                        final Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Required permissions must be granted", Snackbar.LENGTH_LONG);
                        snackbar.setAction("Grant", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                snackbar.dismiss();
                                final Intent i = new Intent();
                                i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                i.addCategory(Intent.CATEGORY_DEFAULT);
                                i.setData(Uri.parse("package:" + getApplicationContext().getPackageName()));
                                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                                getApplicationContext().startActivity(i);
                            }
                        });
                        snackbar.setActionTextColor(Color.GREEN);
                        snackbar.setDuration(Snackbar.LENGTH_INDEFINITE);
                        snackbar.show();
                    }
                })
                .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at Settings > Permission")
                .setPermissions(android.Manifest.permission.ACCESS_FINE_LOCATION)
                .check();
    }
    boolean checkServiceAvailable(){
        if(isGooglePlayServicesAvailable(this, startup.this)){
            if(isLocationServiceEnabled(this)){
                if(isOnline()){
                    FirebaseAuth auth = FirebaseAuth.getInstance();
                    if(permissionGranted) {
                        if (isLoggedIn(auth)) {
                            Intent i = new Intent(this, MainActivity.class);
                            startActivity(i);
                            finish();
                        } else {
                            Intent i = new Intent(this, MapsActivity.class);
                            startActivity(i);
                            finish();
                        }
                    }

                }
                else{
                    final Snackbar snackbar = showSnackbar("No network available", findViewById(android.R.id.content), "Okay", Color.YELLOW);
                    final Handler handler = new Handler();
                    final int delay = 2000; //milliseconds
                    handler.postDelayed(new Runnable(){
                        public void run(){
                            if (isOnline()){
                                snackbar.dismiss();
                                final Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "You are now online.", Snackbar.LENGTH_LONG);
                                snackbar.setAction("Okay !", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        snackbar.dismiss();
                                        FirebaseAuth auth = FirebaseAuth.getInstance();
                                        if (isLoggedIn(auth)) {
                                            Intent i = new Intent(getApplicationContext(), MainActivity.class);
                                            startActivity(i);
                                            finish();
                                        } else {
                                            Intent i = new Intent(getApplicationContext(), MapsActivity.class);
                                            startActivity(i);
                                            finish();
                                        }
                                    }
                                });
                                snackbar.setActionTextColor(Color.YELLOW);
                                snackbar.show();
                            }
                            handler.postDelayed(this, delay);
                        }
                    }, delay);
                    //isLocationServiceEnabled(this);
                    return false;
                }
            }
            else{
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
                return false;
            }
        }
        else {
            final Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Google Play Services not available", Snackbar.LENGTH_INDEFINITE);
            snackbar.show();
            return false;
        }
        return false;
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
        if(paused) {
            if(TedPermission.isGranted(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)){
                checkServiceAvailable();
            } else {
                final Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Required permissions must be granted", Snackbar.LENGTH_LONG);
                snackbar.setAction("Grant", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        snackbar.dismiss();
                        final Intent i = new Intent();
                        i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        i.addCategory(Intent.CATEGORY_DEFAULT);
                        i.setData(Uri.parse("package:" + getApplicationContext().getPackageName()));
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                        getApplicationContext().startActivity(i);
                    }
                });
                snackbar.setActionTextColor(Color.GREEN);
                snackbar.setDuration(Snackbar.LENGTH_INDEFINITE);
                snackbar.show();
            }
        }
    }
    public void onPause() {
        super.onPause();
        paused = true;
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

        }

        try{
            network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        }catch(Exception ex){

        }

        return gps_enabled || network_enabled;
    }
}
