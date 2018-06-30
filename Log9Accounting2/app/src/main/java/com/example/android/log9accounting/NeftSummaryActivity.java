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

public class NeftSummaryActivity extends AppCompatActivity {

    private DatabaseReference dbRef;
    private ListView listview;
    ArrayList<String> id=new ArrayList<>();
    ArrayList<String> date=new ArrayList<>();
    ArrayList<String> amount=new ArrayList<>();
    ArrayList<String> description=new ArrayList<>();
    ArrayList<String> type=new ArrayList<>();
    ArrayList<String> image=new ArrayList<>();
    ArrayList<String> category=new ArrayList<>();
    ArrayList<String> head=new ArrayList<>();
    ArrayList<String> reimburse=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_neft_summary);

        dbRef = FirebaseDatabase.getInstance().getReference("NEFT");

        final CustomListNeft adapter = new
                CustomListNeft(NeftSummaryActivity.this, id, date, amount, description, type, image, category, head, reimburse);
        listview = findViewById(R.id.list_neft_summary);

        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot year: dataSnapshot.getChildren()) {
                    if(!year.getKey().equals("Transaction Type")) {
                        Log.d("Year", year.getKey());
                        for(DataSnapshot transactionId: year.getChildren()) {
                            if(!transactionId.getKey().equals("Yearly Counter")) {
                                Log.d("Transaction ID", transactionId.getKey());
                                id.add("Transaction ID: " + transactionId.child("ID").getValue());
                                date.add("Transaction Date: " + transactionId.child("Transaction Date").getValue());
                                amount.add("Amount: " + transactionId.child("Amount").getValue());
                                description.add("Description: " + transactionId.child("Description").getValue());
                                type.add("Transaction Type: " + transactionId.child("Type").getValue());
                                image.add("" + transactionId.child("Image").getValue());
                                category.add("Transaction Category: " + transactionId.child("Category").getValue());
                                head.add("Head: " + transactionId.child("Transaction Head").getValue());
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
