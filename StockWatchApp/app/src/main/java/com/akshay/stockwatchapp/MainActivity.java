package com.akshay.stockwatchapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener {

    private static final String TAG = "MainActivity";
    int position;
    MainActivity mainActivity;
    ArrayList<Stock> stockArrayList = new ArrayList<>();
    HashMap<String,String> stockHashMap = new HashMap<String, String>();
    RecyclerView recyclerView;
    StockAdapter stockAdapter;
    SwipeRefreshLayout swipeRefreshLayout;
    String searchName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.recyclerView);
        stockAdapter = new StockAdapter(stockArrayList,this);
        recyclerView.setAdapter(stockAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                doRefresh();
            }
        });
       new NameLoader(this).execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu: ");
        getMenuInflater().inflate(R.menu.opt_add,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.add_stock:
                addStock();
                break;
            default:
                Toast.makeText(mainActivity, "User is a wizardx", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    private void doRefresh() {
        Collections.shuffle(stockArrayList);
        stockAdapter.notifyDataSetChanged();
        swipeRefreshLayout.setRefreshing(false);
        Toast.makeText(this, "List content shuffled", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v) {
        Toast.makeText(v.getContext(),"short click detected",Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onLongClick(View v) {
        Toast.makeText(v.getContext(),"Long click detected",Toast.LENGTH_SHORT).show();
        position = recyclerView.getChildLayoutPosition(v);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Stock");
        builder.setMessage("Do you want to delete \'" + stockArrayList.get(position).getName() + "\' Stock");
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                stockArrayList.remove(position);
                stockAdapter.notifyDataSetChanged();
                Toast.makeText(getApplicationContext(), ""+stockArrayList.size(), Toast.LENGTH_SHORT).show();
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
    public void updateData(HashMap<String,String> cList) {
        stockHashMap.putAll(cList);
        for(String key : stockHashMap.keySet()) {
            if(key.startsWith("FB") || stockHashMap.get(key).startsWith("FB")) {
                //DO SOMETHING HERE
                //CODE
            }
        }
        stockAdapter.notifyDataSetChanged();
    }
    public void addStock(){
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
                searchName = et.getText().toString().trim();
                if(searchName.toString().trim().equals("")){
                    Toast.makeText(MainActivity.this, "Please Enter Stock Name", Toast.LENGTH_SHORT).show();
                    addStock();
                }
                else {
                    searchStock(searchName);
                }

                Log.d(TAG, "onClick: symbol:"+searchName);
            }
        });
        builder.setNegativeButton("NO WAY", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Toast.makeText(getApplicationContext(), "Search Operation Cancelled", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setMessage("Please enter a stock name:");
        builder.setTitle("Stock");

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void searchStock(String searchStock)
    {
    ArrayList<Stock> stocksTempList = new ArrayList<>();
        for(String key : stockHashMap.keySet()) {
                if (key.startsWith(searchStock)) {
                    Stock temp = new Stock(key, stockHashMap.get(key));
                    stocksTempList.add(temp);
                }
        }
        Toast.makeText(getApplicationContext(), "FOUND"+stocksTempList.size(), Toast.LENGTH_SHORT).show();
        if(stocksTempList.size()>1)
        {
            selectMultipleStock(stocksTempList);
        }
        else if(stocksTempList.size()==1)
        {
           //
        }
        else
            {

        }
    }

    public void selectMultipleStock(final ArrayList<Stock> stockArrayList){
        final CharSequence[] sArray = new CharSequence[stockArrayList.size()];

        for (int i = 0; i < stockArrayList.size(); i++)
            sArray[i] = stockArrayList.get(i).getSymbol() + " - " + stockArrayList.get(i).getName();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Make a selection");
        builder.setIcon(R.drawable.ic_youtube_searched_for_black_24dp);

        // Set the builder to display the string array as a selectable
        // list, and add the "onClick" for when a selection is made
        builder.setItems(sArray, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Stock temp = stockArrayList.get(which);
                Toast.makeText(MainActivity.this, "You selected"+temp.getSymbol(), Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Nevermind", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Toast.makeText(MainActivity.this, "You changed your mind!", Toast.LENGTH_SHORT).show();

            }
        });
        AlertDialog dialog = builder.create();

        dialog.show();
    }


}
