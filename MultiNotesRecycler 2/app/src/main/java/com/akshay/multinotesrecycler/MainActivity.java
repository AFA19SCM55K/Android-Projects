package com.akshay.multinotesrecycler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.JsonReader;
import android.util.JsonWriter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener, Serializable {
    private List<Notes> NotesList = new ArrayList<>();
    private RecyclerView recyclerView;
    private NotesAdapter notesAdapter;
    private int position;
    private static final String TAG = "MainActivity";
    Notes notes;

    private static final int REQUEST_CODE = 22;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.recyclerView);
        notesAdapter = new NotesAdapter(NotesList, this);
        recyclerView.setAdapter(notesAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        loadFile();
    }

    @Override
    protected void onResume() {
        NotesList.size();
        super.onResume();
        notesAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onPause() {
        //Save the NotesList to the Json file
        saveNotes();
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.opt_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.add_item:
                Intent intent_add = new Intent(MainActivity.this,Notes_Type.class);
                startActivityForResult(intent_add,REQUEST_CODE);
                return true;
            case R.id.info_item:
                Intent intent_info = new Intent(MainActivity.this,Info.class);
                startActivity(intent_info);
                return true;
                default:
                    Toast.makeText(getApplicationContext(),"something other than given menu pressed",Toast.LENGTH_SHORT);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Notes edit_note = (Notes) data.getExtras().getSerializable("NOTE");
                Log.d(TAG, "edit_note: "+edit_note);
                String status = data.getStringExtra("STATUS");
                if (status.equals("NO_CHANGE")) {
                    Log.d(TAG, "onActivityResult: "+"No change called");
                } else if (status.equals("CHANGE")) {
                    NotesList.remove(position);
                    NotesList.add(0, edit_note);
                } else if (status.equals("NEW_NOTE")) {
                    NotesList.add(0, edit_note);
                }
            }
        }
    }



    @Override
    public void onClick(View v) {
        position = recyclerView.getChildLayoutPosition(v);
        Toast.makeText(getApplicationContext(),"position"+position,Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(MainActivity.this,Notes_Type.class);
        intent.putExtra("title",NotesList.get(position).getTitle());
        intent.putExtra("description",NotesList.get(position).getDescription());
        intent.putExtra("date",NotesList.get(position).getDate());
        startActivityForResult(intent,REQUEST_CODE);
    }

    @Override
    public boolean onLongClick(View v) {
        position = recyclerView.getChildLayoutPosition(v);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Note");
        builder.setMessage("Do you want to delete \'" + NotesList.get(position).getTitle().toString() + "\' note");
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                NotesList.remove(position);
                notesAdapter.notifyDataSetChanged();
                position = -1;
            }
        });
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                position = -1;
            }

        });

        AlertDialog dialog = builder.create();
        dialog.show();
        return false;
    }

    private Notes loadFile() {

        try {

            InputStream is = this.getApplicationContext().openFileInput(this.getString(R.string.file_name));
            JsonReader reader = new JsonReader(new InputStreamReader(is, this.getString(R.string.encoding)));
            String name;

            reader.beginObject();

            while (reader.hasNext()) {
                name = reader.nextName();
                if (name.equals("notes")) {

                    reader.beginArray();
                    while (reader.hasNext()) {
                        Notes tempNotes = new Notes();
                        reader.beginObject();
                        while(reader.hasNext()) {
                            name = reader.nextName();
                            if (name.equals("title")) {
                                tempNotes.setTitle(reader.nextString());
                            } else if (name.equals("date")) {
                                tempNotes.setDate(reader.nextString());
                            } else if (name.equals("description")) {
                                tempNotes.setDescription(reader.nextString());
                            } else {
                                reader.skipValue();
                            }
                        }
                        reader.endObject();
                        NotesList.add(tempNotes);

                    }
                    reader.endArray();
                }
                else{
                    reader.skipValue();
                }

            }
            reader.endObject();

        }catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error: "+e);
        }
        return null;
    }

    private void saveNotes() {

        try {
            FileOutputStream fos = getApplicationContext().openFileOutput(getString(R.string.file_name), Context.MODE_PRIVATE);

            JsonWriter writer = new JsonWriter(new OutputStreamWriter(fos, getString(R.string.encoding)));
            writer.setIndent("  ");
            writer.beginObject();
            writer.name("notes");
            writeNotesArray(writer);
            writer.endObject();
            writer.close();

//            Toast.makeText(this, getString(R.string.saved), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.getStackTrace();
        }
    }

    public void writeNotesArray(JsonWriter writer) throws IOException {
        writer.beginArray();
        for (Notes value : NotesList) {
            writeNotesObject(writer, value);
        }
        writer.endArray();
    }

    public void writeNotesObject(JsonWriter writer, Notes val) throws IOException{
        writer.beginObject();
        writer.name("title").value(val.getTitle());
        writer.name("date").value(val.getDate());
        writer.name("description").value(val.getDescription());
        writer.endObject();
    }



}
