package com.akshay.stockwatchapp;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class NameLoader extends AsyncTask<Void, Void, String> {
    private static final String TAG = "NameLoader";
    @SuppressLint("StaticFieldLeak")
    private MainActivity mainActivity;

    private static final String DATA_URL =
            "https://api.iextrading.com/1.0/ref-data/symbols";

    public NameLoader(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    protected String doInBackground(Void... voids) {
        Log.d(TAG, "doInBackground: ");
        Uri dataUri = Uri.parse(DATA_URL);
        String urlToUse = dataUri.toString();
        StringBuilder sb = new StringBuilder();
        try {
            URL url = new URL(urlToUse);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            Log.d(TAG, "doInBackground: Response code "+connection.getResponseCode());
            connection.setRequestMethod("GET");
            InputStream is = connection.getInputStream();
            BufferedReader reader = new BufferedReader((new InputStreamReader(is)));
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
            Log.d(TAG, "doInBackground: bp:"+sb.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    @Override
    protected void onPostExecute(String s) {
        HashMap<String,String> stockHashMap = parseJSON(s);
        Log.d(TAG, "onPostExecute: bp:"+stockHashMap);
        if (stockHashMap != null)
            Toast.makeText(mainActivity, "Loaded " + stockHashMap.size() + " stocks.", Toast.LENGTH_SHORT).show();
        mainActivity.updateData(stockHashMap);
    }

    private HashMap<String,String> parseJSON(String s) {

        HashMap<String,String> stockHashMap = new HashMap<String, String>();
        try {
            JSONArray jsonArray = new JSONArray(s);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jStock = (JSONObject) jsonArray.get(i);
                String name = jStock.getString("name");
                String symbol = jStock.getString("symbol");
                stockHashMap.put(symbol,name);
            }
            Log.d(TAG, "parseJSON: bp"+stockHashMap.size());
            return stockHashMap;
        } catch (Exception e) {
            Log.d(TAG, "parseJSON: " + e.getMessage());
            e.printStackTrace();
        }
        return null;

}
}
