package me.lab.photonics.photonicslab;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.baoyz.widget.PullRefreshLayout;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LabelFormatter;
import com.jjoe64.graphview.Viewport;
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

public class DayGraph extends AppCompatActivity {

    private LinearLayout headLayout;
    private RequestAdapter requestAdapter;
    private ListView listView;
    static Button dateBtn;
    static String dayDate="";
    private Button pressureBtn, concentrationBtn;
    private ProgressDialog progressDialog;
    private TextView errorTxt, COPTxt;
    private String selectedGraph;
    private ArrayList<DayData> dayData;
    private PullRefreshLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day_graph);

        dateBtn = (Button) findViewById(R.id.day);
        pressureBtn = (Button) findViewById(R.id.pressure);
        concentrationBtn = (Button) findViewById(R.id.concentration);
        errorTxt = (TextView) findViewById(R.id.error);
        COPTxt = (TextView) findViewById(R.id.dataCOP);
        listView = (ListView)findViewById(R.id.listview);
        headLayout = (LinearLayout)findViewById(R.id.head);

        dayData = new ArrayList<>();

        layout = (PullRefreshLayout) findViewById(R.id.swipeRefreshLayout);
//        layout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh() {
//                // start refresh
//                dayData.clear();
//                url = "http://heroku-postgres-952261c8.herokuapp.com/dbFile/expensesemp";
//                Volley.newRequestQueue(getApplicationContext()).add(jsongetRequestQueueVolley(url));
//            }
//        });

        dateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new DatePickerFragment();
                newFragment.show(getSupportFragmentManager(), "datePicker");
            }
        });
        concentrationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dayData.clear();
                if (dayDate.equals("")) {
                    errorTxt.setText("Enter valid dates");
                    errorTxt.setTextColor(Color.RED);
                } else {
                    errorTxt.setText("");
                    headLayout.setBackgroundColor(Color.rgb(33, 150, 243));
                    COPTxt.setText("Concentration (in %)");
                    selectedGraph = "concentration";
                    String url = "https://profarup.herokuapp.com/getDataFromServer/dayData";
                    Volley.newRequestQueue(getApplicationContext()).add(jsongetRequestQueueVolley(url));
                    startProgressBar();
                }
            }
        });
        pressureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dayData.clear();
                if (dayDate.equals("")) {
                    errorTxt.setText("Enter valid dates");
                    errorTxt.setTextColor(Color.RED);
                } else {
                    errorTxt.setText("");
                    headLayout.setBackgroundColor(Color.rgb(76, 175, 80));
                    COPTxt.setText("Pressure (in bar)");
                    selectedGraph = "pressure";
                    String url = "https://profarup.herokuapp.com/getDataFromServer/dayData";
                    Volley.newRequestQueue(getApplicationContext()).add(jsongetRequestQueueVolley(url));
                    startProgressBar();
                }
            }
        });
    }

    private boolean generateData(final JSONArray datas) {

        for (int i=0; i<datas.length(); i++) {
            try {
                JSONObject jsonObject = datas.getJSONObject(i);
                if(selectedGraph.equalsIgnoreCase("concentration")){
                    dayData.add(i,new DayData(jsonObject.getString("time"), jsonObject.getString("concentration")));
                }
                if(selectedGraph.equalsIgnoreCase("pressure")){
                    dayData.add(i,new DayData(jsonObject.getString("time"), jsonObject.getString("pressure")));
                }
            }catch (JSONException e){
                Log.d("error JSONException", e.toString());
            }
        }
        Log.d("dayData", dayData.get(0).getData());
        requestAdapter = new RequestAdapter(getApplicationContext(),dayData);
        listView.setAdapter(requestAdapter);
        return true;
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
                        Log.d("error", "No data found");
                        errorTxt.setText("No data found");
                        errorTxt.setTextColor(Color.RED);
                        listView.setEmptyView(findViewById(R.id.error));
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
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json");
                headers.put("day", dayDate);
                return headers;
            }
        };
        return jsonObjReq;
    }


    public static class DatePickerFragment extends DialogFragment
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
            dayDate = year + monthString.substring(monthString.length()-2, monthString.length()) + dayString.substring(dayString.length()-2, dayString.length());
            dateBtn.setText(day + "-" + (month+1) + "-" + year);
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
