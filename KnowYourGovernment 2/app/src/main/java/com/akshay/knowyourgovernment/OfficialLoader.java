package com.akshay.knowyourgovernment;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

import static java.lang.System.out;

public class OfficialLoader extends AsyncTask<String,Void, ArrayList<Official>> {

    private MainActivity mainActivity;
    private final String API_KEY = "";
    private final String URL_FOR_OFFICIAL = "https://www.googleapis.com/civicinfo/v2/representatives?key=";
    private static final String TAG = "OfficialLoader";

    public OfficialLoader(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    protected ArrayList<Official> doInBackground(String... strings) {
        ArrayList<Official> officialArrayList = new ArrayList<>();
        String URL_TO_USE = URL_FOR_OFFICIAL+API_KEY+"&address="+strings[0];
        Uri uri = Uri.parse(URL_TO_USE);
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

        } catch (Exception e) {


        }
        Log.d(TAG, "doInBackground: calling ");
        setLocation(sb.toString());
        officialArrayList = parseJSON(sb.toString());
        return officialArrayList;
    }


    @Override
    protected void onPostExecute(ArrayList<Official> officials) {
        if(officials.size()<=0 || officials == null){

            mainActivity.errorText.setText("Location is not valid");
            mainActivity.location.setText("Location Unavailable");
        }
        mainActivity.doOfficialData(officials);
        super.onPostExecute(officials);
    }

    private void setLocation(String data){
        TextView location = mainActivity.findViewById(R.id.location);
        try{
            JSONObject normalizedInput = new JSONObject(data);
            normalizedInput = normalizedInput.getJSONObject("normalizedInput");
            String city = normalizedInput.getString("city");
            String state = normalizedInput.getString("state");
            String zip = normalizedInput.getString("zip");
            String whole_address = city + ", "+state+", "+zip;
            Log.d(TAG, "setLocation: "+whole_address);
            location.setText(whole_address);
        }catch (Exception e){

        }
    }


    private ArrayList<Official> parseJSON(String s) {
       ArrayList<Official> officialArrayList = new ArrayList<>();
       Official official = new Official();
        try {
            JSONObject temp = new JSONObject(s);
            JSONArray offices = (JSONArray)temp.getJSONArray("offices");
            JSONArray officials = (JSONArray) temp.get("officials");
            Log.d(TAG, "parseJSON: bp:"+offices.length());

            for(int i =0;i<offices.length();i++){
                JSONObject office = (JSONObject) offices.get(i);
                JSONObject officialIndices = (JSONObject)offices.get(i);
                JSONArray index = officialIndices.getJSONArray("officialIndices");

                for(int j=0;j<index.length();j++){
                    Log.d(TAG, "parseJSON: bp2:");
                    Official official_intermediate = new Official();
                    JSONObject officalData_JSON = (JSONObject)officials.get(index.getInt(j));
                    official_intermediate = fetchOfficialDetails(officalData_JSON);
                    official = official_intermediate;
                    official.setTitle(office.getString("name"));
                    officialArrayList.add(official);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return officialArrayList;

    }

    private String getName(JSONObject officialData_json){

        String name="";
        try {
            if(officialData_json.getString("name").equals("") || officialData_json.getString("name")==null)
            return "";
            else
                name = officialData_json.getString("name");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return name;
    }
    private String getline1(JSONObject officialData_json){

        String line1="";
        try {

            JSONArray jaddress_arr;
            if(officialData_json.has("address"))
            {
                jaddress_arr = (JSONArray) officialData_json.get("address");
            }
            else{
                return "";
            }

            JSONObject actual_data = (JSONObject)jaddress_arr.get(0);
            if(actual_data.has("line1") && actual_data.getString("line1")!=null && !actual_data.getString("line1").equals("")){
               line1 = actual_data.getString("line1");

               return line1;
            }
            else {
                return "";
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return line1;
    }
    private String getline2(JSONObject officialData_json){

        String line2="";
        try {

            JSONArray jaddress_arr;
            if(officialData_json.has("address"))
            {
                jaddress_arr = (JSONArray) officialData_json.get("address");
            }
            else{
                return "";
            }

            JSONObject actual_data = (JSONObject)jaddress_arr.get(0);
            if(actual_data.has("line2") && actual_data.getString("line2")!=null && !actual_data.getString("line2").equals("")){
                line2 = actual_data.getString("line2");

                return line2;
            }
            else {
                return "";
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return line2;
    }

    private String getCity(JSONObject officialData_json){

        String city="";
        try {
            JSONArray jaddress_arr;
            if(officialData_json.has("address"))
            {
                jaddress_arr = (JSONArray) officialData_json.get("address");
            }
            else{
                return "";
            }
            JSONObject actual_data = (JSONObject)jaddress_arr.get(0);
            if(actual_data.has("city") && actual_data.getString("city")!=null && !actual_data.getString("city").equals("")){
                city = actual_data.getString("city");

                return city;
            }
            else {
                return "";
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return city;
    }

    private String getState(JSONObject officialData_json){

        String state="";
        try {

            JSONArray jaddress_arr;
            if(officialData_json.has("address"))
            {
                jaddress_arr = (JSONArray) officialData_json.get("address");
            }
            else{
                return "";
            }
            JSONObject actual_data = (JSONObject)jaddress_arr.get(0);
            if(actual_data.has("state") && actual_data.getString("state")!=null && !actual_data.getString("state").equals("")){
                state = actual_data.getString("state");

                return state;
            }
            else {
                return "";
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return state;
    }


    private String getZip(JSONObject officialData_json){

        String zip="";
        try {

            JSONArray jaddress_arr;
            if(officialData_json.has("address"))
            {
                jaddress_arr = (JSONArray) officialData_json.get("address");
            }
            else{
                return "";
            }
            JSONObject actual_data = (JSONObject)jaddress_arr.get(0);
            if(actual_data.has("zip") && actual_data.getString("zip")!=null && !actual_data.getString("zip").equals("")){
                zip = actual_data.getString("zip");

                return zip;
            }
            else {
                return "";
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return zip;
    }

    private String getURL(JSONObject officialData_json){

        String urls="";
        try {

            JSONArray jaddress_arr;
            if(officialData_json.has("urls"))
            {
                jaddress_arr = (JSONArray) officialData_json.get("urls");
            }
            else{
                return "";
            }
            if(jaddress_arr.get(0).toString().equals("") ||jaddress_arr.get(0).toString()==null)
            {
                return "";
            }else {
            urls = jaddress_arr.get(0).toString();
            return urls;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return urls;
    }
    private String getEmail(JSONObject officialData_json){

        String emails="";
        try {

            JSONArray jaddress_arr;
            if(officialData_json.has("emails"))
            {
                jaddress_arr = (JSONArray) officialData_json.get("emails");
            }
            else{
                return "";
            }
            if(jaddress_arr.get(0).toString().equals("") ||jaddress_arr.get(0).toString()==null)
            {
                return "";
            }else {
                emails = jaddress_arr.get(0).toString();
                return emails;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return emails;
    }

    private String getParty(JSONObject officialData_json){

        String party="";
        try {

            if(officialData_json.has("party")){
                party =  officialData_json.getString("party");
            }
            else{
                return "<not available>";
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return party;
    }

    private String getphotoUrl(JSONObject officialData_json){

        String photoUrl="";
        try {

            if(officialData_json.has("photoUrl")){
                photoUrl =  officialData_json.getString("photoUrl");
            }
            else{
                return "";
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return photoUrl;
    }

    private String getPhone(JSONObject officialData_json){

        String phones="";
        try {

            JSONArray jaddress_arr;
            if(officialData_json.has("phones"))
            {
                jaddress_arr = (JSONArray) officialData_json.get("phones");
            }
            else{
                return "";
            }
            if(jaddress_arr.get(0).toString().equals("") ||jaddress_arr.get(0).toString()==null)
            {
                return "";
            }else {
                phones = jaddress_arr.get(0).toString();
                return phones;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return phones;
    }

    private ArrayList<Channel> getChannels(JSONObject officialData_json){

        ArrayList<Channel> tempList = new ArrayList<>();
        Channel temp;
        Log.d(TAG, "getChannels: "+tempList.size());

        try {

            JSONArray jaddress_arr;
            if(officialData_json.has("channels"))
            {
                jaddress_arr = (JSONArray) officialData_json.get("channels");
                for(int i =0 ;i<jaddress_arr.length();i++){
                    JSONObject channel = (JSONObject)jaddress_arr.get(i);
                    temp = new Channel(channel.getString("type"), channel.getString("id"));
                    tempList.add(temp);
                }
                Log.d(TAG, "getChannels: After"+tempList.size());
            }
            else{
                return tempList;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


        return tempList;

    }


    private Official fetchOfficialDetails(JSONObject officalData_json) {
        Official official = new Official();
        try{
            official.setName(getName(officalData_json));
            official.setParty("( "+getParty(officalData_json)+" )");

            //address
            String line2 = getline2(officalData_json);
            String line1 = getline1(officalData_json);
            String city = getCity(officalData_json);
            String state = getState(officalData_json);
            String zip = getZip(officalData_json);

            line1= line1.equals("") || line1==null ?"":line1+", ";
            line2= line2.equals("") || line2==null ?"":line2+", ";
            city = city.equals("") || city==null ?"":city+", ";
            state =state.equals("") || state==null ?"":state+", ";
            zip = zip.equals("") || zip==null ?"":zip+"";



            //optimize this
            official.setAddress(line1+line2+city+state+zip);
            //address end

            official.setUrl(getURL(officalData_json));
            official.setEmails(getEmail(officalData_json));
            official.setPhotoURL(getphotoUrl(officalData_json));
            official.setPhones(getPhone(officalData_json));
            official.setChannels(getChannels(officalData_json));
            Log.d(TAG, "fetchOfficialDetails: URL:"+official.getPhotoURL());

        }catch (Exception e){

        }
        return official;
    }
}
