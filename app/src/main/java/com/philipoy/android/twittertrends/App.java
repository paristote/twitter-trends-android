package com.philipoy.android.twittertrends;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;

import io.fabric.sdk.android.Fabric;

/**
 * Created by paristote on 1/11/16.
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        TwitterAuthConfig authConfig = new TwitterAuthConfig(Constants.TWITTER_KEY, Constants.TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig), new Crashlytics());
    }

    public static class Constants {
        // Twitter
        private static final String TWITTER_KEY = BuildConfig.CONSUMER_KEY;
        private static final String TWITTER_SECRET = BuildConfig.CONSUMER_SECRET;
        // Collect Tweet Service
        public static final int COLLECT_TWEET_CODE = 10;
        // Storage Service
        public static final String STORAGE_FILE = "digest.json";
        // Location Service
        public static final int LOCATION_PERMISSION_CODE = 20;
    }
}
