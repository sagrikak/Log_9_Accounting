package com.example.android.log9accounting;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

public class NeftImageActivity extends AppCompatActivity {
    private String id, date, amount, description, type, url, category, head, reimburse;
    private ImageView transactionImage;
    private TextView txt_id, txt_date, txt_amount, txt_description, txt_type, txt_category, txt_head, txt_reimburse;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_neft_image);

        url = getIntent().getStringExtra("imageUrl");
        id = getIntent().getStringExtra("id");
        date = getIntent().getStringExtra("date");
        amount = getIntent().getStringExtra("amount");
        description = getIntent().getStringExtra("description");
        type = getIntent().getStringExtra("type");
        category = getIntent().getStringExtra("category");
        head = getIntent().getStringExtra("head");
        reimburse = getIntent().getStringExtra("reimburse");

        transactionImage = findViewById(R.id.transactionImage);
        txt_id = findViewById(R.id.txt_id);
        txt_date = findViewById(R.id.txt_date);
        txt_amount = findViewById(R.id.txt_amount);
        txt_description = findViewById(R.id.txt_description);
        txt_type = findViewById(R.id.txt_type);
        txt_category = findViewById(R.id.txt_category);
        txt_head = findViewById(R.id.txt_head);
        txt_reimburse = findViewById(R.id.txt_reimburse);

        Glide.with(this)
                .load(url)
                .placeholder(R.drawable.image_loading)
                .error(R.drawable.image_not_available_2)
                .into(transactionImage);

        txt_id.setText(id);
        txt_date.setText(date);
        txt_amount.setText(amount);
        txt_description.setText(description);
        txt_type.setText(type);
        txt_category.setText(category);
        txt_head.setText(head);
        txt_reimburse.setText(reimburse);
    }

    @Override
    public void onBackPressed()
    {
        Intent intent = new Intent(this, NeftSummaryActivity.class);
        startActivity(intent);
        finish();
    }
}
