package com.akshay.knowyourgovernment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class OfficialAdapter extends RecyclerView.Adapter<MyViewHolder> {
    private MainActivity mainActivity;
    private ArrayList<Official> officialsArrayList;

    public OfficialAdapter(MainActivity mainActivity, ArrayList<Official> officialsArrayList) {
        this.mainActivity = mainActivity;
        this.officialsArrayList = officialsArrayList;
    }

    @NonNull
    @Override

    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.official_row,parent,false);
        itemView.setOnClickListener(mainActivity);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Official temp = officialsArrayList.get(position);
        holder.title.setText(temp.getTitle());
        holder.name.setText(temp.getName());
        holder.party.setText(temp.getParty());
    }

    @Override
    public int getItemCount() {
        return officialsArrayList.size();
    }
}
