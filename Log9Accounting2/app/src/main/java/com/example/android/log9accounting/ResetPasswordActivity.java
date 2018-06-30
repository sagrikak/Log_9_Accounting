package com.example.android.log9accounting;

import android.content.Context;
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

public class ResetPasswordActivity extends AppCompatActivity {
    private String curPswd, newPswd, email, newPswd2;
    private EditText curPassword, newPassword, newPassword2;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        curPassword = findViewById(R.id.et_current_pswd);
        newPassword = findViewById(R.id.et_new_pswd);
        newPassword2 = findViewById(R.id.et_new_pswd2);

        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    @Override
    public void onBackPressed()
    {
        Intent intent = new Intent(this, AdminMainActivity.class);
        startActivity(intent);
        finish();
    }

    public void updateUI(FirebaseUser user) {
        if(user == null) {
            Intent intent = new Intent(this, LogInActivity.class);
            startActivity(intent);
        } else  {
            email = user.getEmail();
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void switchActivity() {
        Intent intent = new Intent(this, AdminMainActivity.class);
        startActivity(intent);
    }

    public void onResetPasswordPressed(View view) {
        curPswd = curPassword.getText().toString();
        newPswd = newPassword.getText().toString();
        newPswd2 = newPassword2.getText().toString();

        if (TextUtils.isEmpty(curPassword.getText().toString()) || TextUtils.isEmpty(newPassword.getText().toString()) || TextUtils.isEmpty(newPassword2.getText().toString())) {
            Toast toast = Toast.makeText(getApplicationContext(), "Please enter all the details", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        if (!isNetworkAvailable()) {
            Toast toast = Toast.makeText(getApplicationContext(), "Network not available", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, curPswd)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("signin", "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            if(newPswd.length() < 6) {
                                Toast.makeText(ResetPasswordActivity.this, "New password should at least be 6 characters long.",
                                        Toast.LENGTH_SHORT).show();
                            }
                            if(!newPswd.equals(newPswd2)) {
                                Toast.makeText(ResetPasswordActivity.this, "New passwords don't match.",
                                        Toast.LENGTH_SHORT).show();
                            }
                            if(newPswd.length() >= 6 && newPswd.equals(newPswd2)) {
                                user.updatePassword(newPswd)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Log.d("passwordchange", "User password updated.");
                                                    Toast.makeText(ResetPasswordActivity.this, "Password Changed.",
                                                            Toast.LENGTH_SHORT).show();
                                                    switchActivity();
                                                }
                                            }
                                        });
                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("signin", "signInWithEmail:failure", task.getException());
                            Toast.makeText(ResetPasswordActivity.this, "Current password does not match.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });


    }
}
