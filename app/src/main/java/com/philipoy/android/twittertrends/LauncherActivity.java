package com.philipoy.android.twittertrends;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.philipoy.android.twittertrends.activity.ShowTweetActivity;
import com.philipoy.android.twittertrends.activity.TwitterLoginActivity;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterSession;

public class LauncherActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_launcher);

        TwitterSession session = Twitter.getSessionManager().getActiveSession();
        if (session == null) {
            // no session => login
            startActivity(new Intent(this, TwitterLoginActivity.class));
        } else {
            // user logged-in => show tweet
            startActivity(new Intent(this, ShowTweetActivity.class));
        }
        finish();
    }
}
