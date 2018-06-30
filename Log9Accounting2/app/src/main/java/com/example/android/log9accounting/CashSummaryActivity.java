package com.example.android.log9accounting;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class CashSummaryActivity extends AppCompatActivity {

    private FirebaseDatabase mDatabase;
    private DatabaseReference dbRef;
    private ListView listview;
    ArrayList<String> id=new ArrayList<>();
    ArrayList<String> date=new ArrayList<>();
    ArrayList<String> amount=new ArrayList<>();
    ArrayList<String> description=new ArrayList<>();
    ArrayList<String> type=new ArrayList<>();
    ArrayList<String> head=new ArrayList<>();
    ArrayList<String> reimburse=new ArrayList<>();
    ArrayList<String> image=new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cash_summary);

        mDatabase = FirebaseDatabase.getInstance();
        dbRef = mDatabase.getReference("Cash");

        final CustomList adapter = new
                CustomList(CashSummaryActivity.this, id, date, amount, description, type, image, head, reimburse);

        listview = findViewById(R.id.list_cash_summary);

        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot year: dataSnapshot.getChildren()) {
                    if(!year.getKey().equals("Transaction Type")) {
                        Log.d("Year", year.getKey());
                        for(DataSnapshot month: year.getChildren()) {
                            Log.d("Month", month.getKey());
                            for(DataSnapshot transactionId: month.getChildren()) {
                                if(!transactionId.getKey().equals("Monthly Counter")) {
                                    Log.d("Transaction ID", transactionId.getKey());
                                    id.add("Transaction ID: " + transactionId.child("ID").getValue());
                                    date.add("Transaction Date: " + transactionId.child("Transaction Date").getValue());
                                    amount.add("Amount: " + transactionId.child("Amount").getValue());
                                    description.add("Description: " + transactionId.child("Description").getValue());
                                    type.add("Transaction Type: " + transactionId.child("Type").getValue());
                                    head.add("Head: " + transactionId.child("Transaction Head").getValue());
                                    image.add("" + transactionId.child("Image").getValue());
                                    if(transactionId.child("Reimbursement ID").getValue() != null) {
                                        reimburse.add("Reimbursement ID: " + transactionId.child("Reimbursement ID").getValue());
                                    } else {
                                        reimburse.add("");
                                    }
                                    //adapter.notifyDataSetChanged();
                                }
                            }
                        }
                    }
                }
                listview.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
    @Override
    public void onBackPressed()
    {
        Intent intent = new Intent(this, AdminMainActivity.class);
        startActivity(intent);
        finish();
    }
}
