package com.example.assignment1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {
    public EditText miles;
    public EditText km;
    public EditText result;
    public TextView m_lbl;
    public TextView km_lbl;
    public RadioButton m_to_km;
    public RadioButton km_to_m;
    public RadioGroup rg1;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v("oncreate","oncreate was called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        result = findViewById(R.id.result_txt);
        result.setVerticalScrollBarEnabled(true);
        result.setMovementMethod(new ScrollingMovementMethod());
//        result.setEnabled(false);
        miles = findViewById(R.id.m_txt);
        km = findViewById(R.id.km_txt);
        m_lbl = findViewById(R.id.m_lbl);
        km_lbl = findViewById(R.id.km_lbl);
        m_to_km = findViewById(R.id.m_to_km);
        km_to_m = findViewById(R.id.km_to_m);
        result.setKeyListener(null);
getChecked();
    }

    private void getChecked() {
        m_to_km.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m_lbl.setText("Miles");
                km_lbl.setText("Kilometers");
                Log.v("M_LBL",m_lbl.getText().toString());
                miles.setHint("Miles");
                km.setHint("Kilometers");
            }
        });
        km_to_m.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m_lbl.setText("Kilometers");
                km_lbl.setText("Miles");
                Log.v("M_LBL",m_lbl.getText().toString());
                miles.setHint("Kilometers");
                km.setHint("Miles");
            }
        });
    }

    public void doCalc(View view){
        String s4 = miles.getText().toString().trim();
        miles.setText("");
        String result_calc;
        if(m_to_km.isChecked())
        {
           if(s4.equals("")){
              Toast.makeText(getApplicationContext(),"Please Enter Miles",Toast.LENGTH_LONG).show();
              Log.v("Default","No value in miles");
           }
           else
           {
               double miles = Double.parseDouble(s4);
               result.setText(result.getText()+"\n"+miles+"m ==>"+(new DecimalFormat("###.#").format(miles*1.60934))+"km");
               km.setText((new DecimalFormat("###.#").format(miles*1.609)));

           }

        }
        else if(km_to_m.isChecked()){
            if(s4.equals("")){
                Toast.makeText(getApplicationContext(),"Please Enter Kilometers",Toast.LENGTH_LONG).show();
            }
            else{
                double miles = Double.parseDouble(s4);
                result.setText(result.getText()+"\n"+miles+"km ==>"+(new DecimalFormat("###.#").format(miles*0.6213))+"m");
                km.setText((new DecimalFormat("###.#").format(miles*0.621)));
            }

        }

    }

    public void clear(View view){
        result.setText("");
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {

        outState.putString("result_state",result.getText().toString());
        outState.putBoolean("m_to_km_state",m_to_km.isChecked());
        super.onSaveInstanceState(outState);

    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Boolean b = savedInstanceState.getBoolean("m_to_km_state");
        if(b)
        {
            m_to_km.performClick();
        }
        else
        {
            km_to_m.performClick();
        }
        result.setText(savedInstanceState.getString("result_state"));

    }
}
