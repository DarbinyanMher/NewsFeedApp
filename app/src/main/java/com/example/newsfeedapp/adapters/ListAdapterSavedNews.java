package com.example.newsfeedapp.adapters;

import android.content.Context;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.example.newsfeedapp.R;
import com.example.newsfeedapp.model.ListItemSavedNews;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;


public class ListAdapterSavedNews  extends RecyclerView.Adapter<ListAdapterSavedNews.ListViewHolder>{
    private Context context;
    private ArrayList<ListItemSavedNews> newsFeedList;
    private ListAdapterNews.onItemClick onItemClick;


    public ListAdapterSavedNews(Context context, ArrayList<ListItemSavedNews> NewsFeedList, ListAdapterNews.onItemClick onItemClick) {
        this.context = context;
        this.newsFeedList = NewsFeedList;
        this.onItemClick = onItemClick;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @NonNull
    @Override
    public ListViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ListViewHolder(LayoutInflater.from(context).inflate(R.layout.list_item_saved_news, viewGroup,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ListViewHolder listViewHolder, int i) {
        listViewHolder.bindView(newsFeedList.get(i));
    }

    public void updateList(ArrayList<ListItemSavedNews> newList) {
        newsFeedList = newList;
        notifyDataSetChanged();
    }
    @Override
    public int getItemCount() {
        return newsFeedList.size();
    }

    class ListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
       private TextView newsTitle;
       private CircleImageView newsImage;



        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        public ListViewHolder (View itemView) {
            super(itemView);
            newsTitle = itemView.findViewById(R.id.savedArticleTitle);
            newsImage = itemView.findViewById(R.id.savedArticleImage);
            itemView.setOnClickListener(this);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClick.onClick(v, getAdapterPosition());
                }
            });
        }

        public void bindView (ListItemSavedNews item) {
            newsTitle.setText((Html.fromHtml(item.getCategory()).toString()));
            Picasso.with(context)
                    .load(item.getImageUrl())
                    .into(newsImage);
        }

        public void onClick(View view) {

        }
    }
}
