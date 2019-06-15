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

package com.example.android.popularmoviesstage1.utilities;


import android.net.Uri;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

/**
 * These utilities will be used to communicate with the movie servers.
 */

public final class NetworkUtils {

    private static final String TAG = NetworkUtils.class.getSimpleName();

    /**
     * Builds the URL used to talk to the movie server using a sort type. This sort type is based
     * on the query capabilities of the movie provider that we are using.
     *
     * @param sortQuery The type of sort that will be queried for.
     * @return The URL to use to query the movie server.
     */
    public static URL buildQueryUrl(String sortQuery, String apiKey) {
        String queryUrl = "http://api.themoviedb.org/3/movie/"+sortQuery+"?api_key="+apiKey;
        Uri builtUri = Uri.parse(queryUrl);

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        Log.v(TAG, "Built URI " + url);

        return url;
    }

    /**
     * Builds the URL used to talk to the movie server using a sort type. This sort type is based
     * on the query capabilities of the movie provider that we are using.
     *
     * @param movieId The type of sort that will be queried for.
     * @return The URL to use to query the movie server.
     */
    public static URL buildMovieVideoUrl(String movieId, String apiKey) {
        String queryUrl = "http://api.themoviedb.org/3/movie/"+movieId+"/videos?api_key="+apiKey;
        Uri builtUri = Uri.parse(queryUrl);

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        Log.v(TAG, "Built URI " + url);

        return url;
    }

    /**
     * Builds the URL used to talk to the movie server using a sort type. This sort type is based
     * on the query capabilities of the movie provider that we are using.
     *
     * @param movieId The type of sort that will be queried for.
     * @return The URL to use to query the movie server.
     */
    public static URL buildMovieReviewUrl(String movieId, String apiKey) {
        String queryUrl = "http://api.themoviedb.org/3/movie/"+movieId+"/reviews?api_key="+apiKey;
        Uri builtUri = Uri.parse(queryUrl);

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        Log.v(TAG, "Built URI " + url);

        return url;
    }

    /**
     * This method returns the entire result from the HTTP response.
     *
     * @param url The URL to fetch the HTTP response from.
     * @return The contents of the HTTP response.
     * @throws IOException Related to network and stream reading
     */
    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }

}
