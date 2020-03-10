package com.akshay.stockwatchapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.text.LoginFilter;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener {

    private static final String TAG = "MainActivity";
    int position;
    MainActivity mainActivity;
    ArrayList<Stock> stockArrayList = new ArrayList<>();
    HashMap<String, String> stockHashMap = new HashMap<String, String>();
    RecyclerView recyclerView;
    StockAdapter stockAdapter;
    SwipeRefreshLayout swipeRefreshLayout;
    String searchName = "";
    private DatabaseHandler databaseHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.recyclerView);
        stockAdapter = new StockAdapter(stockArrayList, this);
        recyclerView.setAdapter(stockAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(checkNetwork()) {
                    doRefresh();
                }else {
                    errorNetworkDialog();
                }
            }
        });
        databaseHandler = new DatabaseHandler(this);


    }

    private void errorNetworkDialog() {
        // Simple dialog - no buttons.
        swipeRefreshLayout.setRefreshing(false);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setIcon(R.drawable.ic_error_outline_black_24dp);

        builder.setMessage("Check your internet connection");
        builder.setTitle("Netowork Error");

        AlertDialog dialog = builder.create();
        dialog.show();

    }

    @Override
    protected void onResume() {
        if (checkNetwork()) {
            Log.i("NetworkInfo: bp:", "Network is connected");
            new NameLoader(this).execute();
        } else {
            errorNetworkDialog();
            Log.i("NetoworkInfo: bp:", "Network is not connected");
        }
        databaseHandler.dumpDbToLog();
        ArrayList<Stock> list = databaseHandler.loadCountries();
        stockArrayList.clear();
//        stockArrayList.addAll(sortList(list));
        Log.d(TAG, "onResume: dp" + sortList(list));
        Log.d(TAG, "onResume: dp:" + stockArrayList);
        Log.d(TAG, "onResume: " + list);
        for (int i = 0; i < list.size(); i++) {
            String symbol = list.get(i).getSymbol();
            Log.d(TAG, "onResume: fg:" + symbol);
            new StockLoader(MainActivity.this).execute(symbol);
        }
//        stockAdapter.notifyDataSetChanged();
        super.onResume();
    }

    ArrayList<Stock> sortList(ArrayList<Stock> temp) {
        Collections.sort(stockArrayList, new Comparator<Stock>() {
            @Override
            public int compare(Stock o1, Stock o2) {
                return o1.getSymbol().compareTo(o2.getSymbol());
            }
        });
        return temp;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu: ");
        getMenuInflater().inflate(R.menu.opt_add, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onDestroy() {
        databaseHandler.shutDown();
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_stock:
                if (checkNetwork()) {
                    addStock();
                } else {
                    errorNetworkDialog();
                }
                break;
            default:
                Toast.makeText(mainActivity, "User is a wizardx", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    private void doRefresh() {
        if(stockHashMap.isEmpty()){
            new NameLoader(this).execute();
        }
        swipeRefreshLayout.setRefreshing(false);
        databaseHandler.dumpDbToLog();
        ArrayList<Stock> list = databaseHandler.loadCountries();
        stockArrayList.clear();
        for (int i = 0; i < list.size(); i++) {
            String symbol = list.get(i).getSymbol();
            Log.d(TAG, "onResume: fg:" + symbol);
            new StockLoader(MainActivity.this).execute(symbol);
        }


    }

    @Override
    public void onClick(View v) {
        int i = recyclerView.getChildLayoutPosition(v);
        String marketPlaceURL = "http://www.marketwatch.com/investing/stock/" + stockArrayList.get(i).getSymbol();
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(marketPlaceURL));
        startActivity(intent);
    }

    @Override
    public boolean onLongClick(View v) {
        Toast.makeText(v.getContext(), "Long click detected", Toast.LENGTH_SHORT).show();
        position = recyclerView.getChildLayoutPosition(v);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Stock");
        builder.setMessage("Do you want to delete \'" + stockArrayList.get(position).getName() + "\' Stock");
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                databaseHandler.deleteStock(stockArrayList.get(position));
                databaseHandler.dumpDbToLog();
                stockArrayList.remove(position);
                stockArrayList = sortList(stockArrayList);
                stockAdapter.notifyDataSetChanged();
                Toast.makeText(getApplicationContext(), "" + stockArrayList.size(), Toast.LENGTH_SHORT).show();
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

    public void updateData(HashMap<String, String> cList) {
        if (cList != null) {
            stockHashMap.putAll(cList);
        } else {
            checkNetwork();
            Toast.makeText(this, "No data", Toast.LENGTH_SHORT).show();
        }
        for (String key : stockHashMap.keySet()) {
            if (key.startsWith("FB") || stockHashMap.get(key).startsWith("FB")) {
                //DO SOMETHING HERE
                //CODE
            }
        }
        stockAdapter.notifyDataSetChanged();
    }

    public void addStock() {
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
                if (searchName.toString().trim().equals("")) {
                    Toast.makeText(MainActivity.this, "Please Enter Stock Name", Toast.LENGTH_SHORT).show();
                    addStock();
                } else {
                    if (checkNetwork()) {
                        Log.i("NetworkInfo: bp:", "Network is connected");
                        Log.d(TAG, "onClick: searchstock:" + searchName);
                        searchStock(searchName);
                    } else {
                        errorNetworkDialog();
                        Log.i("NetoworkInfo: bp:", "Network is not connected");
                    }

                }

                Log.d(TAG, "onClick: symbol:" + searchName);
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

    public void searchStock(String searchStock) {
        ArrayList<Stock> stocksTempList = new ArrayList<>();
        for (String key : stockHashMap.keySet()) {
            if (key.startsWith(searchStock) || stockHashMap.get(key).contains(searchStock)) {
                Stock temp = new Stock(key, stockHashMap.get(key));
                Log.d(TAG, "searchStock: temp:" + temp.getSymbol());
                stocksTempList.add(temp);
            }
        }
        Toast.makeText(getApplicationContext(), "FOUND " + stocksTempList.size(), Toast.LENGTH_SHORT).show();
        if (stocksTempList.size() > 1) {
            selectMultipleStock(stocksTempList);
        } else if (stocksTempList.size() == 1) {
            if (isDuplicate(stocksTempList.get(0)) && stocksTempList.get(0).getSymbol() != null) {
                saveToDB(stocksTempList.get(0));
                databaseHandler.dumpDbToLog();
            }
        } else {
            Toast.makeText(this, "Stock does not exists", Toast.LENGTH_SHORT).show();
            errorDoesNotExistsDialog(searchStock);

        }
    }


    public Boolean isDuplicate(Stock stock) {
        for (Stock s : stockArrayList) {
            if (stock != null) {
                if (s.getSymbol() != null && stock.getSymbol() != null && s.getSymbol().equals(stock.getSymbol())) {
                    Log.d(TAG, "saveToDB: bp:" + s.getSymbol());
                    Log.d(TAG, "saveToDB: bp:" + stock.getSymbol());
                    Toast.makeText(this, "STOCK ALREADY PRESENT", Toast.LENGTH_SHORT).show();
                    errorDuplicateDialog();
                    return false;
                }
            } else {
                Toast.makeText(this, "No data available", Toast.LENGTH_SHORT).show();
            }
            //something here
        }
        return true;
    }

    private void saveToDB(Stock stock) {
        Log.d(TAG, "saveToDB: bp" + stockArrayList);
        Log.d(TAG, "saveToDB: bp" + stock);
        databaseHandler.addStocks(stock);
        new StockLoader(MainActivity.this).execute(stock.getSymbol());
        stockAdapter.notifyDataSetChanged();
    }

    public void selectMultipleStock(final ArrayList<Stock> stockArrayList) {
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
                if (isDuplicate(temp)) {
                    saveToDB(temp);
                }
//                } else {
//                    errorDuplicateDialog();
//                }

                Toast.makeText(MainActivity.this, "You selected" + temp.getSymbol(), Toast.LENGTH_SHORT).show();
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

    private boolean checkNetwork() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            Toast.makeText(this, "Check your Internet Connection", Toast.LENGTH_SHORT).show();
            return false;
        }
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }


//        public void setStock(Stock stock) {
//        ArrayList<Stock> temp = new ArrayList<>();
//        temp.addAll(stockArrayList);
//
//            for(Stock s:temp){
//                Log.d(TAG, "setStock: ");
//                if(s!=null && stock!=null && s.getSymbol().equalsIgnoreCase(stock.getSymbol())){
//                    temp.remove(s);
//                }
//                else
//                {
//                    Toast.makeText(this, "No data available", Toast.LENGTH_SHORT).show();
//                }
//            }
//            temp.add(stock);
//            stockArrayList.clear();
//            stockArrayList.addAll(temp);
//            stockArrayList = sortList(stockArrayList);
//            stockAdapter.notifyDataSetChanged();
//        }
    public void setStock(Stock stock) {
        if (stock != null) {
            Log.d(TAG, "setStock: In Stock !=null condition");
            int index = stockArrayList.indexOf(stock);
            if (index > -1) {
                Log.d(TAG, "setStock: In Stock index");
                stockArrayList.remove(index);
            }
            stockArrayList.add(stock);
            stockArrayList = sortList(stockArrayList);
            stockAdapter.notifyDataSetChanged();
        }
    }

    public void errorDuplicateDialog() {
        // Simple dialog - no buttons.

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setIcon(R.drawable.ic_error_outline_black_24dp);

        builder.setMessage("Duplicate Stock");
        builder.setTitle("Stock Already Exists");

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void errorDoesNotExistsDialog(String s) {
        // Simple dialog - no buttons.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setIcon(R.drawable.ic_error_outline_black_24dp);

        builder.setMessage("Stock \'"+ s +"\' Does Not Exists");
        builder.setTitle("Stock Selection");

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}

