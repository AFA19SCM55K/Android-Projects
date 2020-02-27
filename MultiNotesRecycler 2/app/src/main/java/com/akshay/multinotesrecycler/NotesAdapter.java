package com.akshay.multinotesrecycler;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Calendar;
import java.util.List;

class NotesAdapter extends RecyclerView.Adapter<MyViewHolder> {
    private List<Notes> notesList;
    private MainActivity mainActivity;
    private static final String TAG = "NotesAdapter";

    public NotesAdapter(List<Notes> notesList,MainActivity mainActivity)
    {
        this.notesList = notesList;
        this.mainActivity = mainActivity;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        Log.d(TAG, "onCreateViewHolder: ");
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.notes_row,viewGroup,false);
        itemView.setOnClickListener(mainActivity);
        itemView.setOnLongClickListener(mainActivity);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: ");
        Log.d(TAG, "notelist: " + notesList);
        Notes note = notesList.get(position);
        holder.title.setText(note.getTitle());
        holder.timestamp.setText(Calendar.getInstance().getTime()+"");
        holder.note.setText("Description"+(Calendar.getInstance().getTimeZone()));
        if(note.getDescription().length() > 80){
            String new_Note = note.getDescription().substring(0, 79);
            new_Note = new_Note.concat("...");
            holder.note.setText(new_Note);
        }
        else {
            holder.note.setText(note.getDescription());
        }
    }

    @Override
    public int getItemCount() {
        Log.d(TAG, "getItemCount: ");
        mainActivity.getSupportActionBar().setTitle("Multi Notes "+"("+notesList.size()+")");
        Log.d(TAG, "getItemCount: "+notesList.size());
        return notesList.size();
    }


}
