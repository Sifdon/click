package com.makrand.click;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

public class logInActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    TextView title, disc;
    EditText email, password;
    Button logIn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        Toolbar tb = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(tb);

        auth = FirebaseAuth.getInstance();
        title = findViewById(R.id.logIn_title);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        logIn = findViewById(R.id.logIn_btn);
        disc = findViewById(R.id.disc);

        logIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isEmpty(email) || isEmpty(password)){
                    Toast.makeText(getApplicationContext(), "Both fields are important", Toast.LENGTH_SHORT).show();
                }
                else {
                    doLogin(auth, email.getText().toString().trim(), password.getText().toString().trim());
                }
            }
        });
    }

    private boolean isEmpty(EditText etText) {
        if (etText.getText().toString().trim().length() > 0)
            return false;

        return true;
    }
    void doLogin(FirebaseAuth auth, String email, String password){
        AlertDialog.Builder builder = new AlertDialog.Builder(logInActivity.this);
        builder.setCancelable(false)
                .setTitle("Please Wait")
                .setMessage("Logging In");
        final AlertDialog ad = builder.create();
        ad.show();
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            ad.dismiss();
                            Toast.makeText(getApplicationContext(), "Login Successful", Toast.LENGTH_LONG).show();
                            Intent i = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(i);
                            finish();
                        }
                        else {
                            ad.dismiss();
                            Toast.makeText(getApplicationContext(), "Login failed, try again later"+ task.getException(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}
