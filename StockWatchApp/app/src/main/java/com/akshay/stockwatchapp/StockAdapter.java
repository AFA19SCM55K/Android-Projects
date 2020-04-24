package com.akshay.stockwatchapp;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Locale;

public class StockAdapter extends RecyclerView.Adapter<MyViewHolder> {
    private static final String TAG = "StockAdapter";
    private ArrayList<Stock> stockArrayList;
    private MainActivity mainActivity;

    private StockAdapter(ArrayList<Stock> stockArrayList, MainActivity mainActivity)
    {
        this.stockArrayList = stockArrayList;
        this.mainActivity = mainActivity;
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: ");
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.stocks_row,parent,false);
        itemView.setOnClickListener(mainActivity);
        itemView.setOnLongClickListener(mainActivity);
        Log.d(TAG, "onCreateViewHolder: ");
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Stock stock = stockArrayList.get(position);
        try {
            if (stock.getPriceChange() < 0) {
                setRed(holder, stock);
            } else if (stock.getPriceChange() > 0) {
                setGreen(holder, stock);
            }
            setStock(holder, stock);
        }catch (Exception e)
        {
            Toast.makeText(mainActivity, "Invalid Stock Response : 500", Toast.LENGTH_SHORT).show();
        }

    }

    private void setGreen(MyViewHolder holder, Stock stock) {
        holder.name.setTextColor(mainActivity.getResources().getColor(R.color.green));
        holder.symbol.setTextColor(mainActivity.getResources().getColor(R.color.green));
        holder.price.setTextColor(mainActivity.getResources().getColor(R.color.green));
        holder.percentageChange.setTextColor(mainActivity.getResources().getColor(R.color.green));
        holder.priceChange.setTextColor(mainActivity.getResources().getColor(R.color.green));
        holder.imageView.setImageResource(R.drawable.ic_arrow_drop_up_black_24dp);

    }

    private void setRed(MyViewHolder holder, Stock stock) {
        holder.name.setTextColor(mainActivity.getResources().getColor(R.color.red));
        holder.symbol.setTextColor(mainActivity.getResources().getColor(R.color.red));
        holder.price.setTextColor(mainActivity.getResources().getColor(R.color.red));
        holder.percentageChange.setTextColor(mainActivity.getResources().getColor(R.color.red));
        holder.priceChange.setTextColor(mainActivity.getResources().getColor(R.color.red));
        holder.imageView.setImageResource(R.drawable.ic_arrow_drop_down_black_24dp);
    }

    private void setStock(MyViewHolder holder, Stock stock) {
        holder.name.setText(stock.getName());
        holder.symbol.setText(stock.getSymbol());
        holder.price.setText(String.format(Locale.US, "%.2f", stock.getPrice()));
        holder.priceChange.setText(String.format(Locale.US, "%.2f", stock.getPriceChange()));
        holder.percentageChange.setText(String.format(Locale.US, "(%.2f%%)", stock.getChangePercentage()));
    }



    @Override
    public int getItemCount() {
        return stockArrayList.size();
    }
}
