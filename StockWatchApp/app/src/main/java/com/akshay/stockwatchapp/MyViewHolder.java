package com.akshay.stockwatchapp;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MyViewHolder extends RecyclerView.ViewHolder {
    public TextView name;
    TextView symbol;
    TextView price;
    TextView priceChange;
    TextView percentageChange;
    ImageView imageView;
    MyViewHolder(@NonNull View itemView) {
        super(itemView);
        name = itemView.findViewById(R.id.name);
        symbol = itemView.findViewById(R.id.symbol);
        price = itemView.findViewById(R.id.price);
        percentageChange = itemView.findViewById(R.id.percentageChange);
        priceChange = itemView.findViewById(R.id.priceChange);
        imageView = itemView.findViewById(R.id.status_arrow);

    }
}
