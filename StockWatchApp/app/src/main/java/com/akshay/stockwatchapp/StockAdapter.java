package com.akshay.stockwatchapp;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class StockAdapter extends RecyclerView.Adapter<MyViewHolder>{
    private static final String TAG = "StockAdapter";
    private ArrayList<Stock> stockArrayList;
    private MainActivity mainActivity;

    public StockAdapter(ArrayList<Stock> stockArrayList, MainActivity mainActivity) {
        this.stockArrayList = stockArrayList;
        this.mainActivity = mainActivity;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: ");
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: ");

    }

    @Override
    public int getItemCount() {
        Log.d(TAG, "getItemCount: ");
        return 0;
    }
}
