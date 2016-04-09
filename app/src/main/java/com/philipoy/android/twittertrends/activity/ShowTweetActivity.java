package com.philipoy.android.twittertrends.activity;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import com.philipoy.android.twittertrends.App;
import com.philipoy.android.twittertrends.Logger;
import com.philipoy.android.twittertrends.R;
import com.philipoy.android.twittertrends.service.NotificationService;
import com.philipoy.android.twittertrends.service.PopularTrendingTweetService;
import com.philipoy.android.twittertrends.storage.StorageHelper;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.tweetui.CompactTweetView;
import com.twitter.sdk.android.tweetui.TweetViewFetchAdapter;

import org.json.JSONException;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class ShowTweetActivity extends AppCompatActivity {

    private ListView mTweetsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_tweet);

        requestLocationPermission();
        setUpTweetServiceAlarm();
        setUpNotificationAlarm();
        setUpViews();
        setUpTweetList();
    }

    /**
     * Calls the PopularTrendingTweetService every hour
     */
    private void setUpTweetServiceAlarm() {
        // TODO : Only create the alarm if it does not already exist
        Intent getTweetIntent = new Intent(this, PopularTrendingTweetService.class);
        PendingIntent alarmIntent = PendingIntent.getService(this, App.Constants.COLLECT_TWEET_CODE, getTweetIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Calendar cal = Calendar.getInstance();
        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), AlarmManager.INTERVAL_HOUR, alarmIntent);
    }

    /**
     * Calls the NotificationService every day at around 8pm
     */
    private void setUpNotificationAlarm() {
        // TODO : Only create the alarm if it does not already exist
        Intent notificationIntent = new Intent(this, NotificationService.class);
        PendingIntent alarmIntent = PendingIntent.getService(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        cal.set(Calendar.HOUR_OF_DAY, 20);
        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), AlarmManager.INTERVAL_DAY, alarmIntent);
    }

    private void setUpViews() {

        mTweetsList = (ListView) findViewById(R.id.tweets_list_view);

    }

    private void setUpTweetList() {

        try {

            Date today = Calendar.getInstance().getTime();
            List<Long> tweets = new StorageHelper(ShowTweetActivity.this).getTweetsOfDay(today);

            if (tweets.isEmpty()) {
                Toast.makeText(ShowTweetActivity.this, "No tweets to display at this time", Toast.LENGTH_LONG).show();
                return;
            }

            TweetViewFetchAdapter<CompactTweetView> adapter =
                    new TweetViewFetchAdapter<CompactTweetView>(ShowTweetActivity.this);

            adapter.setTweetIds(tweets, new Callback<List<Tweet>>() {
                @Override
                public void success(Result<List<Tweet>> result) {
                }

                @Override
                public void failure(TwitterException e) {
                    Toast.makeText(ShowTweetActivity.this, "Could not load tweets", Toast.LENGTH_LONG).show();
                }
            });
            mTweetsList.setAdapter(adapter);

        } catch (JSONException | IOException e) {
            Logger.e("TWEET", e.getMessage(), e);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_show_tweet, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            Twitter.logOut();
            Intent i = new Intent(this, TwitterLoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    /*
        LOCATION PERMISSION
     */

    private void requestLocationPermission() {

        int permissionCheck = ContextCompat.checkSelfPermission(ShowTweetActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION);
        if ((permissionCheck == PackageManager.PERMISSION_GRANTED)) {

            Toast.makeText(this, "Location activated", Toast.LENGTH_SHORT).show();

        } else {

            ActivityCompat.requestPermissions(ShowTweetActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, App.Constants.LOCATION_PERMISSION_CODE);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {

            case App.Constants.LOCATION_PERMISSION_CODE:

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Location activated", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Cannot load tweets, need the location of the device.", Toast.LENGTH_LONG).show();
                    finish();
                }

                break;

            default:
                break;
        }
    }
}
