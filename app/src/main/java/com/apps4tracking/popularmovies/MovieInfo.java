package com.apps4tracking.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

public class MovieInfo implements Parcelable {
    String id;
    String original_title;
    String poster_path;
    String overview;
    String vote_average;
    String release_date;

    MovieInfo (String id,
               String original_title,
               String poster_path,
               String overview,
               String vote_average,
               String release_date
    ) {
        this.id = id;
        this.original_title = original_title;
        this.poster_path = poster_path;
        this.overview = overview;
        this.vote_average = vote_average;
        this.release_date = release_date;
    }

    private MovieInfo(Parcel in) {
        id = in.readString();
        original_title = in.readString();
        poster_path = in.readString();
        overview = in.readString();
        vote_average = in.readString();
        release_date = in.readString();
    }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(original_title);
        dest.writeString(poster_path);
        dest.writeString(overview);
        dest.writeString(vote_average);
        dest.writeString(release_date);
    }

    public static final Parcelable.Creator<MovieInfo> CREATOR = new Parcelable.Creator<MovieInfo>() {
        @Override
        public MovieInfo createFromParcel(Parcel source) { return new MovieInfo(source); }

        @Override
        public MovieInfo[] newArray(int size) { return new MovieInfo[size]; }
    };
}
