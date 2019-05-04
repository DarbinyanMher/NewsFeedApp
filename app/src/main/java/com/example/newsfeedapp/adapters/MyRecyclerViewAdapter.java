package com.example.newsfeedapp.adapters;


import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.newsfeedapp.R;
import com.example.newsfeedapp.activities.MainActivity;
import com.example.newsfeedapp.model.ListItemSavedNews;

import java.util.HashSet;
import java.util.Set;

import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

 public class MyRecyclerViewAdapter extends RealmRecyclerViewAdapter<ListItemSavedNews, MyRecyclerViewAdapter.MyViewHolder> {

    private boolean inDeletionMode = false;
    private Set<Integer> countersToDelete = new HashSet<>();

    public MyRecyclerViewAdapter(MainActivity mainActivity, OrderedRealmCollection<ListItemSavedNews> data) {
        super(data, true);
        // Only set this if the model class has a primary key that is also a integer or long.
        // In that case, {@code getItemId(int)} must also be overridden to return the key.
        // See https://developer.android.com/reference/android/support/v7/widget/RecyclerView.Adapter.html#hasStableIds()
        // See https://developer.android.com/reference/android/support/v7/widget/RecyclerView.Adapter.html#getItemId(int)
        setHasStableIds(true);
    }

    void enableDeletionMode(boolean enabled) {
        inDeletionMode = enabled;
        if (!enabled) {
            countersToDelete.clear();
        }
        notifyDataSetChanged();
    }

    Set<Integer> getCountersToDelete() {
        return countersToDelete;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_saved_news, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        final ListItemSavedNews obj = getItem(position);
        holder.data = obj;
      //  final int itemId = obj.getId();
        //noinspection ConstantConditions
        holder.title.setText(obj.getCategory());

//        if (inDeletionMode) {
//            holder.deletedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//                @Override
//                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                    if (isChecked) {
//                        countersToDelete.add(itemId);
//                    } else {
//                        countersToDelete.remove(itemId);
//                    }
//                }
//            });
//        } else {
//            holder.deletedCheckBox.setOnCheckedChangeListener(null);
//        }
//        holder.deletedCheckBox.setVisibility(inDeletionMode ? View.VISIBLE : View.GONE);
    }

//    @Override
//    public long getItemId(int index) {
//        //noinspection ConstantConditions
//        return getItem(index).getId();
//    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView category;
//        CheckBox deletedCheckBox;
        public ListItemSavedNews data;

        MyViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.savedArticleTitle);
            //deletedCheckBox = view.findViewById(R.id.checkBox);
        }
    }
}