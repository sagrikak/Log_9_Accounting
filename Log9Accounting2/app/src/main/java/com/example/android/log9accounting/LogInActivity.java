package com.example.android.log9accounting;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LogInActivity extends AppCompatActivity {
    EditText etCode, etPwd;
    String email, password, code, type, ui;
    private FirebaseAuth mAuth;
    private ProgressDialog progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        etCode = findViewById(R.id.et_empcode);
        etPwd = findViewById(R.id.et_password);

        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle("Exit")
                .setMessage("Are you sure?")
                .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.addCategory(Intent.CATEGORY_HOME);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                }).setNegativeButton("no", null).show();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void onLogIn(View view) {
        code = etCode.getText().toString();
        password = etPwd.getText().toString();
        email = code + "@yahoo.com";

        if (TextUtils.isEmpty(etCode.getText().toString()) || TextUtils.isEmpty(etPwd.getText().toString())) {
            Toast toast = Toast.makeText(getApplicationContext(), "Please enter all the details", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        if (!isNetworkAvailable()) {
            Toast toast = Toast.makeText(getApplicationContext(), "Network not available", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        //creating and showing progress dialog
        progressBar = new ProgressDialog(this);
        progressBar.setMax(100);
        progressBar.setMessage("Logging In...");
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        if(isNetworkAvailable())
            progressBar.show();
        progressBar.setCancelable(false);
        //starting upload

        loginUser(email, password);
    }

    public void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("TAG", "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                            progressBar.dismiss();
                            switchActivity();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.d("signInWithEmail:failure", String.valueOf(task.getException()));
                            Toast.makeText(LogInActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            progressBar.dismiss();
                            updateUI(null);
                        }

                        // ...
                    }
                });
    }

    public void switchActivity() {
        Intent intent = new Intent(this, AdminMainActivity.class);
        startActivity(intent);
    }

    private void updateUI(FirebaseUser user) {
        if(user!=null) {
            ui = user.getEmail();
            Log.d("Current5", ui);
        }
    }
}
