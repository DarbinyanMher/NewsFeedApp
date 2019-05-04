package com.example.newsfeedapp.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.newsfeedapp.R;
import com.example.newsfeedapp.model.ListItemNews;
import com.example.newsfeedapp.model.ListItemSavedNews;
import com.example.newsfeedapp.utils.Constants;
import com.squareup.picasso.Picasso;

import io.realm.Realm;
import io.realm.RealmResults;

public class NewsPageActivity extends AppCompatActivity {
    private TextView body, articleTitle, contentCategory;
    private ImageView articleImage;
    private Realm realm;
    private ListItemNews itemNews;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_page);
        initUI();
        realm = Realm.getDefaultInstance();
    }

    private void initUI() {
        itemNews = getIntent().getParcelableExtra(Constants.LISTITEMSNEWS_EXTRA);
        Toolbar toolbar = findViewById(R.id.toolbarNewsPage);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(itemNews.getCategory());
        getSupportActionBar().setSubtitle("news");
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        body = findViewById(R.id.bodyText);
        articleImage = findViewById(R.id.articleImage);
        articleTitle = findViewById(R.id.articleTitle);
        progressBar = findViewById(R.id.progressBar);
        body.setText(Html.fromHtml(itemNews.getBody()).toString());
        articleTitle.setText(itemNews.getTitle());
        body.setMovementMethod(new ScrollingMovementMethod());
        Picasso.with(this)
                .load(itemNews.getImageUrl())
                .into((articleImage));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.news_page_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.addNews) {
            progressBar.setVisibility(View.VISIBLE);
            writeToDB(itemNews);
            Intent intent = new Intent(getBaseContext(), MainActivity.class);
            startActivity(intent);

        }
        return super.onOptionsItemSelected(item);
    }

    public void writeToDB(final ListItemNews listItemNews) {
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(@NonNull  Realm bgRealm) {
                ListItemSavedNews user = bgRealm.createObject(ListItemSavedNews.class, listItemNews.getId() );
               // user.setId(listItemNews.getId());
                user.setCategory(listItemNews.getCategory());
                user.setImageUrl(listItemNews.getImageUrl());
                user.setBodyContent(listItemNews.getBody());
                user.setTitle(listItemNews.getTitle());
        }
    }, new Realm.Transaction.OnSuccess() {
        @Override
        public void onSuccess() {
            progressBar.setVisibility(View.INVISIBLE);
            Snackbar.make(progressBar, R.string.item_successfully, Snackbar.LENGTH_LONG).show();
            RealmResults<ListItemSavedNews> guests = realm.where(ListItemSavedNews.class).findAll();
            Log.v("Database", "Data inserted");
        }
    }, new Realm.Transaction.OnError() {
        @Override
        public void onError(Throwable error) {
            Log.e("Database", error.getMessage());
        }
    });
}
    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }
}
