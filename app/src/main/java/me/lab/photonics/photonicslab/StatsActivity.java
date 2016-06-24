package me.lab.photonics.photonicslab;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Created by abhisek on 6/23/16.
 */
public class StatsActivity extends AppCompatActivity {
    private TextView cstatTxt, pstatTxt, errorTxt;
    private static Button frmBtn, tillBtn, showBtn;
    private ProgressDialog progressDialog;
    private static String fromDate = "";
    private static String toDate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);
        cstatTxt = (TextView) findViewById(R.id.cstat);
        pstatTxt = (TextView) findViewById(R.id.pstat);
        errorTxt = (TextView) findViewById(R.id.errs);
        frmBtn = (Button) findViewById(R.id.frm);
        tillBtn = (Button) findViewById(R.id.till);
        showBtn = (Button) findViewById(R.id.getSats);

        frmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new DateFromPickerFragment();
                newFragment.show(getSupportFragmentManager(), "datePicker");
            }
        });
        tillBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new DateToPickerFragment();
                newFragment.show(getSupportFragmentManager(), "datePicker");
            }
        });
        showBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fromDate.equals("") || toDate.equals("")) {
                    errorTxt.setText("Enter valid dates");
                    errorTxt.setTextColor(Color.RED);
                } else {
                    errorTxt.setText("");
                    String url = "https://profarup.herokuapp.com/getDataFromServer/recentData";
                    Volley.newRequestQueue(getApplicationContext()).add(jsongetRequestQueueVolley(url));
                    startProgressBar();
                }
            }
        });
        // find expenseRequests
    }

    private JsonObjectRequest jsongetRequestQueueVolley(String url) {
        final JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                url, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                if(progressDialog != null && progressDialog.isShowing()){
                    progressDialog.dismiss();
                }
                Log.d("hi: ", response.toString());
                try {
                    JSONArray result = response.getJSONArray("result");
                    if (result.length() == 0) {
                        //headText.setText("Sorry you do not have any previous data");
                        Log.d("error", "No data found");
                        errorTxt.setText("No data found");
                        errorTxt.setTextColor(Color.RED);
                        return;
                    }else {
                        generateStatData(result);
                    }

                } catch (JSONException e) {
                    //e.printStackTrace();
                    Log.d("error: ", e.toString());
                    Toast.makeText(getApplicationContext(),
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(progressDialog != null && progressDialog.isShowing()){
                    progressDialog.dismiss();
                }
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_SHORT).show();
                // hide the progress dialog
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("from", fromDate);
                headers.put("to", toDate);
                return headers;
            }
        };
        return jsonObjReq;
    }

    public static class DateFromPickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            // Do something with the date chosen by the user
            String dayString = "00" + day;
            String monthString = "00" + (month + 1);
            fromDate = year + monthString.substring(monthString.length()-2, monthString.length()) + dayString.substring(dayString.length()-2, dayString.length());
            frmBtn.setText(day + "-" + (month+1) + "-" + year);
        }
    }

    public static class DateToPickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            // Do something with the date chosen by the user
            String dayString = "00" + day;
            String monthString = "00" + (month + 1);
            toDate = year + monthString.substring(monthString.length()-2, monthString.length()) + dayString.substring(dayString.length()-2, dayString.length());
            tillBtn.setText(day + "-" + (month+1) + "-" + year);
        }
    }

    private void generateStatData(JSONArray data) {
        double p_sum = 0.0;
        double c_sum = 0.0;
        int n = data.length();
        double c_avg, p_avg, c_sd, p_sd;
        double[] c_list = new double[n];
        double[] p_list = new double[n];
        for(int i=0;i<n;i++){
            try {
                JSONObject jsonObject = data.getJSONObject(i);
                Double c_temp = Double.parseDouble(jsonObject.getString("concentration"));
                Double p_temp = Double.parseDouble(jsonObject.getString("pressure"));
                c_list[i] = c_temp;
                p_list[i] = p_temp;
                c_sum = c_sum + c_temp;
                p_sum = p_sum + p_temp;

            }catch (JSONException e){
                Log.d("error JSONException", e.toString());
            }
        }
        c_avg = c_sum/n;
        p_avg = p_sum/n;
        double c_dev=0, p_dev=0;
        for(int i=0;i<n;i++){
            c_dev = c_dev + Math.pow((c_list[i]-c_avg),2);
            p_dev = p_dev + Math.pow((p_list[i]-p_avg),2);
        }
        c_sd = Math.sqrt(c_dev/n);
        p_sd = Math.sqrt(p_dev/n);
        String c_dat = "Concentration Stats\n\nMean: "+String.valueOf(c_avg)+"\nStandard Deviation: "+String.valueOf(c_sd);
        String p_dat = "Pressure Stats\n\nMean: "+String.valueOf(p_avg)+"\nStandard Deviation: "+String.valueOf(p_sd);
        cstatTxt.setText(c_dat);
        pstatTxt.setText(p_dat);
        //Toast.makeText(getApplicationContext(), "Data: " + p_dat, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void startProgressBar(){
        progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading data...");
        progressDialog.show();
    }

}
