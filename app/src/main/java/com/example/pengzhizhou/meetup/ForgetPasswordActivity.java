package com.example.pengzhizhou.meetup;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;


public class ForgetPasswordActivity extends ActionBarActivity {
    private AutoCompleteTextView mEmailView;
    private String url = Utility.getServerUrl() + "forget-password-meetup.php";
    private RequestParams params = new RequestParams();
    private View focusView = null;
    private int fromPage = 0;
    private String email = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(R.layout.activity_forget_password_actionbar);

        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);

        Bundle b = getIntent().getExtras();
        if (b != null){
            mEmailView.setText(b.getString("email"));
            fromPage = b.getInt("fromPage", 0);

        }

        findViewById(R.id.forget_password_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadEmail();
            }
        });

        findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent;
                if (fromPage == 0) {
                    myIntent = new Intent(ForgetPasswordActivity.this, MainActivity.class);
                }
                else {
                    myIntent = new Intent(ForgetPasswordActivity.this, LoginActivity.class);
                }
                startActivity(myIntent);
            }
        });
    }

    private void uploadEmail() {
        boolean cancel = false;
        email = mEmailView.getText().toString();

        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        } else {
            params.put("email", email);
        }

        if (cancel) {
            focusView.requestFocus();
        }
        else {
            triggerUploadEmail();
        }

    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    public void triggerUploadEmail() {
        makeHTTPCall();
    }

    public void makeHTTPCall() {
        AsyncHttpClient client = new AsyncHttpClient();
        client.setTimeout(30000);
        // Don't forget to change the IP address to your LAN address. Port no as well.
        client.post(url,  params, new AsyncHttpResponseHandler() {
            // When the response returned by REST has Http
            // response code '200'
            @Override
            public void onSuccess(String response) {
                String parts = null;
                if (response.contains("&&asb##")) {
                    parts = response.split("&&asb##")[1];
                }

                if (parts!=null && isInteger(parts)) {
                    for (int i=0; i < 2; i++) {
                        Toast.makeText(getApplicationContext(), "修改新密码请求已发送到此邮箱地址：" + email + "," +
                                        " 请登陆您的邮箱查看修改密码链接",
                                Toast.LENGTH_LONG).show();
                    }

                } else {
                    Toast.makeText(getApplicationContext(), "您所输入的邮箱账号不存在，请重新输入",
                            Toast.LENGTH_LONG).show();
                }

            }

            // When the response returned by REST has Http
            // response code other than '200' such as '404',
            // '500' or '403' etc
            @Override
            public void onFailure(int statusCode, Throwable error,
                                  String content) {
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

            private boolean isInteger(String s) {
                try {
                    Integer.parseInt(s);
                } catch(NumberFormatException e) {
                    return false;
                } catch(NullPointerException e) {
                    return false;
                }
                return true;
            }
        });
    }

}
