package com.example.pengzhizhou.meetup;

/**
 * The entry page of this app
 * Created by pengzhizhou on Sep/17/15.
 */

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Space;


public class MainActivity extends Activity {

    private String loginUser = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        SharedPreferences settings = this.getSharedPreferences("MyPrefsFile", 0);
        loginUser = settings.getString("KEY_LOGIN_USER", null);
        super.onCreate(savedInstanceState);

        if (loginUser != null){
            Intent myIntent;
            myIntent = new Intent(this, TabHostActivity.class);
            startActivity(myIntent);

        }else {
            setContentView(R.layout.activity_main);
            int displayHeight = getWindowManager().getDefaultDisplay().getHeight();

            Space spaceTaken = (Space)findViewById(R.id.spaceTaken);
            spaceTaken.getLayoutParams().height = displayHeight * 6 / 10;

        }
    }

    public void onClick1(View v) {
        Intent myIntent;
        myIntent = new Intent(MainActivity.this, RegistrationActivity.class);
        myIntent.putExtra("fromPage", 0);
        startActivity(myIntent);
    }


    public void onClick(View v) {
        Intent myIntent;
        myIntent = new Intent(this, LoginActivity.class);
        myIntent.putExtra("fromPage", 1);
        startActivity(myIntent);
    }

    public void listActivity(View v){
        Intent intent = new Intent(this, TabHostActivity.class);
        startActivity(intent);
    }
}
