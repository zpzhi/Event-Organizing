package com.example.pengzhizhou.meetup;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Matrix;

import com.github.jjobes.slidedatetimepicker.SlideDateTimeListener;
import com.github.jjobes.slidedatetimepicker.SlideDateTimePicker;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class PostActivityDetailActivity extends ActionBarActivity{

    private SimpleDateFormat mFormatter = new SimpleDateFormat("MMMM dd yyyy hh:mm aa");
    private TextView mActDate,mActTime;
    private EditText mActTitle, mActDuration;
    private EditText mActAddress;
    private EditText mActPhoneNum;
    private EditText mActDescription;
    private View mProgressView;
    String encodedString;
    Bitmap bitmap;
    String imgPath, fileName;
    ProgressDialog prgDialog;
    private Uri outputFileUri;
    ImageView imgView;
    Bitmap bm = null;
    public String url = Utility.getServerUrl() + "/signin/post-activity-from-android.php";
    RequestParams paramsA = new RequestParams();

    private Thread worker;

    private static int RESULT_LOAD_IMG = 1;

    private SlideDateTimeListener listener = new SlideDateTimeListener() {
        @Override
        public void onDateTimeSet(Date date)
        {
            Toast.makeText(PostActivityDetailActivity.this,
                    mFormatter.format(date), Toast.LENGTH_SHORT).show();
            //params.remove("activityTime");
            DateFormat formatter = new SimpleDateFormat("MMdd");
            DateFormat formatter1 = new SimpleDateFormat("hh:mm");

            mActDate.setText(formatter.format(date));
            mActTime.setText(formatter1.format(date));

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            String timetodb = sdf.format(date);

            paramsA.put("activityTime", timetodb);

            Log.i("activityTime", timetodb);
        }

        // Optional cancel listener
        @Override
        public void onDateTimeCancel()
        {
            Toast.makeText(PostActivityDetailActivity.this,
                    "Canceled", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_activity_detail);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(R.layout.activity_post_activity_detail_actionbar);

        SharedPreferences settings = getSharedPreferences("MyPrefsFile", 0);
        String loginUser = settings.getString("KEY_LOGIN_USER", null);
        paramsA.put("loginUser", loginUser);

        prgDialog = new ProgressDialog(this);
        // Set Cancelable as False
        prgDialog.setCancelable(false);

        //mProgressView = findViewById(R.id.login_progress);

        mActTitle = (EditText) findViewById(R.id.postTitle);

        Calendar c = Calendar.getInstance();
        int minutes = c.get(Calendar.MINUTE);
        int hours = c.get(Calendar.HOUR_OF_DAY);

        mActDate = (TextView) findViewById(R.id.activityDate);
        mActTime = (TextView) findViewById(R.id.currentTime);
        mActTime.setText(hours+":"+minutes);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        String timetodb = sdf.format(c.getTime());
        paramsA.put("activityTime", timetodb);

        mActTime.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v)
            {
                new SlideDateTimePicker.Builder(getSupportFragmentManager())
                        .setListener(listener)
                        .setInitialDate(new Date())
                                .setMinDate(Calendar.getInstance().getTime())
                                //.setMaxDate(maxDate)
                                //.setIs24HourTime(true)
                                .setTheme(SlideDateTimePicker.HOLO_LIGHT)
                                .setIndicatorColor(Color.parseColor("#990000"))
                        .build()
                        .show();
            }
        });

        mActDuration = (EditText) findViewById(R.id.actDuration);
        mActAddress = (EditText) findViewById(R.id.locationText);
        mActPhoneNum = (EditText) findViewById(R.id.contactPhoneNumber);
        mActDescription = (EditText) findViewById(R.id.activityDescription);

        imgView = (ImageView) findViewById(R.id.imgView);
        TextView uploadImg = (TextView) findViewById(R.id.insertImage);
        uploadImg.setOnClickListener(new View.OnClickListener()
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


        TextView postActivity = (TextView) findViewById(R.id.postActivity);
        postActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptPostActivity();
            }
        });

    }

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

    // When Image is selected from Gallery
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

                Uri selectedImage;
                String[] filePathColumn;
                if (isCamera) {
                    imgPath = outputFileUri.getPath();
                    //filePathColumn = new String[]{ MediaStore.Images.Media.DATA };
                } else {
                    selectedImage = data == null ? null : data.getData();
                    filePathColumn = new String[]{ MediaStore.Images.Media.DATA };
                    // Get the cursor
                    Cursor cursor = this.getContentResolver().query(selectedImage,
                            filePathColumn, null, null, null);
                    // Move to first row
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    imgPath = cursor.getString(columnIndex);
                    cursor.close();

                }

                // Set the Image in ImageView
                final BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 6;

                bm = BitmapFactory.decodeFile(imgPath,options);
                ExifInterface exif = new ExifInterface(imgPath);
                int rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                int rotationInDegrees = exifToDegrees(rotation);
                Matrix matrix = new Matrix();
                if (rotation != 0f) {
                    matrix.preRotate(rotationInDegrees);
                    bm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
                }
                imgView.setImageBitmap(bm);
                // Get the Image's file name
                String fileNameSegments[] = imgPath.split("/");
                fileName = fileNameSegments[fileNameSegments.length - 1];
                // Put file name in Async Http Post Param which will used in Php web app

                paramsA.put("filename", fileName);

            } else {
                Toast.makeText(this, "You haven't picked Image",
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

    /*
    private TextView mActDate;
    private EditText mActTitle,mActTime, mActDuration;
    private EditText mActAddress;
    private EditText mActPhoneNum;
    private EditText mActDescription;
     */
    public void attemptPostActivity() {
        // Reset errors.
        mActTitle.setError(null);
        mActDuration.setError(null);
        mActAddress.setError(null);
        mActPhoneNum.setError(null);
        mActDescription.setError(null);

        // Store values at the time of the login attempt.
        String title = mActTitle.getText().toString();
        String duration = mActDuration.getText().toString();
        String address = mActAddress.getText().toString();
        String phoneNumber =  mActPhoneNum.getText().toString();
        String description =  mActDescription.getText().toString();


        boolean cancel = false;
        View focusView = null;

        // Check for a valid title, if the user entered one.
        if (TextUtils.isEmpty(title)) {
            mActTitle.setError(getString(R.string.error_field_required));
            focusView = mActTitle;
            cancel = true;
        }else{
            paramsA.put("title", title);
        }

        // Check for a valid duration.
        if (TextUtils.isEmpty(duration)) {
            mActDuration.setError(getString(R.string.error_field_required));
            focusView = mActDuration;
            cancel = true;
        } else if (Integer.parseInt(duration) <= 0) {
            mActDuration.setError(getString(R.string.error_invalid_duration));
            focusView = mActDuration;
            cancel = true;
        }else{
            paramsA.put("duration", duration);
        }

        // Check if address valid
        if (TextUtils.isEmpty(address)) {
            mActAddress.setError(getString(R.string.error_field_required));
            focusView = mActAddress;
            cancel = true;
        }else{
            paramsA.put("address", address);
        }

        // Check if phoneNumber valid
        if (TextUtils.isEmpty(phoneNumber)) {
            mActPhoneNum.setError(getString(R.string.error_field_required));
            focusView = mActPhoneNum;
            cancel = true;
        }else{
            paramsA.put("phoneNumber", phoneNumber);
        }

        // Check if description valid
        if (TextUtils.isEmpty(description)) {
            mActDescription.setError(getString(R.string.error_field_required));
            focusView = mActDescription;
            cancel = true;
        }else{
            paramsA.put("description", description);
        }

        if (imgPath !=null && !imgPath.isEmpty()) {

                            //BitmapFactory.Options options;
                            //options = new BitmapFactory.Options();
                            //options.inSampleSize = 3;
                            //bitmap = BitmapFactory.decodeFile(imgPath,
                             //       options);
            //final BitmapFactory.Options options = new BitmapFactory.Options();
            //options.inSampleSize = 8;

            //bm = BitmapFactory.decodeFile(imgPath,options);

                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            // Must compress the Image to reduce image size to make upload easy
                            bm.compress(Bitmap.CompressFormat.PNG, 50, stream);
                            byte[] byte_arr = stream.toByteArray();
                            // Encode Image to String
                            encodedString = Base64.encodeToString(byte_arr, 0);
                            paramsA.put("imageString", encodedString);

        }

        if (cancel) {
                // There was an error; don't attempt login and focus the first
                // form field with an error.
                focusView.requestFocus();
        } else {
                // Show a progress spinner, and kick off a background task to
                // perform the user login attempt.
                prgDialog.setMessage("Converting Image to Binary Data");
                prgDialog.show();

                postActivity();

            }

    }


    public void postActivity() {
        makeHTTPCall();
    }

    // Make Http call to upload Image to Php server
    public void makeHTTPCall() {
        prgDialog.setMessage("Invoking Php");
        AsyncHttpClient client = new AsyncHttpClient();
        // Don't forget to change the IP address to your LAN address. Port no as well.
        client.post(url, paramsA, new AsyncHttpResponseHandler() {
                    // When the response returned by REST has Http
                    // response code '200'
                    @Override
                    public void onSuccess(String response) {
                        // Hide Progress Dialog
                        prgDialog.hide();
                        //showProgress(false);

                        if (response.equals("success")) {
                            Toast.makeText(getApplicationContext(), response,
                                    Toast.LENGTH_LONG).show();

                            Intent myIntent;
                            myIntent = new Intent(PostActivityDetailActivity.this, TabHostActivity.class);
                            //myIntent.putExtra("user", post.this.getRegisteredUsername());
                            startActivity(myIntent);
                        } else {
                            Toast.makeText(getApplicationContext(), response,
                                    Toast.LENGTH_LONG).show();
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
                            /*Toast.makeText(
                                    getApplicationContext(),
                                    "Error Occured \n Most Common Error: \n1. Device not connected to Internet\n2. Web App is not deployed in App server\n3. App server is not running\n HTTP Status code : "
                                            + statusCode, Toast.LENGTH_LONG)
                                    .show();*/
                            for (int i=0; i < 10; i++)
                            {
                                Toast.makeText(getApplicationContext(),
                                        "what happen? - "+content + statusCode,
                                        Toast.LENGTH_LONG).show();
                            }


                        }
                    }
                });
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_post_activity_detail, menu);
        return true;
    }

}
