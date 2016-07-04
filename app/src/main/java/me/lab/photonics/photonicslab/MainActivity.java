package me.lab.photonics.photonicslab;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private TextView concTxt, pressureTxt, dateTxt, timeTxt;
    private Button graphBtn, detailsBtn, statsBtn, dayGraphBtn;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        concTxt = (TextView) findViewById(R.id.concentration);
        pressureTxt = (TextView) findViewById(R.id.pressure);
        dateTxt = (TextView) findViewById(R.id.date);
        timeTxt = (TextView) findViewById(R.id.time);
        detailsBtn = (Button) findViewById(R.id.detailsbutton);
        detailsBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, DetailsActivity.class);
                startActivity(intent);
            }
        });
        graphBtn = (Button) findViewById(R.id.graphbutton);
        graphBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intt = new Intent(MainActivity.this, GraphActivity.class);
                startActivity(intt);
            }
        });
        statsBtn = (Button) findViewById(R.id.statsbutton);
        statsBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, StatsActivity.class);
                startActivity(intent);
            }
        });
        dayGraphBtn = (Button) findViewById(R.id.dayGraph);
        dayGraphBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intt = new Intent(MainActivity.this, DayGraph.class);
                startActivity(intt);
            }
        });

        // find expenseRequests
        String url = "https://profarup.herokuapp.com/getDataFromServer/recentData";
        Volley.newRequestQueue(getApplicationContext()).add(jsongetRequestQueueVolley(url));
        startProgressBar();
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
                        return;
                    }else{
                        pressureTxt.setText("Pressure  :  " + result.getJSONObject(0).getString("pressure"));
                        concTxt.setText("Concentration : " + result.getJSONObject(0).getString("concentration"));
                        String dateS = result.getJSONObject(0).getString("date");
                        dateTxt.setText("Date : " + dateS.substring(6,8) + "-" + dateS.substring(4,6) + "-" + dateS.substring(0,4));
                        String timeS = result.getJSONObject(0).getString("time");
                        timeTxt.setText("Time : " + timeS.substring(0,2) + "-" + timeS.substring(2,4) + "-" + timeS.substring(4,6));
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
        });
        return jsonObjReq;
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void startProgressBar(){
        progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading data...");
        progressDialog.show();
    }
}
