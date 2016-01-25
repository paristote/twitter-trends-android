package com.philipoy.android.twittertrends;

import com.philipoy.android.twittertrends.model.Location;
import com.philipoy.android.twittertrends.model.TrendsResult;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Session;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.models.Search;
import com.twitter.sdk.android.core.services.params.Geocode;

import java.util.List;

import retrofit.http.EncodedQuery;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by paristote on 1/12/16.
 */
public class ExtendedTwitterApiClient extends TwitterApiClient {

    public ExtendedTwitterApiClient(Session session) {
        super(session);
    }

    public TrendsService getTrendsService() {
        return getService(TrendsService.class);
    }

    public SearchService getSyncSearchService() {
        return getService(SearchService.class);
    }

    public interface TrendsService {

        // Async call to closest.json API
        @GET("/1.1/trends/closest.json")
        void closest(@Query("lat") long latitude, @Query("long") long longitude, Callback<List<Location>> cb);

        // Synchronous call to closest.json API
        @GET("/1.1/trends/closest.json")
        List<Location> closest(@Query("lat") long latitude, @Query("long") long longitude);

        // Async call to place.json API
        @GET("/1.1/trends/place.json")
        void place(@Query("id") long woeid, Callback<List<TrendsResult>> cb);

        // Synchronous call to place.json API
        @GET("/1.1/trends/place.json")
        List<TrendsResult> place(@Query("id") long woeid);
    }

    public interface SearchService {

        // Synchronous call to the search service
        @GET("/1.1/search/tweets.json")
        Search tweets(@Query("q") String var1, @EncodedQuery("geocode") Geocode var2, @Query("lang") String var3, @Query("locale") String var4, @Query("result_type") String var5, @Query("count") Integer var6, @Query("until") String var7, @Query("since_id") Long var8, @Query("max_id") Long var9, @Query("include_entities") Boolean var10);
    }
}
