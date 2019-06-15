package com.example.android.popularmoviesstage1;

import android.os.Parcel;
import android.os.Parcelable;
import android.widget.ImageView;

public class MovieItem implements Parcelable {

    int id;
    String title;
    String imageUrl;
    String overview;
    int vote_average;
    String release_date;

    public MovieItem(int id, String vTitle, String vImageUrl, String vOverview, int vote_average,
                     String release_date) {

        this.id = id;
        this.title = vTitle;
        this.imageUrl = vImageUrl;
        this.overview = vOverview;
        this.vote_average = vote_average;
        this.release_date = release_date;
    }

    protected MovieItem(Parcel in) {
        id = in.readInt();
        title = in.readString();
        imageUrl = in.readString();
        overview = in.readString();
        vote_average = in.readInt();
        release_date = in.readString();
    }

    public static final Creator<MovieItem> CREATOR = new Creator<MovieItem>() {
        @Override
        public MovieItem createFromParcel(Parcel in) {
            return new MovieItem(in);
        }

        @Override
        public MovieItem[] newArray(int size) {
            return new MovieItem[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(title);
        parcel.writeString(imageUrl);
        parcel.writeString(overview);
        parcel.writeInt(vote_average);
        parcel.writeString(release_date);
    }
}
