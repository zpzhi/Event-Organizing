package com.example.pengzhizhou.meetup;
/**
 * Registration implementation
 * Created by pengzhizhou on Sep/17/15.
 */
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class RegistrationActivity extends ActionBarActivity{

    // UI references.
    private AutoCompleteTextView mEmailView;
    private AutoCompleteTextView mUserNameView;
    private AutoCompleteTextView mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private String url = Utility.getServerUrl() + "register-meetup.php";
    private RequestParams params = new RequestParams();
    private String imgPath, imgfileName;
    private ProgressDialog prgDialog;
    private String encodedString;
    private Bitmap bm = null;
    private Uri outputFileUri;
    private ImageView imgView;
    private int fromPage = 0;

    private static int RESULT_LOAD_IMG = 1;
    private static final int CROP_FROM_CAMERA = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(R.layout.activity_registration_actionbar);

        Bundle b = getIntent().getExtras();
        if (b != null){
            fromPage = b.getInt("fromPage", 0);

        }

        prgDialog = new ProgressDialog(this);
        // Set Cancelable as False
        prgDialog.setCancelable(false);

        // Set up the Registration form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        mUserNameView = (AutoCompleteTextView) findViewById(R.id.username);

        mPasswordView = (AutoCompleteTextView) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptRegister();
                    return true;
                }
                return false;
            }
        });

        //Button uploadImg = (Button) findViewById(R.id.uploadImg);
        imgView = (ImageView) findViewById(R.id.imgView);
        imgView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                // Start the Intent
                // startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
                if(bm!=null)
                {
                    bm.recycle();
                    bm=null;
                }
                openImageIntent();
            }
        });

        findViewById(R.id.email_register_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptRegister();
            }
        });

        findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent;
                if (fromPage == 0) {
                    myIntent = new Intent(RegistrationActivity.this, MainActivity.class);
                }
                else {
                    myIntent = new Intent(RegistrationActivity.this, LoginActivity.class);
                }
                startActivity(myIntent);
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    // duplicate codes with PostActivityDetail when upload image
    private void openImageIntent() {

        // Determine Uri of camera image to save.
        final File root = new File(Environment.getExternalStorageDirectory() + File.separator + "MyDir" + File.separator);
        root.mkdirs();
        final String fname = "img_"+ System.currentTimeMillis() + ".jpg";
        final File sdImageMainDirectory = new File(root, fname);
        outputFileUri = Uri.fromFile(sdImageMainDirectory);

        // Camera.
        final List<Intent> cameraIntents = new ArrayList<Intent>();
        final Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        final PackageManager packageManager = getPackageManager();
        final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
        for(ResolveInfo res : listCam) {
            final String packageName = res.activityInfo.packageName;
            final Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(packageName);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            cameraIntents.add(intent);
        }

        // Filesystem.
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        // Chooser of filesystem options.
        final Intent chooserIntent = Intent.createChooser(galleryIntent, "Select Source");

        // Add the camera options.
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[cameraIntents.size()]));

        startActivityForResult(chooserIntent, RESULT_LOAD_IMG);
    }

    // When Image is selected from Gallery or Camera
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            // When an Image is picked
            if (requestCode == RESULT_LOAD_IMG && resultCode == Activity.RESULT_OK) {
                // Get the Image from data

                //Uri selectedImage = data.getData();
                final boolean isCamera;
                if (data == null) {
                    isCamera = true;
                } else {
                    final String action = data.getAction();
                    if (action == null) {
                        isCamera = false;
                    } else {
                        isCamera = action.equals(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    }
                }

                String[] filePathColumn;
                if (isCamera) {
                    imgPath = outputFileUri.getPath();
                    //filePathColumn = new String[]{ MediaStore.Images.Media.DATA };
                } else {
                    outputFileUri = data == null ? null : data.getData();
                    filePathColumn = new String[]{ MediaStore.Images.Media.DATA };
                    // Get the cursor
                    Cursor cursor = this.getContentResolver().query(outputFileUri,
                            filePathColumn, null, null, null);
                    // Move to first row
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    imgPath = cursor.getString(columnIndex);
                    cursor.close();

                }
                doCrop();

            }
            else if (requestCode == CROP_FROM_CAMERA){
                Bundle extras = data.getExtras();
                /**
                 * After cropping the image, get the bitmap of the cropped image and
                 * display it on imageview.
                 */
                if (extras != null) {
                    //bm = extras.getParcelable("data");

                    String filePath = Environment.getExternalStorageDirectory()
                            + "/temporary_holder.jpg";

                    bm = BitmapFactory.decodeFile(filePath);
                    imgView.setImageBitmap(bm);
                    // Get the Image's file name
                    String fileNameSegments[] = imgPath.split("/");
                    imgfileName = fileNameSegments[fileNameSegments.length - 1];
                }
            }
            else {
                Toast.makeText(this, "你没有选择头像",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG)
                    .show();
        }

    }

    // rotate image to the right orientation
    private static int exifToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) { return 90; }
        else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {  return 180; }
        else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {  return 270; }
        return 0;
    }

    public void triggerRegister() {
        makeHTTPCall();
    }

    public void makeHTTPCall() {
        AsyncHttpClient client = new AsyncHttpClient();
        client.setTimeout(30000);
        // Don't forget to change the IP address to your LAN address. Port no as well.
        client.post(url,  params, new AsyncHttpResponseHandler() {
                    // When the response returned by REST has Http
                    // response code '200'
                    View focusView = null;
                    @Override
                    public void onSuccess(String response) {
                        // Hide Progress Dialog
                        prgDialog.hide();
                        showProgress(false);
                        String parts = null;
                        if (response.contains("&&asb##")) {
                            parts = response.split("&&asb##")[1];
                        }

                        if (parts!=null && isInteger(parts)) {
                            Toast.makeText(getApplicationContext(), "成功",
                                    Toast.LENGTH_LONG).show();

                            SharedPreferences settings = getSharedPreferences("MyPrefsFile", 0);
                            SharedPreferences.Editor editor = settings.edit();
                            editor.putString("KEY_LOGIN_USER", getRegisteredUsername());
                            editor.putString("KEY_LOGIN_USER_ID", parts);
                            editor.commit();

                            Intent myIntent;
                            myIntent = new Intent(RegistrationActivity.this, TabHostActivity.class);
                            startActivity(myIntent);
                        } else {
                            if (response.contains("This email is already")){
                                mEmailView.setError("这个邮箱已被注册");
                                focusView = mEmailView;
                            }
                            else if (response.contains("This UserName is already")){
                                mUserNameView.setError("这个用户名已被注册");
                                focusView = mUserNameView;
                            }
                            else {
                                mEmailView.setError(response);
                                focusView = mEmailView;
                            }

                            focusView.requestFocus();
                        }

                    }

                    // When the response returned by REST has Http
                    // response code other than '200' such as '404',
                    // '500' or '403' etc
                    @Override
                    public void onFailure(int statusCode, Throwable error,
                                          String content) {
                        // Hide Progress Dialog
                        prgDialog.hide();
                        showProgress(false);
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

    public static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch(NumberFormatException e) {
            return false;
        } catch(NullPointerException e) {
            return false;
        }
        // only got here if we didn't return false
        return true;
    }

    public String getRegisteredUsername(){
        if (mUserNameView != null){
            return mUserNameView.getText().toString();
        }
        return "";
    }


    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        // Dismiss the progress bar when application is closed
        if (prgDialog != null) {
            prgDialog.dismiss();
        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptRegister() {
        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);
        mUserNameView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String username = mUserNameView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;


        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }else{
            params.put("password", password);
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
        }else{
            params.put("email", email);
        }

        // Check if username valid
        if (TextUtils.isEmpty(username)) {
            mUserNameView.setError(getString(R.string.error_field_required));
            focusView = mUserNameView;
            cancel = true;
        }else{
            params.put("username", username);
        }


        params.put("imgSelectedStatus", "0");

        if (cancel) {
                // There was an error; don't attempt login and focus the first
                // form field with an error.
                focusView.requestFocus();
            } else {
                params.put("filename", imgfileName);
                // Show a progress spinner, and kick off a background task to
                // perform the user login attempt.
                showProgress(true);

                if (imgfileName != null && !imgfileName.isEmpty()) {

                    bm = ThumbnailUtils.extractThumbnail(bm, bm.getWidth()/2, bm.getHeight()/2);


                    params.put("imgSelectedStatus", "1");
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    // Must compress the Image to reduce image size to make upload easy
                    bm.compress(Bitmap.CompressFormat.PNG, 50, stream);
                    byte[] byte_arr = stream.toByteArray();
                    // Encode Image to String
                    encodedString = Base64.encodeToString(byte_arr, 0);
                    params.put("imageString", encodedString);
                }

                triggerRegister();
        }
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

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }


    private void doCrop() {
        final ArrayList<CropOption> cropOptions = new ArrayList<CropOption>();
        /**
         * Open image crop app by starting an intent
         * ‘com.android.camera.action.CROP‘.
         */
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setType("image/*");

        /**
         * Check if there is image cropper app installed.
         */
        List<ResolveInfo> list = getPackageManager().queryIntentActivities(
                intent, 0);

        int size = list.size();

        /**
         * If there is no image cropper app, display warning message
         */
        if (size == 0) {

            Toast.makeText(this, "Can not find image crop app",
                    Toast.LENGTH_SHORT).show();

            return;
        } else {
            /**
             * Specify the image path, crop dimension and scale
             */
            //intent.setData(outputFileUri);

            intent.setDataAndType(outputFileUri, "image/*");

            intent.putExtra("crop", "true");

            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);

            intent.putExtra("outputX", 400);
            intent.putExtra("outputY", 400);

            //intent.putExtra("return-data", true);
            File f = new File(Environment.getExternalStorageDirectory(),
                    "/temporary_holder.jpg");
            try {
                f.createNewFile();
            } catch (IOException ex) {
                Log.e("io", ex.getMessage());
            }

            Uri uri = Uri.fromFile(f);

            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            /**
             * There is posibility when more than one image cropper app exist,
             * so we have to check for it first. If there is only one app, open
             * then app.
             */

            if (size == 1) {
                Intent i = new Intent(intent);
                ResolveInfo res = list.get(0);

                i.setComponent(new ComponentName(res.activityInfo.packageName,
                        res.activityInfo.name));

                startActivityForResult(i, CROP_FROM_CAMERA);
            } else {
                /**
                 * If there are several app exist, create a custom chooser to
                 * let user selects the app.
                 */
                for (ResolveInfo res : list) {
                    final CropOption co = new CropOption();

                    co.title = getPackageManager().getApplicationLabel(
                            res.activityInfo.applicationInfo);
                    co.icon = getPackageManager().getApplicationIcon(
                            res.activityInfo.applicationInfo);
                    co.appIntent = new Intent(intent);

                    co.appIntent
                            .setComponent(new ComponentName(
                                    res.activityInfo.packageName,
                                    res.activityInfo.name));

                    cropOptions.add(co);
                }

                CropOptionAdapter adapter = new CropOptionAdapter(
                        getApplicationContext(), cropOptions);

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Choose Crop App");
                builder.setAdapter(adapter,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int item) {
                                startActivityForResult(
                                        cropOptions.get(item).appIntent,
                                        CROP_FROM_CAMERA);
                            }
                        });

                builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {

                        if (outputFileUri != null) {
                            //getContentResolver().delete(outputFileUri, null,
                            //        null);
                            outputFileUri = null;
                        }
                    }
                });

                AlertDialog alert = builder.create();

                alert.show();
            }
        }
    }
}
