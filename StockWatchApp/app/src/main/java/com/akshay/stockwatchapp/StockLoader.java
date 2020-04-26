package com.akshay.stockwatchapp;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.LongDef;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

public class StockLoader extends AsyncTask<String, Void, String> {


    private static final String TAG = "StockLoader";
    private String DATA_URL = "https://cloud.iexapis.com/stable/stock/";
    @SuppressLint("StaticFieldLeak")
    private MainActivity mainActivity;

    StockLoader(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }



    @Override
    protected String doInBackground(String... strings) {
        String URL_FOR_STOCK = DATA_URL + strings[0] + "/quote?token="+ BuildConfig.API_KEY;
        Uri uri = Uri.parse(URL_FOR_STOCK);
        String url_string = uri.toString();
        StringBuilder sb = new StringBuilder();
        try {
            URL url = new URL(url_string);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            InputStream inputStream = connection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while((line = bufferedReader.readLine())!=null){
                sb.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    @Override
    protected void onPostExecute(String s) {
        Log.d(TAG, "onPostExecute: bp:"+s);
        Stock stock = jsonToMap(s);
        Log.d(TAG, "onPostExecute: xz:"+(stock==null)+"");
        if(stock!=null && stock.getSymbol()!=null && stock.getName()!=null) {
            mainActivity.setStock(stock);
        }
        super.onPostExecute(s);
    }

    private Stock jsonToMap(String s) {
        Stock stock = new Stock();
        try {
            JSONObject jsonObject = new JSONObject(s);
            String symbol = jsonObject.getString("symbol");
            String name = jsonObject.getString("companyName");
            Log.d(TAG, "jsonToMap: jp:"+name);
            double price = jsonObject.getDouble("latestPrice");
            double priceChange = jsonObject.getDouble("change");
            double changePercentage = jsonObject.getDouble("changePercent");
            Log.d(TAG, "jsonToMap: bp:"+changePercentage+" latetPrice="+price);
            stock.setName(name);
            stock.setSymbol(symbol);
            stock.setPrice(price);
            stock.setPriceChange(priceChange);
            stock.setChangePercentage(changePercentage);
            return stock;
        } catch (JSONException e) {
            Toast.makeText(mainActivity, "Error in API for the stock", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        return null;
    }
}
