package movieparser.example.android.popularmoviesstage1.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;

@Entity(tableName = "favorite")
public class FavoriteEntry implements Parcelable {

    @PrimaryKey(autoGenerate = false)
    private int id;
    private String title;
    private String imageUrl;
    private String overview;
    private int vote_average;
    private String release_date;

    public FavoriteEntry(int id, String title, String imageUrl, String overview, int vote_average,
                         String release_date) {
        this.id = id;
        this.title = title;
        this.imageUrl = imageUrl;
        this.overview = overview;
        this.vote_average = vote_average;
        this.release_date = release_date;

    }

    protected FavoriteEntry(Parcel in) {
        id = in.readInt();
        title = in.readString();
        imageUrl = in.readString();
        overview = in.readString();
        vote_average = in.readInt();
        release_date = in.readString();
    }

    public static final Creator<FavoriteEntry> CREATOR = new Creator<FavoriteEntry>() {
        @Override
        public FavoriteEntry createFromParcel(Parcel in) {
            return new FavoriteEntry(in);
        }

        @Override
        public FavoriteEntry[] newArray(int size) {
            return new FavoriteEntry[size];
        }
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
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

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public int getVote_average() {
        return vote_average;
    }

    public void setVote_average(int vote_average) {
        this.vote_average = vote_average;
    }

    public String getRelease_date() {
        return release_date;
    }

    public void setRelease_date(String release_date) {
        this.release_date = release_date;
    }


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
