package com.akshay.knowyourgovernment;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

public class PhotoDetail extends AppCompatActivity {

    private TextView name, title, party, location;
    private ImageView party_logo, image_logo;
    String partyname_click="";
//    Official temp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_detail);
        initializeComponents();
        setLocation();
        fillData();
    }

    private void initializeComponents() {
        name = findViewById(R.id.name);
        title = findViewById(R.id.title);
        party = findViewById(R.id.party);
        party_logo = findViewById(R.id.party_imageview);
        image_logo = findViewById(R.id.photoURL);
        location = findViewById(R.id.location);
    }

    void setLocation() {
        if (getIntent().hasExtra("location")) {
            location.setText(getIntent().getStringExtra("location"));
        } else {
            location.setText("");
        }
    }

    void fillData() {
        Intent intent = getIntent();
        Bundle extras = getIntent().getExtras();
        if (intent.hasExtra("official")) {

            Official temp = (Official) intent.getExtras().getSerializable("official");
            title.setText(temp.getTitle());
            name.setText(temp.getName());
            party.setText(temp.getParty());

            if (temp.getParty().toLowerCase().trim().contains("democratic")) {

                //set navigation color
                getWindow().setNavigationBarColor(Color.BLUE);
                //
                party_logo.setImageResource(R.drawable.dem_logo);
                getWindow().getDecorView().setBackgroundColor(Color.BLUE);
                partyname_click="demo";
            } else if (temp.getParty().toLowerCase().trim().contains("republican")) {
                getWindow().setNavigationBarColor(Color.RED);
                party_logo.setImageResource(R.drawable.rep_logo);
                getWindow().getDecorView().setBackgroundColor(Color.RED);
                partyname_click="rep";
            } else {
                party_logo.setImageResource(R.drawable.non);
                getWindow().getDecorView().setBackgroundColor(Color.BLACK);
                partyname_click="";
            }
            loadRemoteImage(temp.getPhotoURL());
        }
    }

    private void loadRemoteImage(final String imageURL) {

        Picasso picasso = new Picasso.Builder(this).build();
        if (imageURL.equals("")) {
            picasso.load(R.drawable.missing).into(image_logo);
        } else {
            picasso.load(imageURL)
                    .error(R.drawable.brokenimage)
                    .placeholder(R.drawable.placeholder)
                    .into(image_logo);
        }
    }

    public void party_logoclick(View v){
        if(partyname_click.equalsIgnoreCase("dem")){
            String marketPlaceURL = "https://democrats.org/";
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(marketPlaceURL));
            startActivity(intent);
        }
        else if(partyname_click.equalsIgnoreCase("rep")){
            String marketPlaceURL = "https://gop.com/";
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(marketPlaceURL));
            startActivity(intent);
        }
        else{
            Toast.makeText(this, "Party site does not exists", Toast.LENGTH_SHORT).show();
        }
    }
}
