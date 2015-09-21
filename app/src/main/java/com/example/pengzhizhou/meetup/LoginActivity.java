package com.example.pengzhizhou.meetup;

/**
 * Implement the login function
 * Created by pengzhizhou on Sep/17/15.
 */

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;


public class LoginActivity extends ActionBarActivity {

    private UserLoginTask mAuthTask = null;
    private AutoCompleteTextView mEmailView;
    private AutoCompleteTextView mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    private String titleText = null;
    private String imageNameText = null;
    private String eventTimeText = null;
    private String addressText = null;
    private String eventID = null;
    private String originalActivity = null;
    private int fromPage = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(R.layout.activity_login_actionbar);

        Intent iin= getIntent();
        Bundle b = iin.getExtras();
        if ( b != null ) {
            titleText = (String) b.get("itemTitle");
            imageNameText = (String) b.get("itemImage");
            eventTimeText = (String) b.get("eventTime");
            addressText = (String) b.get("itemAddress");
            eventID = (String) b.get("itemId");
            originalActivity = (String) b.get("originActivity");
            fromPage = b.getInt("fromPage", 0);
        }

        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);

        mPasswordView = (AutoCompleteTextView) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        findViewById(R.id.sign_in).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        TextView registerForm = (TextView)findViewById(R.id.goLogin);
        registerForm.setPaintFlags(registerForm.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);

        findViewById(R.id.forget_password).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent;
                myIntent = new Intent(LoginActivity.this, ForgetPasswordActivity.class);
                myIntent.putExtra("fromPage", 1);
                startActivity(myIntent);
            }
        });

        findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent;
                if (fromPage == 1){
                    myIntent = new Intent(LoginActivity.this, MainActivity.class);
                }
                else {
                    myIntent = new Intent(LoginActivity.this, TabHostActivity.class);
                }

                startActivity(myIntent);
            }
        });

    }

    public void onClick(View v) {
        Intent myIntent;
        myIntent = new Intent(LoginActivity.this, RegistrationActivity.class);
        myIntent.putExtra("fromPage", 1);
        startActivity(myIntent);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;


        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute();
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<String,Void,String> {

        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected String doInBackground(String... params) {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(Utility.getServerUrl() + "login-meetup.php");
            String result = null;
            //add name value pair for the country code
            List<NameValuePair> nameValuePairs = new ArrayList<>(2);
            nameValuePairs.add(new BasicNameValuePair("email",String.valueOf(mEmail)));
            nameValuePairs.add(new BasicNameValuePair("password",String.valueOf(mPassword)));
            try {
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            } catch (UnsupportedEncodingException e) {
                return e.toString();
            }

            try {
                HttpResponse response = httpclient.execute(httppost);
                result = Utility.inputStreamToString(
                        response.getEntity().getContent()).toString();
            }

            catch (ClientProtocolException e) {
                return e.toString();
            } catch (IOException e) {
                return e.toString();
            }

            return result;
        }

        @Override
        protected void onPostExecute(final String response) {
            mAuthTask = null;
            showProgress(false);

            if (response.contains("&&asb##")) {
                String[] parts = response.split("&&asb##");
                SharedPreferences settings = getSharedPreferences("MyPrefsFile", 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("KEY_LOGIN_USER", parts[0]);
                editor.putString("KEY_LOGIN_USER_ID", parts[1]);
                editor.commit();

                Intent myIntent;
                if (originalActivity != null && originalActivity.equals("EventDetailActivity")){
                    myIntent = new Intent(LoginActivity.this, EventDetailActivity.class);
                    myIntent.putExtra("itemTitle",titleText);
                    myIntent.putExtra("itemImage", imageNameText);
                    myIntent.putExtra("eventTime", eventTimeText);
                    myIntent.putExtra("itemAddress", addressText);
                    myIntent.putExtra("itemId", eventID);
                }
                else{
                    myIntent = new Intent(LoginActivity.this, TabHostActivity.class);
                }

                startActivity(myIntent);
            } else {
                if (response.equals("error 1")){
                    mEmailView.setError("此邮箱不存在于本系统");
                    mEmailView.requestFocus();

                }
                else if (response.equals("error 2")){
                    mPasswordView.setError("用户名和密码不匹配");
                    mPasswordView.requestFocus();
                }

            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

}



