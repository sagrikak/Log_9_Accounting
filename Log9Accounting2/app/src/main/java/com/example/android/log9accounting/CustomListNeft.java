package com.example.android.log9accounting;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

class CustomListNeft extends BaseAdapter {

    private Context context;
    private ArrayList<String> id, date, amount, description, type, image, category, head, reimburse;

    CustomListNeft(Context context,
                          ArrayList<String> id, ArrayList<String> date, ArrayList<String> amount, ArrayList<String> description, ArrayList<String> type, ArrayList<String> image, ArrayList<String> category, ArrayList<String> head, ArrayList<String> reimburse) {
        this.context = context;
        this.id = id;
        this.date = date;
        this.amount = amount;
        this.description = description;
        this.type = type;
        this.image = image;
        this.category = category;
        this.head = head;
        this.reimburse = reimburse;
    }

    public int getCount() {
        Log.d("list size", String.valueOf(id.size()));
        return id.size();
    }

    public Object getItem(int arg0) {
        return null;
    }

    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        Log.d("position", String.valueOf(position));
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView= inflater.inflate(R.layout.list_single_neft, parent, false);

        TextView txt_id = rowView.findViewById(R.id.txt_id);
        txt_id.setText(id.get(position));
        Log.d("ID", id.get(position));

        TextView txt_date = rowView.findViewById(R.id.txt_date);
        txt_date.setText(date.get(position));

        TextView txt_amount = rowView.findViewById(R.id.txt_amount);
        txt_amount.setText(amount.get(position));

        TextView txt_description = rowView.findViewById(R.id.txt_description);
        txt_description.setText(description.get(position));

        TextView txt_type = rowView.findViewById(R.id.txt_type);
        txt_type.setText(type.get(position));

        TextView txt_category = rowView.findViewById(R.id.txt_category);
        txt_category.setText(category.get(position));

        TextView txt_head = rowView.findViewById(R.id.txt_head);
        txt_head.setText(head.get(position));

        TextView txt_reimburse = rowView.findViewById(R.id.txt_reimburse);
        txt_reimburse.setText(reimburse.get(position));

        ImageView transactionImage = rowView.findViewById(R.id.transactionImage);
        Glide.with(context).load(image.get(position)).placeholder(R.drawable.image_loading).error(R.drawable.image_not_available_2).override(200, 250).into(transactionImage);
        Log.d("ImageUrl", image.get(position));

        transactionImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, NeftImageActivity.class).putExtra("imageUrl", image.get(position));
                intent.putExtra("id", id.get(position));
                intent.putExtra("description", description.get(position));
                intent.putExtra("amount", amount.get(position));
                intent.putExtra("date", date.get(position));
                intent.putExtra("type", type.get(position));
                intent.putExtra("category", category.get(position));
                intent.putExtra("head", head.get(position));
                intent.putExtra("reimburse", reimburse.get(position));

                context.startActivity(intent);
            }
        });

        return rowView;
    }
}
