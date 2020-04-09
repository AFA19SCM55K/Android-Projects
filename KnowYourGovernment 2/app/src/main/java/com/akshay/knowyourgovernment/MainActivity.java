package com.akshay.knowyourgovernment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private RecyclerView recyclerView;
    public TextView location;
    public TextView errorText;
    private ArrayList<Official> officialArrayList = new ArrayList<>();
    private OfficialAdapter officialAdapter;
    private static final String TAG = "MainActivity";

    private static int MY_LOCATION_REQUEST_CODE_ID = 329;
    private LocationManager locationManager;
    private Criteria criteria;
    Location currentLocation;
    SwipeRefreshLayout swiper;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializaComponents();
        swiper.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (checkNetwork()) {
                    doStuff();
                } else {
                    errorNetworkDialog();
                }
                errorText.setVisibility(View.GONE);
                swiper.setRefreshing(false);
            }
        });

        officialAdapter = new OfficialAdapter(this, officialArrayList);
        recyclerView.setAdapter(officialAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        if (checkNetwork()) {
            doStuff();
        } else {
            errorNetworkDialog();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void doStuff() {
        ArrayList<Official> temp = new ArrayList<>();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        criteria = new Criteria();
        criteria.setPowerRequirement(Criteria.POWER_HIGH);
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setSpeedRequired(false);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION
                    },
                    MY_LOCATION_REQUEST_CODE_ID);
        } else {
            Toast.makeText(this, "Location already enabled", Toast.LENGTH_SHORT).show();
            setLocation();
        }
    }
    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull
            String[] permissions, @NonNull
                    int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == MY_LOCATION_REQUEST_CODE_ID) {
            if (permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION) &&
                    grantResults[0] == PERMISSION_GRANTED) {

                errorText.setVisibility(View.GONE);
                setLocation();
                return;
            }
            else {
                location.setText("Location Unavailable");
                errorLocationDialog();
            }
        }

    }

    @SuppressLint("MissingPermission")
    private void setLocation() {
        Toast.makeText(this, "In Setup location", Toast.LENGTH_SHORT).show();
        String bestProvider = locationManager.getBestProvider(criteria, true);
        currentLocation = locationManager.getLastKnownLocation(bestProvider);
        if (currentLocation != null) {
            doLocationName(currentLocation);
        } else {
           location.setText("Location Unavailable");
        }
    }
    public void doLocationName(Location currentLocation) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses;
            addresses = geocoder.getFromLocation(currentLocation.getLatitude(),currentLocation.getLongitude(), 1);
            if(!addresses.get(0).getPostalCode().equals("") && addresses.get(0).getPostalCode()!=null){
                doOfficialLoader(addresses.get(0).getPostalCode());
            }
            else if(!addresses.get(0).getAdminArea().equals("") && addresses.get(0).getAdminArea()!=null){
                doOfficialLoader(addresses.get(0).getAdminArea());
            }
            else if(!addresses.get(0).getCountryName().equals("") && addresses.get(0).getCountryName()!=null){
                doOfficialLoader(addresses.get(0).getCountryName());
            }
            else if(!addresses.get(0).getAddressLine(0).equals("") && addresses.get(0).getAddressLine(0)!=null){
                doOfficialLoader(addresses.get(0).getAddressLine(0));
            }
            else if(!addresses.get(0).getLocality().equals("") && addresses.get(0).getLocality()!=null){
                doOfficialLoader(addresses.get(0).getLocality());
            }


        } catch (IOException e) {
            Toast.makeText(this, e.getMessage().toUpperCase(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void doOfficialLoader(String paramter) {
        new OfficialLoader(this).execute(paramter);
    }

    public void doOfficialData(ArrayList<Official> tempList) {
        officialArrayList.clear();

        if(tempList.size()!=0){
            errorText.setVisibility(View.GONE);
            officialArrayList.addAll(tempList);
        }else {
            errorText.setVisibility(View.VISIBLE);
            location.setText("Location Unavailable");
        }
        officialAdapter.notifyDataSetChanged();

    }

    public void initializaComponents() {
        errorText = findViewById(R.id.errorText);
        recyclerView = findViewById(R.id.recyclerView);
        location = findViewById(R.id.location);
        errorText = findViewById(R.id.errorText);
        swiper = findViewById(R.id.swiper);
    }

    private boolean checkNetwork() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            Toast.makeText(this, "Check your Internet Connection", Toast.LENGTH_SHORT).show();
            return false;
        }
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    public void errorNetworkDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.ic_error_outline_black_24dp);
        builder.setMessage("Check your internet connection");
        builder.setTitle("Network Error");
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    public void errorLocationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.ic_error_outline_black_24dp);
        builder.setMessage("Check your Location Settings");
        builder.setTitle("Location Error");
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onClick(View v) {
        int position = recyclerView.getChildAdapterPosition(v);
        Official temp = officialArrayList.get(position);
        Toast.makeText(this, "" + temp.getName(), Toast.LENGTH_SHORT).show();
        Intent i = new Intent(MainActivity.this,OfficialActivity.class);
        i.putExtra("location",location.getText().toString());
        i.putExtra("official",temp);
        MainActivity.this.startActivity(i);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu: ");
        getMenuInflater().inflate(R.menu.opt_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search:
                if (checkNetwork()) {
                    errorText.setVisibility(View.GONE);
                    Toast.makeText(this, "Search clicked", Toast.LENGTH_SHORT).show();
                    showSearchDialog();
                } else {
                    errorNetworkDialog();
                }
                break;
            case R.id.about:
                Intent intent = new Intent(this,about.class);
                startActivity(intent);
                break;
            default:
                Toast.makeText(getApplicationContext(), "User is a wizard", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    private void showSearchDialog() {

        // Single input value dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Create an edittext and set it to be the builder's view
        final EditText et = new EditText(this);
        et.setInputType(InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        et.setGravity(Gravity.CENTER_HORIZONTAL);
        builder.setView(et);

        builder.setIcon(R.drawable.ic_youtube_searched_for_black_24dp);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Log.d(TAG, "onClick: symbol:");
                try{
                    new OfficialLoader(MainActivity.this).execute(et.getText().toString().trim().replaceAll("\\s",""));

                }catch (Exception e){
                    Log.d(TAG, "onClick: bp:"+"in catch block");
                    errorText = findViewById(R.id.errorText);
                    errorText.setVisibility(View.VISIBLE);
                    errorText.setText("hello");
                }
            }
        });
        builder.setNegativeButton("NO WAY", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Toast.makeText(getApplicationContext(), "Search Operation Cancelled", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setMessage("Please enter a city/zip/state:");
        builder.setTitle("Stock");

        AlertDialog dialog = builder.create();
        dialog.show();
    }


}


