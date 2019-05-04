package com.example.newsfeedapp.model;

import android.os.Parcel;
import android.os.Parcelable;


public class ListItemNews implements Parcelable {
    private String id;
    private String title, imageUrl, category, body, pageNumber;

public ListItemNews(String id, String title, String imageUrl, String category, String body, String pageNumber) {
    this.id = id;
    this.title = title;
    this.imageUrl = imageUrl;
    this.category = category;
    this.body = body;
    this.pageNumber = pageNumber;
}

    protected ListItemNews(Parcel in) {
        id = in.readString();
        title = in.readString();
        imageUrl = in.readString();
        category = in.readString();
        body = in.readString();
        pageNumber = in.readString();
    }

    public static final Creator<ListItemNews> CREATOR = new Creator<ListItemNews>() {
        @Override
        public ListItemNews createFromParcel(Parcel in) {
            return new ListItemNews(in);
        }

        @Override
        public ListItemNews[] newArray(int size) {
            return new ListItemNews[size];
        }
    };
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return super.toString();
    }

    public String getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(String pageNumber) {
        this.pageNumber = pageNumber;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeString(imageUrl);
        dest.writeString(category);
        dest.writeString(body);
    }
}
