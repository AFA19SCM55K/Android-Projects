package com.akshay.multinotesrecycler;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

class MyViewHolder extends RecyclerView.ViewHolder {

    public TextView title;
    public TextView note;
    public TextView timestamp;

    public MyViewHolder(@NonNull View itemView) {
        super(itemView);
        title = itemView.findViewById(R.id.title_txt);
        note = itemView.findViewById(R.id.description_txt);
        timestamp = itemView.findViewById(R.id.timestamp_txt);

    }
}
