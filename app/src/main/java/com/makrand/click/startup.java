package com.makrand.click;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.net.InetAddress;

public class startup extends AppCompatActivity {
    boolean called = false;
    Dialog errorDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);
        isGooglePlayServicesAvailable(this);
        if (isNetworkConnected()){
            if (!isInternetAvailable()){
                showDialog("Offline", "Device is connected to a network but no internet available. Continue ?");
            }

        }
        else {
            showDialog("Offline", "Network not available");
        }
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if(!called) {
            try {
                FirebaseDatabase.getInstance().setPersistenceEnabled(true);
            }
            catch (Exception e){
                Log.d("Excep. for persistance", e.toString());
            }
            called = true;
        }
        if(auth.getCurrentUser() != null){
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
            finish();
        }
        else{
            Intent i = new Intent(this, MapsActivity.class);
            startActivity(i);
            finish();
        }
    }
    public void onResume(){
        super.onResume();
        isGooglePlayServicesAvailable(this);
    }
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }
    void showDialog(String title, String msg){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(msg)
                .setTitle(title)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }


    public boolean isInternetAvailable() {
        try {
            InetAddress ipAddr = InetAddress.getByName("google.com"); //You can replace it with your name
            return !ipAddr.equals("");

        } catch (Exception e) {
            return false;
        }

    }
    public boolean isGooglePlayServicesAvailable(Context context){
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(resultCode)) {

                if (errorDialog == null) {
                    errorDialog = googleApiAvailability.getErrorDialog(this, resultCode, 2404);
                    errorDialog.setCancelable(false);
                }

                if (!errorDialog.isShowing())
                    errorDialog.show();

            }
        }
        return resultCode == ConnectionResult.SUCCESS;
    }

    }
