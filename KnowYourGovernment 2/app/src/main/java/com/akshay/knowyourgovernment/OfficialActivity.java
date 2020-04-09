package com.akshay.knowyourgovernment;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class OfficialActivity extends AppCompatActivity {
    private static final String TAG = "OfficialActivity";

    private TextView name, title, party, address, url, email, phone, location;
    private ImageView party_logo, image_logo, googleplus_imageview, twitter_imageview, facebook_imageview, youtube_imageview;
    private TextView lbl_address,lbl_email,lbl_url, lbl_phone;
    ArrayList<Channel> channels = new ArrayList<>();
    Channel googleplus, twitter, facebook, youtube;
    String partyname_click = "";
    Official temp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_official);
        initializeComponents();
        setLocation();
        fillData();

    }

    private void initializeComponents() {
        name = findViewById(R.id.name);
        title = findViewById(R.id.title);
        party = findViewById(R.id.party);
        phone = findViewById(R.id.phone);
        address = findViewById(R.id.address);
        url = findViewById(R.id.url);
        email = findViewById(R.id.email);
        location = findViewById(R.id.location);
        lbl_address = findViewById(R.id.lbl_address);
        lbl_email = findViewById(R.id.lbl_email);
        lbl_url = findViewById(R.id.lbl_url);
        lbl_phone = findViewById(R.id.lbl_phone);

        //party and candidate image
        party_logo = findViewById(R.id.party_imageview);
        image_logo = findViewById(R.id.photoURL);


        //social media icons
        googleplus_imageview = findViewById(R.id.googleplus_imageview);
        facebook_imageview = findViewById(R.id.facebook_imageview);
        twitter_imageview = findViewById(R.id.twitter_imageview);
        youtube_imageview = findViewById(R.id.youtube_imageview);


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

                temp = (Official) intent.getExtras().getSerializable("official");
                title.setText(temp.getTitle());
                name.setText(temp.getName());
                party.setText(temp.getParty());




                //phone start
                if(!temp.getPhones().equals("")) {
                    phone.setText(temp.getPhones());
                    phone.setLinkTextColor(Color.WHITE);
                    Linkify.addLinks(phone, Linkify.ALL);
                }
                else {
                    lbl_phone.setVisibility(View.GONE);
                    phone.setVisibility(View.GONE);

                }
                //end

                //address
            if(!temp.getAddress().equals("")) {
                address.setText(temp.getAddress());
                address.setLinkTextColor(Color.WHITE);
                Linkify.addLinks(address, Linkify.ALL);
            }else {
                lbl_address.setVisibility(View.GONE);
                address.setVisibility(View.GONE);
            }
            //address end


            if(!temp.getEmails().equals("")) {
                email.setLinkTextColor(Color.WHITE);
                email.setText(temp.getEmails().toLowerCase().trim());
                Linkify.addLinks(email, Linkify.ALL);
            }else {
                lbl_email.setVisibility(View.GONE);
                email.setVisibility(View.GONE);
            }


            if(!temp.getUrl().equals("")) {
                url.setText(temp.getUrl());
                url.setLinkTextColor(Color.WHITE);
                Linkify.addLinks(url, Linkify.ALL);
            }else {
                lbl_url.setVisibility(View.GONE);
                url.setVisibility(View.GONE);
            }
            //url end


                if (temp.getParty().toLowerCase().trim().contains("democratic")) {
                    party_logo.setImageResource(R.drawable.dem_logo);
                    getWindow().getDecorView().setBackgroundColor(Color.BLUE);
                    partyname_click = "dem";
                } else if (temp.getParty().toLowerCase().trim().contains("republican")) {
                    party_logo.setImageResource(R.drawable.rep_logo);
                    getWindow().getDecorView().setBackgroundColor(Color.RED);
                    partyname_click = "rep";
                } else {
                    partyname_click = "";
                    party_logo.setImageResource(R.drawable.non);
                    getWindow().getDecorView().setBackgroundColor(Color.BLACK);
                }
                loadRemoteImage(temp.getPhotoURL());

                channels = temp.getChannels();
                if (channels.size() < 1) {
                    //list is empty show no imageviews


                } else {
                    //do code.
                    for (Channel channel : channels) {
                        if (channel.getType().toLowerCase().contains("facebook")) {
                            facebook_imageview.setVisibility(View.VISIBLE);
                            facebook = channel;

                        }
                        if (channel.getType().toLowerCase().contains("twitter")) {

                            twitter_imageview.setVisibility(View.VISIBLE);
                            twitter = channel;

                        }
                        if (channel.getType().toLowerCase().contains("googleplus")) {
                            googleplus_imageview.setVisibility(View.VISIBLE);
                            googleplus = channel;

                        }
                        if (channel.getType().toLowerCase().contains("youtube")) {
                            youtube_imageview.setVisibility(View.VISIBLE);
                            youtube = channel;

                        }
                    }

                }


        }
    }

    //handle twitter click
    public void twitterClicked(View v) {
        String name = twitter.getId();
        Intent intent = null;
        try {
            getPackageManager().getPackageInfo("com.twitter.android", 0);
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("twitter://user?screen_name=" + name));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        } catch (Exception e) {
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/" + name));
        }
        startActivity(intent);
    }

    public void facebookClicked(View v) {
        String id = facebook.getId();
        String FACEBOOK_URL = "https://www.facebook.com/" + id;
        String urlToUse;
        PackageManager packageManager = getPackageManager();
        try {
            int versionCode = packageManager.getPackageInfo("com.facebook.katana", 0).versionCode;
            if (versionCode >= 3002850) { //newer versions of fb app
                urlToUse = "fb://facewebmodal/f?href=" + FACEBOOK_URL;
            } else { //older versions of fb app
                urlToUse = "fb://page/" + id;
            }
        } catch (PackageManager.NameNotFoundException e) {
            urlToUse = FACEBOOK_URL; //normal web url
        }
        Intent facebookIntent = new Intent(Intent.ACTION_VIEW);
        facebookIntent.setData(Uri.parse(urlToUse));
        startActivity(facebookIntent);
    }


    public void googlePlusClicked(View v) {
        String name = googleplus.getId();
        Intent intent = null;
        try {
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setClassName("com.google.android.apps.plus", "com.google.android.apps.plus.phone.UrlGatewayActivity");
            intent.putExtra("customAppUri", name);
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://plus.google.com/" + name)));
        }
    }

    public void youTubeClicked(View v) {
        String name = youtube.getId();
        Intent intent = null;
        try {
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setPackage("com.google.android.youtube");
            intent.setData(Uri.parse("https://www.youtube.com/" + name));
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/" + name)));
        }
    }

    //load images and fill
    private void loadRemoteImage(final String imageURL) {
        Log.d(TAG, "loadImage: " + imageURL);

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

    public void showPhotoDetails(View view){
        if(temp.getPhotoURL().equals("") || temp.getPhotoURL()==null)
        {
            Toast.makeText(this, "Image does not exists", Toast.LENGTH_SHORT).show();
        }else {
            Intent intent = new Intent(this,PhotoDetail.class);
            intent.putExtra("location",location.getText());
            intent.putExtra("official",temp);
            startActivity(intent);
        }

    }

}
