package com.example.pengzhizhou.meetup;

import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
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


public class MainActivity extends PlusBaseActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private UserLoginTask mAuthTask = null;
    private String loginUser = null;

    @Override
    protected void onPlusClientRevokeAccess() {

    }

    @Override
    protected void onPlusClientSignIn() {

    }

    @Override
    protected void onPlusClientSignOut() {

    }

    @Override
    protected void onPlusClientBlockingUI(boolean show) {

    }

    @Override
    protected void updateConnectButtonState() {

    }

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

            TextView displayLoginForm = (TextView) findViewById(R.id.loginTrigger);
            displayLoginForm.setPaintFlags(displayLoginForm.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

            TextView registerForm = (TextView) findViewById(R.id.registerTrigger);
            registerForm.setPaintFlags(registerForm.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        }
    }

    public void onClick1(View v) {
        Intent myIntent;
        myIntent = new Intent(MainActivity.this, RegistrationActivity.class);
        startActivity(myIntent);
    }


    public void onClick(View v) {
        LinearLayout space = (LinearLayout) findViewById(R.id.spaceTaken);
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT, 4.0f);
        space.setLayoutParams(param);

        LinearLayout loginLayout = (LinearLayout) findViewById(R.id.login_f);
        Animation slideUp = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up);
        loginLayout.setVisibility(View.VISIBLE);
        loginLayout.startAnimation(slideUp);

        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        populateAutoComplete();

        mPasswordView = (EditText) findViewById(R.id.password);
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

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
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

    private void populateAutoComplete() {
        getLoaderManager().initLoader(0, null, this);
    }

    public void listActivity(View v){
        Intent intent = new Intent(this, TabHostActivity.class);
        startActivity(intent);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

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
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
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
        protected void onPostExecute(final String user) {
            mAuthTask = null;

            if (!user.equals("")) {
                String[] parts = user.split("&&asb##");
                SharedPreferences myPrefs = getSharedPreferences("MyPrefsFile", 0);
                SharedPreferences.Editor editor = myPrefs.edit();
                editor.putString("KEY_LOGIN_USER", parts[0]);
                editor.putString("KEY_LOGIN_USER_ID", parts[1]);
                editor.commit();

                Intent myIntent;
                myIntent = new Intent(MainActivity.this, TabHostActivity.class);
                startActivity(myIntent);
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
        }
    }
}
