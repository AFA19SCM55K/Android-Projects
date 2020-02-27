package com.akshay.multinotesrecycler;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import java.io.Serializable;
import java.util.Calendar;

public class Notes_Type extends AppCompatActivity implements Serializable {
    private static final String TAG = "Notes_Type";
    private EditText title_txt;
    private EditText description_txt;
    private String oldTitle = "",oldDescription="";
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        intent = getIntent();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes__type);
        title_txt = findViewById(R.id.notes_title);
        description_txt = findViewById(R.id.description);
        description_txt.setGravity(Gravity.TOP);
        description_txt.setVerticalScrollBarEnabled(true);
        description_txt.setMovementMethod(new ScrollingMovementMethod());

        if(getIntent().hasExtra("title"))
        {
            oldTitle = getIntent().getStringExtra("title");
            title_txt.setText(getIntent().getStringExtra("title"));

        }
        if(getIntent().hasExtra("description"))
        {
            oldDescription = getIntent().getStringExtra("description");
            description_txt.setText(getIntent().getStringExtra("description"));

        }
    }

    @Override
    protected void onPause() {
        checkNote();
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.opt_save,menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.save:
                Toast.makeText(this,"save pressed",Toast.LENGTH_LONG).show();
                checkNote();
                break;
                default:
                    Toast.makeText(this, "something other pressed", Toast.LENGTH_SHORT).show();

        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onBackPressed(){
        if(title_txt.getText().toString().isEmpty())
        {
            Toast.makeText(this, "Empty Notes Cannot be Saved", Toast.LENGTH_SHORT).show();
            super.onBackPressed();
        }
        else if(title_txt.getText().toString().isEmpty() && description_txt.getText().toString().isEmpty())
        {
            Toast.makeText(this, "No Content", Toast.LENGTH_SHORT).show();
        }
        else if(title_txt.getText().toString().trim().equalsIgnoreCase(oldTitle) && description_txt.getText().toString().trim().equalsIgnoreCase(oldDescription))
        {
            Toast.makeText(this, "No Change", Toast.LENGTH_SHORT).show();
            super.onBackPressed();
        }
        else
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Do you want to save this note");
            builder.setTitle("Save Note");
            builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    checkNote();
                }
            });
            builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }


    public void checkNote()
    {
        Notes tempNote = new Notes();
        tempNote.setTitle(title_txt.getText().toString().trim());
        tempNote.setDescription(description_txt.getText().toString().trim());
        tempNote.setDate(Calendar.getInstance().getTime().toString());
        Log.d(TAG, "checkNote: "+tempNote.getDate());
        Intent passIntent = new Intent();
        passIntent.putExtra("NOTE", tempNote);

        if(title_txt.getText().toString().matches("")|| title_txt.getText().toString().trim()==null){
            Toast.makeText(getApplicationContext(),"Note not saved",Toast.LENGTH_LONG).show();
            passIntent.putExtra("STATUS","NO_CHANGE");
        }
        else if(oldTitle.isEmpty() && oldDescription.isEmpty())
        {
            passIntent.putExtra("STATUS","NEW_NOTE");
            Toast.makeText(this, "NEW_NOTE", Toast.LENGTH_SHORT).show();
        }
        else if(title_txt.getText().toString().isEmpty())
        {
            passIntent.putExtra("STATUS","NO_CHANGE");
            Toast.makeText(this, "No Title No Save", Toast.LENGTH_SHORT).show();
        }
        else if(title_txt.getText().toString().trim().equals(oldTitle) && description_txt.getText().toString().trim().equals(oldDescription)){
            passIntent.putExtra("STATUS","NO_CHANGE");
        }
        else
        {
            passIntent.putExtra("STATUS", "CHANGE");
        }
        setResult(RESULT_OK,passIntent);
        finish();
    }
}
