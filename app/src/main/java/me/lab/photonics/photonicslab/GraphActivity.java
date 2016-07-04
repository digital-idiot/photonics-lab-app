package me.lab.photonics.photonicslab;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class GraphActivity extends AppCompatActivity {

    private GraphView graphDisp;
    static Button fromBtn, toBtn;
    static String fromDate="", toDate="";
    private Button pressureBtn, concentrationBtn;
    private ProgressDialog progressDialog;
    private TextView errorTxt;
    private String selectedGraph;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);
        graphDisp = (GraphView) findViewById(R.id.graph);
        fromBtn = (Button) findViewById(R.id.from);
        toBtn = (Button) findViewById(R.id.to);
        pressureBtn = (Button) findViewById(R.id.pressure);
        concentrationBtn = (Button) findViewById(R.id.concentration);
        errorTxt = (TextView) findViewById(R.id.error);

        fromBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new DateFromPickerFragment();
                newFragment.show(getSupportFragmentManager(), "datePicker");
            }
        });
        toBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new DateToPickerFragment();
                newFragment.show(getSupportFragmentManager(), "datePicker");
            }
        });
        concentrationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fromDate.equals("") || toDate.equals("")) {
                    errorTxt.setText("Enter valid dates");
                    errorTxt.setTextColor(Color.RED);
                } else {
                    errorTxt.setText("");
                    selectedGraph = "concentration";
                    String url = "https://profarup.herokuapp.com/getDataFromServer/graphData";
                    Volley.newRequestQueue(getApplicationContext()).add(jsongetRequestQueueVolley(url));
                    startProgressBar();
                }
            }
        });
        pressureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fromDate.equals("") || toDate.equals("")) {
                    errorTxt.setText("Enter valid dates");
                    errorTxt.setTextColor(Color.RED);
                } else {
                    errorTxt.setText("");
                    selectedGraph = "pressure";
                    String url = "https://profarup.herokuapp.com/getDataFromServer/graphData";
                    Volley.newRequestQueue(getApplicationContext()).add(jsongetRequestQueueVolley(url));
                    startProgressBar();
                }
            }
        });
        graphDisp.getViewport().setScrollable(true);
    }

    private DataPoint[] generateData(JSONArray graphDataPoints) {

        DataPoint[] values1 = new DataPoint[graphDataPoints.length()];
        DataPoint[] values2 = new DataPoint[graphDataPoints.length()];
        String x1[] = new String[graphDataPoints.length()];
        for (int i=0; i<graphDataPoints.length(); i++) {
            try {
                JSONObject jsonObject = graphDataPoints.getJSONObject(i);
                String dateS = jsonObject.getString("date");
                x1[i] = dateS.substring(6,8) + "-" + dateS.substring(4,6) + "-" + dateS.substring(0,4);
                double y = Double.parseDouble(jsonObject.getString("concentration"));
                double z = Double.parseDouble(jsonObject.getString("pressure"));
                DataPoint dataPoint1 = new DataPoint(i,y);
                values1[i] = dataPoint1;
                DataPoint dataPoint2 = new DataPoint(i,z);
                values2[i] = dataPoint2;

            }catch (JSONException e){
                Log.d("error JSONException", e.toString());
            }
        }
        graphDisp.removeAllSeries();
        BarGraphSeries<DataPoint> series = new BarGraphSeries<DataPoint>(values1);

        if(selectedGraph.equalsIgnoreCase("concentration")){
            series.resetData(values1);
            series.setSpacing(20);
            series.setColor(Color.rgb(33,150,243));
            series.setDrawValuesOnTop(true);
            series.setValuesOnTopColor(Color.RED);
        }
        if (selectedGraph.equalsIgnoreCase("pressure")){
            series.resetData(values2);
            series.setSpacing(20);
            series.setColor(Color.rgb(76,175,80));
            series.setDrawValuesOnTop(true);
            series.setValuesOnTopColor(Color.RED);
        }

        // for x-axis labelling
        StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(graphDisp);
        staticLabelsFormatter.setHorizontalLabels(x1);
        graphDisp.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);

        graphDisp.addSeries(series);
        graphDisp.setHorizontalScrollBarEnabled(true);
        graphDisp.getViewport().setScrollable(true);
        return values1;
    }

    @Override
    public void onResume() {
        super.onResume();
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
                        graphDisp.removeAllSeries();
                        return;
                    }else {
                        generateData(result);
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
            fromBtn.setText(day + "-" + (month+1) + "-" + year);
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
            toBtn.setText(day + "-" + (month+1) + "-" + year);
        }
    }

    public void startProgressBar(){
        progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading data...");
        progressDialog.show();
    }
}
