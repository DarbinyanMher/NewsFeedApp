package com.example.newsfeedapp.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.newsfeedapp.R;
import com.example.newsfeedapp.model.ListItemSavedNews;
import com.squareup.picasso.Picasso;

import io.realm.Realm;

public class SavedPageActivity extends AppCompatActivity {
    private TextView body, articleTitle, contentCategory;
    private ImageView articleImage;
    private Realm realm;
    private String primaryID;
    private ListItemSavedNews person;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_page);
        realm = Realm.getDefaultInstance();
        initUI();

    }
private void initUI(){
    toolbar = findViewById(R.id.toolbarSavedPage);
    setSupportActionBar(toolbar);
    toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white);
    toolbar.setNavigationOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            finish();
        }
    });
    body = findViewById(R.id.savedBodyText);
    articleTitle = findViewById(R.id.savedArticleTitle);
    articleImage = findViewById(R.id.savedArticleImage);
    primaryID = getIntent().getStringExtra("primaryKey");
    Realm realm = Realm.getDefaultInstance();
    try {
        person = realm.where(ListItemSavedNews.class).equalTo("id", primaryID).findFirst();
        body.setText(Html.fromHtml(person.getBodyContent()));
        getSupportActionBar().setTitle(person.getCategory());
        articleTitle.setText(person.getTitle());
        Picasso.with(this)
                .load(person.getImageUrl())
                .into((articleImage));
    } finally {
        realm.close();
    }
}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.saved_page_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.deleteNews) {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    person.deleteFromRealm();
                }
            });
            Intent intent = new Intent(getBaseContext(), MainActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }


}
