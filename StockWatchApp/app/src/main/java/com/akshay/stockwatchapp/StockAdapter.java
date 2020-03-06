package com.akshay.stockwatchapp;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Locale;

public class StockAdapter extends RecyclerView.Adapter<MyViewHolder> {
    private static final String TAG = "StockAdapter";
    private ArrayList<Stock> stockArrayList;
    private MainActivity mainActivity;

    public StockAdapter(ArrayList<Stock> stockArrayList, MainActivity mainActivity)
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
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Stock stock = stockArrayList.get(position);
        holder.name.setText(stock.getName());
        holder.symbol.setText(stock.getSymbol());
        holder.price.setText(stock.getPrice()+"");
        holder.priceChange.setText(stock.getPriceChange()+"");
        holder.percentageChange.setText(stock.getChangePercentage()+"");
    }

    @Override
    public int getItemCount() {
        return stockArrayList.size();
    }
}
