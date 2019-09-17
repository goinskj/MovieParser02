/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package movieparser.example.android.popularmoviesstage1.utilities;

import android.content.Context;

import movieparser.example.android.popularmoviesstage1.MovieItem;
import movieparser.example.android.popularmoviesstage1.ReviewItem;
import movieparser.example.android.popularmoviesstage1.VideoItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Utility functions to handle product JSON data.
 */

public class MovieJsonUtils {

    private static final String LOG_TAG = MovieJsonUtils.class.getSimpleName();

    /**
     * This method parses JSON from a web response and returns an array of Strings
     * describing the movies over various sort criteria.
     * <p/>
     *
     * @param moviesJsonStr JSON response from server
     *
     * @return Array of Strings describing movie data
     *
     * @throws JSONException If JSON data cannot be properly parsed
     */

    public static ArrayList<MovieItem> getMovieStringsFromJson(Context context, String moviesJsonStr)
            throws JSONException {

        /* String array to hold each movie item */
        ArrayList<MovieItem> parsedMovieData = new ArrayList<MovieItem>();

        JSONObject movieJson = new JSONObject(moviesJsonStr);

        if (movieJson != null) {

            JSONArray movieArray = movieJson.getJSONArray("results");


            for (int i = 0; i < movieArray.length() - 1; i++) {

                JSONObject item = movieArray.getJSONObject(i);

                int id = item.getInt("id");
                String title = item.getString("title");
                String poster_path = item.getString("poster_path");
                String overview = item.getString("overview");
                int vote_average = item.getInt("vote_average");
                String release_date = item.getString("release_date");

                parsedMovieData.add(new MovieItem(id, title, poster_path, overview, vote_average
                        , release_date));
            }

        } else {
            return null;
        }

        return parsedMovieData;

    }

    public static ArrayList<VideoItem> getVideoStringsFromJson(Context context, String moviesVideoJsonStr)
            throws JSONException {

        /* String array to hold each movie item */
        ArrayList<VideoItem> parsedMovieVideoData = new ArrayList<VideoItem>();

        JSONObject movieVideoJson = new JSONObject(moviesVideoJsonStr);

        if (movieVideoJson != null) {

            JSONArray movieArray = movieVideoJson.getJSONArray("results");


            for (int i = 0; i < movieArray.length() - 1; i++) {

                JSONObject item = movieArray.getJSONObject(i);

                int order = i+1;
                String id = item.getString("id");
                String key = item.getString("key");


                parsedMovieVideoData.add(new VideoItem(i, id, key));
            }

        } else {
            return null;
        }

        return parsedMovieVideoData;

    }

    public static ArrayList<ReviewItem> getReviewStringsFromJson(Context context, String moviesReviewJsonStr)
            throws JSONException {

        /* String array to hold each movie item */
        ArrayList<ReviewItem> parsedMovieReviewData = new ArrayList<ReviewItem>();

        JSONObject movieReviewJson = new JSONObject(moviesReviewJsonStr);

        if (moviesReviewJsonStr != null) {

            JSONArray movieArray = movieReviewJson.getJSONArray("results");


            for (int i = 0; i < movieArray.length() - 1; i++) {

                JSONObject item = movieArray.getJSONObject(i);

                String author = item.getString("author");
                String content = item.getString("content");


                parsedMovieReviewData.add(new ReviewItem(author, content));
            }

        } else {
            return null;
        }

        return parsedMovieReviewData;

    }
}
