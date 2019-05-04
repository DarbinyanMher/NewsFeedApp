package com.example.newsfeedapp.utils;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.newsfeedapp.activities.MainActivity;
import com.example.newsfeedapp.model.ListItemNews;
import com.pixplicity.easyprefs.library.Prefs;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.android.volley.VolleyLog.TAG;



public class MyWorker extends Worker {

  private String url_data = "https://content.guardianapis.com/search";
  private String getUpdatedTotalNumber;
  private String getTotalNumber;
  Boolean isUpdated;

    Context context;
    public MyWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        isUpdated = false;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url_data, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // progressDialog.dismiss();
                Log.e("Rest Response", response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONObject jsonObjectNested = jsonObject.getJSONObject("response");



                    getUpdatedTotalNumber = jsonObjectNested.getString("total");
                    getTotalNumber = Prefs.getString("totalItemsNumber", "4");

                    if(!getTotalNumber.equals(getUpdatedTotalNumber)) {
                        isUpdated = true;

                        Prefs.putString("isDataChanged", "isUpdated");
                    }

                    else {
                        Prefs.putString("isDataChanged", "notUpdated");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                })

        {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("api-key", "5c84154e-9984-4993-bfdd-7bcf3fd60bf0");
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(stringRequest);

            return Result.retry();
        //return (isUpdated ? Result.retry() : Result.success());
    }

    private Data createInputData() {
        Data.Builder builder = new Data.Builder();
        builder.putBoolean("booleanKey", isUpdated);
        return builder.build();
    }

//    private void displayNotification(String task, String desc) {
//        NotificationManager manager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
//        if (Build.VERSION.SDK_INT > = Build.VERSION_CODES.O) {
//            NotificationChannel
//        }
//    }
}
