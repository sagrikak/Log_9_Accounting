package com.example.android.log9accounting;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import com.google.firebase.auth.FirebaseAuth;

public class AdminMainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);
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

    public void onCashPressed(View view){
        Intent intent = new Intent(this, DisplayCashActivity.class);
        startActivity(intent);
    }

    public void onCardPressed(View view){
        Intent intent = new Intent(this, DisplayCardActivity.class);
        startActivity(intent);
    }

    public void onNeftPressed(View view){
        Intent intent = new Intent(this, DisplayNeftActivity.class);
        startActivity(intent);
    }

    public void onCashSummaryPressed(View view){
        Intent intent = new Intent(this, CashSummaryActivity.class);
        startActivity(intent);
    }

    public void onCardSummaryPressed(View view){
        Intent intent = new Intent(this, CardSummaryActivity.class);
        startActivity(intent);
    }

    public void onNeftSummaryPressed(View view){
        Intent intent = new Intent(this, NeftSummaryActivity.class);
        startActivity(intent);
    }

    public void onResetPasswordPressed(View view){
        Intent intent = new Intent(this, ResetPasswordActivity.class);
        startActivity(intent);
    }

    public void onLogOut(View view) {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, LogInActivity.class);
        startActivity(intent);
    }
}
