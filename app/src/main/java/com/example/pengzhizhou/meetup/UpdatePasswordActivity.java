package com.example.pengzhizhou.meetup;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;


public class UpdatePasswordActivity extends ActionBarActivity {

    private EditText mPasswordView;
    private EditText mPasswordConfirmView;
    private ProgressDialog progressLoading;
    private String url = Utility.getServerUrl() + "update-password-meetup.php";
    private RequestParams params = new RequestParams();
    private String trimEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_password);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(R.layout.activity_update_password_actionbar);
        String email = null;
        Uri uri = getIntent().getData();
        if (uri != null){
            email = uri.getQueryParameter("email");
        }

        trimEmail = email;
        if (email != null && email.contains("<span>")){
            trimEmail = email.replace("<span>", "");
            trimEmail = trimEmail.replace("</span>", "");
            params.put("email", trimEmail);
        }
        else {
            params.put("email", email);
        }

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordConfirmView = (EditText) findViewById(R.id.confirmPassword);
        findViewById(R.id.update_password_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptUpdate();
            }
        });


        findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent;
                myIntent = new Intent(UpdatePasswordActivity.this, ForgetPasswordActivity.class);
                myIntent.putExtra("email", trimEmail);
                startActivity(myIntent);
            }
        });
    }

    public void attemptUpdate(){
        String password = mPasswordView.getText().toString();
        String password1 = mPasswordConfirmView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for valid passwords
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }
        else if (TextUtils.isEmpty(password1)) {
            mPasswordConfirmView.setError(getString(R.string.error_empty_password));
            focusView = mPasswordConfirmView;
            cancel = true;
        }
        else if (!password.equals(password1)){
            mPasswordConfirmView.setError(getString(R.string.error_invalid_password_confirm));
            focusView = mPasswordConfirmView;
            cancel = true;
        }

        if (cancel){
            focusView.requestFocus();
        }
        else{
            params.put("password", password);
            triggerUpdatePassword();
        }
    }

    public void triggerUpdatePassword() {
        makeHTTPCall();
    }

    public void makeHTTPCall() {
        progressLoading = new ProgressDialog(this);
        progressLoading = ProgressDialog.show(this, "", "更改密码...");
        AsyncHttpClient client = new AsyncHttpClient();
        // Don't forget to change the IP address to your LAN address. Port no as well.
        client.post(url,  params, new AsyncHttpResponseHandler() {
            // When the response returned by REST has Http
            // response code '200'
            @Override
            public void onSuccess(String response) {
                // Hide Progress Dialog
                Utility.dismissProgressDialog(progressLoading);
                if (!response.equals("")) {
                    Toast.makeText(getApplicationContext(), "更改成功",
                            Toast.LENGTH_LONG).show();
                    String[] parts = response.split("&&asb##");
                    SharedPreferences myPrefs = getSharedPreferences("MyPrefsFile", 0);
                    SharedPreferences.Editor editor = myPrefs.edit();
                    editor.putString("KEY_LOGIN_USER", parts[0]);
                    editor.putString("KEY_LOGIN_USER_ID", parts[1]);
                    editor.commit();

                    Intent myIntent;
                    myIntent = new Intent(UpdatePasswordActivity.this, TabHostActivity.class);
                    startActivity(myIntent);
                }
                else {
                    Toast.makeText(getApplicationContext(), "更改密码发生错误",
                            Toast.LENGTH_LONG).show();
                }
            }

            // When the response returned by REST has Http
            // response code other than '200' such as '404',
            // '500' or '403' etc
            @Override
            public void onFailure(int statusCode, Throwable error,
                                  String content) {
                Utility.dismissProgressDialog(progressLoading);
                // When Http response code is '404'
                if (statusCode == 404) {
                    Toast.makeText(getApplicationContext(),
                            "Requested resource not found",
                            Toast.LENGTH_LONG).show();
                }
                // When Http response code is '500'
                else if (statusCode == 500) {
                    Toast.makeText(getApplicationContext(),
                            "Something went wrong at server end",
                            Toast.LENGTH_LONG).show();
                }
                // When Http response code other than 404, 500
                else {
                    Toast.makeText(
                            getApplicationContext(),
                            "Error Occured \n Most Common Error: \n1. Device not connected to Internet\n2. Web App is not deployed in App server\n3. App server is not running\n HTTP Status code : "
                                    + statusCode, Toast.LENGTH_LONG)
                            .show();
                }
            }
        });
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }


}
