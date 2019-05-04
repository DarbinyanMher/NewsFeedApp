package com.example.newsfeedapp.activities;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.android.volley.AuthFailureError;
import com.android.volley.Cache;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.newsfeedapp.R;
import com.example.newsfeedapp.adapters.ListAdapterNews;
import com.example.newsfeedapp.adapters.ListAdapterSavedNews;
import com.example.newsfeedapp.model.ListItemNews;
import com.example.newsfeedapp.model.ListItemSavedNews;
import com.example.newsfeedapp.utils.Constants;
import com.example.newsfeedapp.utils.EndlessRecyclerViewScrollListener;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmResults;

import static com.example.newsfeedapp.utils.Constants.CHANNEL_ID;
import static com.example.newsfeedapp.utils.Constants.CHANNEL_NAME;

public class MainActivity extends AppCompatActivity  {
    private String url_Data = ("https://content.guardianapis.com/search?show-fields=starRating,headline,body,thumbnail,short-url&page-size=20&page=1");
    private String url_DataChecker = ("https://content.guardianapis.com/search?show-fields=headline,body,thumbnail,short-url&page-size=20&page=1");

    private String getUpdatedTotalNumber;
    private String getTotalNumber;
    private String nextUrl_data;
    private RecyclerView feedRecycler;
    private List<ListItemNews> mListItemNews;
    private ListAdapterNews adapterNews;
    private ArrayList<ListItemSavedNews> listItemSavedNews;
    private EndlessRecyclerViewScrollListener scrollListener;
    private RecyclerView savedItemsRecycler;
    private ListAdapterSavedNews mListAdapterSavedNews;
    int nextPage = 1;
    ListItemNews item;
    private int mInterval = 30000;
    private Handler mHandler;
    private boolean isAppInForeground = false;
    private ArrayList<ListItemNews> newList;
    int layoutId;
    DividerItemDecoration dividerItemDecoration;
    RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolBarMainPage);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("NewsFeedApp");
        createNotificationChannel();
        feedRecycler = findViewById(R.id.recyclerViewFeed);
        layoutManager = new LinearLayoutManager(this);
        layoutId = 1;
        dividerItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        feedRecycler.setLayoutManager(layoutManager);
        feedRecycler.setHasFixedSize(true);
        if(isNetworkAvailable()) {
            startWhenNetworkIsAvailable();
        } else  {
            showCustomSnackbar("No internet connection, please enable your network and tap to retry button", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startWhenNetworkIsAvailable();
                }
            }).show();
        }
    }

    private void startWhenNetworkIsAvailable() {
        loadFirstPageData(url_Data);
        mHandler = new Handler();
        startRepeatingTask();
        scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                loadData(page);
            }
        };
        mListItemNews = new ArrayList<>();
        mListAdapterSavedNews = new ListAdapterSavedNews(this, new ArrayList<ListItemSavedNews>(), new ListAdapterNews.onItemClick() {
            @Override
            public void onClick(View view, int position) {
                Intent intentToSavedActivity = new Intent(getBaseContext(), SavedPageActivity.class);
                intentToSavedActivity.putExtra("primaryKey", listItemSavedNews.get(position).getId());
                startActivity(intentToSavedActivity);
            }
        });
        savedItemsRecycler = findViewById(R.id.savedItemsRecyclerView);
        savedItemsRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        feedRecycler.addItemDecoration(dividerItemDecoration);
        feedRecycler.addOnScrollListener(scrollListener);
        savedItemsRecycler.setAdapter(mListAdapterSavedNews);
        showSavedDataIfNeeded();
    }

    private ListAdapterNews.onItemClick onItemClick= new ListAdapterNews.onItemClick (){
        @Override
        public void onClick(View view, int position) {
            String getId = mListItemNews.get(position).getId();
            String getBody = mListItemNews.get(position).getBody();
            String getbodyImage = mListItemNews.get(position).getImageUrl();
            String getTitle = mListItemNews.get(position).getTitle();
            String articleCategory = mListItemNews.get(position).getCategory();
            String getPageNumber = mListItemNews.get(position).getPageNumber();
            Intent intent = new Intent(getBaseContext(), NewsPageActivity.class);
            ListItemNews itemNews = new ListItemNews(getId, getTitle, getbodyImage, articleCategory, getBody, getPageNumber);
            intent.putExtra(Constants.LISTITEMSNEWS_EXTRA, itemNews);
            startActivity(intent);
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chnage_mode_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.itemChangeMode) {
            if (layoutId == 1) {
                layoutId = 2;
                layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
                adapterNews = null;
                feedRecycler.setAdapter(adapterNews);
                feedRecycler.clearOnScrollListeners();
                feedRecycler.removeItemDecoration(dividerItemDecoration);
                feedRecycler.addOnScrollListener(new EndlessRecyclerViewScrollListener(layoutManager) {
                    @Override
                    public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                        loadData(page);
                    }
                });
                feedRecycler.setLayoutManager(layoutManager);
                adapterNews = new ListAdapterNews(this, layoutId, mListItemNews, onItemClick);
                feedRecycler.setAdapter(adapterNews);
            } else {
                layoutId = 1;
                layoutManager = new LinearLayoutManager(this);
                adapterNews = null;
                feedRecycler.setAdapter(adapterNews);
                feedRecycler.clearOnScrollListeners();
                feedRecycler.addItemDecoration(dividerItemDecoration);
                feedRecycler.addOnScrollListener(new EndlessRecyclerViewScrollListener(layoutManager) {
                    @Override
                    public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                        nextPage ++;
                        loadData(nextPage);
                    }
                });
                feedRecycler.setLayoutManager(layoutManager);
                adapterNews = new ListAdapterNews(this, layoutId, mListItemNews, onItemClick);
                feedRecycler.setAdapter(adapterNews);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadData(int page) {
        nextUrl_data = ("https://content.guardianapis.com/search?show-fields=starRating,headline,body,thumbnail,short-url&page-size=20&page=" + (page + 1));
        if(isNetworkAvailable()) {
            loadDatasOfNextPages(nextUrl_data);
        } else  {
            callRecursive();
        }
    }

    private void callRecursive() {
        showCustomSnackbar("No internet connection, please enable it and tap to retry button", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isNetworkAvailable()) {
                    loadDatasOfNextPages(nextUrl_data);
                } else  {
                    callRecursive();
                }
            }
        }).show();
    }
    public void showSavedDataIfNeeded() {
        if(isDBListEmpty()) {
            updateUI();
        } else  {
            updateUI();
            RealmResults<ListItemSavedNews> guests = Realm.getDefaultInstance().where(ListItemSavedNews.class).findAll();
            if(guests.size() > 0) {
                listItemSavedNews = new ArrayList<>(guests);
                mListAdapterSavedNews.updateList(listItemSavedNews);
            }
        }
    }

    private Snackbar showCustomSnackbar(String text, View.OnClickListener clickListener) {
        return Snackbar.make(feedRecycler, text, Snackbar.LENGTH_INDEFINITE)
                .setAction("Retry", clickListener);
    }
    private Boolean isNetworkAvailable() {
        ConnectivityManager mgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = mgr.getActiveNetworkInfo();
        if(networkInfo!= null) {
            return networkInfo.isConnected();
        } else  {
            return false;
        }
    }
    private boolean isDBListEmpty() {
        return Realm.getDefaultInstance().where(ListItemSavedNews.class).findAll().size() == 0;
    }
    private void updateUI() {
        savedItemsRecycler.setVisibility(isDBListEmpty()? View.GONE : View.VISIBLE);
    }

    private void loadFirstPageData(String url_data) {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url_data, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // progressDialog.dismiss();
                Log.e("Rest Response", response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONObject jsonObjectNested = jsonObject.getJSONObject("response");
                    JSONArray jsonArray = jsonObjectNested.getJSONArray("results");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject o = jsonArray.getJSONObject(i);
                        JSONObject fields = o.getJSONObject("fields");
                        if (fields.has("thumbnail")) {
                            item = new ListItemNews(
                                    o.getString("id"),
                                    o.getString("webTitle"),
                                    fields.getString("thumbnail"),
                                    o.getString("sectionName"),
                                    fields.getString("body"),
                                    jsonObjectNested.getString("pages")
                            );
                        }
                        else {
                            item = new ListItemNews(
                                    o.getString("id"),
                                    o.getString("webTitle"),
                                    "https://www.dia.org/sites/default/files/No_Img_Avail.jpg",
                                    o.getString("sectionName"),
                                    fields.getString("body"),
                                    jsonObjectNested.getString("pages"));
                        }

                              mListItemNews.add(item);


                        getTotalNumber = jsonObjectNested.getString("total");
                    }
                    adapterNews = new ListAdapterNews(MainActivity.this, layoutId, mListItemNews, onItemClick);
                    feedRecycler.setAdapter(adapterNews);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("error", "onScrollListener", error);
                    }
                })

        {
            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                try {
                    Cache.Entry cacheEntry = HttpHeaderParser.parseCacheHeaders(response);
                    if (cacheEntry == null) {
                        cacheEntry = new Cache.Entry();
                    }
                    final long cacheHitButRefreshed = 3 * 60 * 1000;
                    final long cacheExpired = 24 * 60 * 60 * 1000;
                    long now = System.currentTimeMillis();
                    final long softExpire = now + cacheHitButRefreshed;
                    final long ttl = now + cacheExpired;
                    cacheEntry.data = response.data;
                    cacheEntry.softTtl = softExpire;
                    cacheEntry.ttl = ttl;
                    String headerValue;
                    headerValue = response.headers.get("Date");
                    if (headerValue != null) {
                        cacheEntry.serverDate = HttpHeaderParser.parseDateAsEpoch(headerValue);
                    }
                    headerValue = response.headers.get("Last-Modified");
                    if (headerValue != null) {
                        cacheEntry.lastModified = HttpHeaderParser.parseDateAsEpoch(headerValue);
                    }
                    cacheEntry.responseHeaders = response.headers;
                    final String jsonString = new String(response.data,
                            HttpHeaderParser.parseCharset(response.headers));

                    return Response.success(jsonString, cacheEntry);
                } catch (UnsupportedEncodingException e) {
                    return Response.error(new ParseError(e));
                }

            }

            @Override
            protected void deliverResponse(String response) {
                super.deliverResponse(response);
            }

            @Override
            public void deliverError(VolleyError error) {
                super.deliverError(error);
            }

            @Override
            protected VolleyError parseNetworkError(VolleyError volleyError) {
                return super.parseNetworkError(volleyError);
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("api-key", "5c84154e-9984-4993-bfdd-7bcf3fd60bf0");
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void loadDatasOfNextPages(String url_data) {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url_data, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // progressDialog.dismiss();
                Log.e("Rest Response", response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONObject jsonObjectNested = jsonObject.getJSONObject("response");
                    JSONArray jsonArray = jsonObjectNested.getJSONArray("results");//make interface like this
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject o = jsonArray.getJSONObject(i);
                        JSONObject fields = o.getJSONObject("fields");
                        if (fields.has("thumbnail")) {
                            item = new ListItemNews(
                                    o.getString("id"),
                                    o.getString("webTitle"),
                                    fields.getString("thumbnail"),
                                    o.getString("sectionName"),
                                    fields.getString("body"),
                                    jsonObjectNested.getString("pages")
                            );
                        }
                        else {
                            item = new ListItemNews(
                                    o.getString("id"),
                                    o.getString("webTitle"),
                                    "https://www.dia.org/sites/default/files/No_Img_Avail.jpg",
                                    o.getString("sectionName"),
                                    fields.getString("body"),
                                    jsonObjectNested.getString("pages"));
                        }
                        mListItemNews.add(item);
                    }
                    adapterNews.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("error", "onScrollListener", error);
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
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { ;
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
    private void showNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_add_news)
                .setContentTitle("New headlines are available!")
                .setContentText("Press to check out the latest news")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(2, builder.build());
    }

    private Runnable mStatusChecker = new Runnable() {
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void run() {
            try {
                    StringRequest stringRequest = new StringRequest(Request.Method.GET, url_DataChecker, new Response.Listener<String>() {
                        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                        @Override
                        public void onResponse(String response) {
                            Log.e("Rest Response", response);
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                JSONObject jsonObjectNested = jsonObject.getJSONObject("response");
                                JSONArray jsonArray = jsonObjectNested.getJSONArray("results");
                                getUpdatedTotalNumber = jsonObjectNested.getString("total");
                                if (!getTotalNumber.equals(getUpdatedTotalNumber)) {
                                    newList = new ArrayList<>();
                                    newList.clear();
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject o = jsonArray.getJSONObject(i);
                                    JSONObject fields = o.getJSONObject("fields");
                                        if (fields.has("thumbnail")) {
                                            newList.add(new ListItemNews(o.getString("id"),
                                                    o.getString("webTitle"),
                                                    fields.getString("thumbnail"),
                                                    o.getString("sectionName"),
                                                    fields.getString("body"),
                                                    jsonObjectNested.getString("total"))
                                            );
                                        }
                                        else {
                                            newList.add(new ListItemNews(o.getString("id"),
                                                    o.getString("webTitle"),
                                                    "https://www.dia.org/sites/default/files/No_Img_Avail.jpg",
                                                    o.getString("sectionName"),
                                                    fields.getString("body"),
                                                    jsonObjectNested.getString("total"))
                                            );
                                        }

                                }
                                    if (isAppInForeground) {
                                        adapterNews.updateList(newList);
                                        getTotalNumber = getUpdatedTotalNumber;
                                   }
                                    else {
                                        showNotification();
                                        adapterNews.updateList(newList);
                                        getTotalNumber = getUpdatedTotalNumber;
                                    }
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
                            }) {

                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            Map<String, String> params = new HashMap<>();
                            params.put("api-key", "5c84154e-9984-4993-bfdd-7bcf3fd60bf0");
                            return params;
                        }
                    };
                    RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
                    requestQueue.add(stringRequest);

            } finally {
                mHandler.postDelayed(mStatusChecker, mInterval);
            }
        }
    };

    void startRepeatingTask() {
        mStatusChecker.run();
    }

    void stopRepeatingTask() {
        mHandler.removeCallbacks(mStatusChecker);
    }
    @Override
    protected void onResume() {
        super.onResume();
        isAppInForeground = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        isAppInForeground = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopRepeatingTask();
    }
}
