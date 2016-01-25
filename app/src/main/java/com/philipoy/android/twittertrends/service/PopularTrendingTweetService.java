package com.philipoy.android.twittertrends.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;

import com.philipoy.android.twittertrends.BuildConfig;
import com.philipoy.android.twittertrends.ExtendedTwitterApiClient;
import com.philipoy.android.twittertrends.Logger;
import com.philipoy.android.twittertrends.model.Trend;
import com.philipoy.android.twittertrends.model.TrendsResult;
import com.philipoy.android.twittertrends.storage.StorageHelper;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.models.Search;
import com.twitter.sdk.android.core.models.Tweet;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * helper methods.
 */
public class PopularTrendingTweetService extends IntentService {

    private final String LOG_TAG = "TWEET_SERVICE";

    public PopularTrendingTweetService() {
        super("PopularTrendingTweetService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        try {
            /*
                STEP 1 : Get the user's location coordinates
            */
            Location currentLocation = getLocation();
            /*
                STEP 2 : Get the Where On Earth ID of the current location
            */
            long woeid;
//            if (BuildConfig.DEBUG) {
//                Logger.d(LOG_TAG, "*** Faking San Francisco location");
//                woeid = getLocationWOEID(37.781157, -122.400612831116);
//            } else
                woeid = getLocationWOEID(currentLocation.getLatitude(), currentLocation.getLongitude());
            /*
                STEP 3 : Get the most popular trend for this location
             */
            Trend trend = getPopularTrend(woeid);
            /*
                STEP 4 : Get the most popular tweet for this trend
             */
            long tweetId = getTweetId(trend.query);
            /*
                STEP 5 : Save the tweet id in storage for today
             */
            saveTweetId(tweetId);
            /*
                DONE
             */
        } catch (TweetServiceException e) {
            endWithFailure(e);
        }
    }

    /**
     * STEP 1 : Get the user's location coordinates
     *
     * @return
     * @throws TweetServiceException
     */
    private Location getLocation() throws TweetServiceException {

        Logger.d(LOG_TAG, "*** STARTED Get Location...");

        final LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        try {
            Location loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            Logger.d(LOG_TAG, "*** DONE Get Location: " + loc.toString());
            return loc;
        } catch (SecurityException e) {
            throw new TweetServiceException("Could not get the current location coordinates", e);
        }
    }

    /**
     * Get the Where On Earth ID of the current location
     *
     * @param latitude
     * @param longitude
     * @return
     * @throws TweetServiceException
     */
    private long getLocationWOEID(double latitude, double longitude) throws TweetServiceException {

        Logger.d(LOG_TAG, "*** STARTED Get WOEID...");

        ExtendedTwitterApiClient trendsClient = new ExtendedTwitterApiClient(Twitter.getSessionManager().getActiveSession());
        List<com.philipoy.android.twittertrends.model.Location> locations = trendsClient.getTrendsService().closest((long) latitude, (long) longitude);
        if (locations != null && !locations.isEmpty()) {
            com.philipoy.android.twittertrends.model.Location loc = locations.get(0);
            Logger.d(LOG_TAG, "*** DONE WOEID: " + loc.woeid);
            return loc.woeid;
        } else {
            throw new TweetServiceException("Could not get the WOEID from Twitter's Trends service");
        }
    }

    /**
     * Get the most popular trend for this location
     *
     * @param woeid
     * @return
     * @throws TweetServiceException
     */
    private Trend getPopularTrend(long woeid) throws TweetServiceException {

        Logger.d(LOG_TAG, "*** STARTED Get Trends...");

        ExtendedTwitterApiClient trendsClient = new ExtendedTwitterApiClient(Twitter.getSessionManager().getActiveSession());
        List<TrendsResult> results = trendsClient.getTrendsService().place(woeid);
        if (results != null && !results.isEmpty()) {
            List<Trend> trends = new ArrayList<>(results.get(0).trends);
            if (trends != null && !trends.isEmpty()) {
                // Order results by tweet volume desc
                Collections.sort(trends, Collections.reverseOrder());
                Trend popularTrend = trends.get(0);

                Logger.d(LOG_TAG, "*** DONE Get most popular trend: " + popularTrend.name);

                return popularTrend;
            }
        }
        throw new TweetServiceException("Could not get the trends from Twitter's Trends service");
    }

    /**
     * STEP 4 : Get the most popular tweet for this trend
     *
     * @param query
     * @return
     * @throws TweetServiceException
     */
    private long getTweetId(String query) throws TweetServiceException {

        Logger.d(LOG_TAG, "*** STARTED Get Tweet...");

        ExtendedTwitterApiClient trendsClient = new ExtendedTwitterApiClient(Twitter.getSessionManager().getActiveSession());
        ExtendedTwitterApiClient.SearchService searchService = trendsClient.getSyncSearchService();
        Search search = searchService.tweets(query, null, null, null, "popular", 1, null, null, null, null);
        if (search != null && search.tweets != null && !search.tweets.isEmpty()) {
            Tweet t = search.tweets.get(0);

            Logger.d(LOG_TAG, "*** DONE Get popular trending tweet: " + t.text);

            return t.getId();
        }
        throw new TweetServiceException("Could not get the tweets from Twitter's Search service");

    }


    /**
     * STEP 5 : Save the tweet id in storage for today
     *
     * @param tweetId
     * @throws TweetServiceException
     */
    private boolean saveTweetId(long tweetId) throws TweetServiceException {
        Logger.d(LOG_TAG, "*** STARTED Save to storage...");

        StorageHelper helper = new StorageHelper(getApplicationContext());
        try {
            Date today = Calendar.getInstance().getTime();
            boolean saved = helper.saveTweetIdForDay(today, tweetId);
            Logger.d(LOG_TAG, "*** DONE Save to storage:");
            return saved;
        } catch (IOException | JSONException e) {
            throw new TweetServiceException("Could not save the tweet id to storage", e);
        }
    }

    /*
        MISC
     */

    private void endWithFailure(Exception e) {
        Logger.e(LOG_TAG, e.getMessage(), e);
    }


    private class TweetServiceException extends Exception {
        public TweetServiceException(String message) {
            super(message);
        }

        public TweetServiceException(String message, Exception e) {
            super(message, e);
        }
    }

}
