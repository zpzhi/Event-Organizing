package com.example.pengzhizhou.meetup;

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
import android.graphics.Color;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.jjobes.slidedatetimepicker.SlideDateTimeListener;
import com.github.jjobes.slidedatetimepicker.SlideDateTimePicker;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class PostActivityDetailActivity extends ActionBarActivity{

    private SimpleDateFormat mFormatter = new SimpleDateFormat("MMMM dd yyyy hh:mm aa");
    private String loginUserId, loginUser;
    private TextView mActDate,mActTime;
    private AutoCompleteTextView mActTitle;
    private AutoCompleteTextView mActAddress;
    private AutoCompleteTextView mActPhoneNum;
    private EditText mActDescription;
    private String mDuration = null;
    private String mSProvince = null;
    private String mSCity = null;
    private Spinner spinner1;
    private Spinner spinner2;
    private ArrayList<String> cityArray;
    private ArrayAdapter<String> city_adapter;
    private String encodedString;
    private String imgPath, fileName;
    private ProgressDialog prgDialog;
    private Uri outputFileUri;
    private ImageView imgView;
    private Bitmap bm = null;
    private String statePre = null;
    private String cityPre = null;
    private String addressPre = null;
    private String phonePre = null;
    public String url = Utility.getServerUrl();
    private RequestParams paramsA = new RequestParams();
    private String[] provinceArray;
    private ArrayAdapter<String> adapter1;

    private static final int RESULT_LOAD_IMG = 1;
    private static final int CROP_FROM_CAMERA = 2;

    private SlideDateTimeListener listener = new SlideDateTimeListener() {
        @Override
        public void onDateTimeSet(Date date, String duration)
        {
            Toast.makeText(PostActivityDetailActivity.this,
                    mFormatter.format(date), Toast.LENGTH_SHORT).show();
            //params.remove("activityTime");
            DateFormat formatter = new SimpleDateFormat("MM-dd");
            DateFormat formatter1 = new SimpleDateFormat("HH:mm");

            mActDate.setText(formatter.format(date));
            mActTime.setText(formatter1.format(date));

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            String timetodb = sdf.format(date);

            paramsA.put("activityTime", timetodb);

            Log.i("activityTime", timetodb);
            mDuration = duration;
            paramsA.put("duration", duration);
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

        String activityTitle;
        Bundle extras = getIntent().getExtras();
        String activityTypeId;
        if (extras != null) {
            activityTitle = extras.getString("activityTypes");
            activityTypeId = extras.getString("activityTypesId");
        }else{
            activityTitle = "活动内容";
            activityTypeId = "8";
        }
        paramsA.put("activityType", activityTypeId);

        TextView postTitle = (TextView) findViewById(R.id.postActivityTitle);
        postTitle.setText(activityTitle);

        SharedPreferences settings = getSharedPreferences("MyPrefsFile", 0);
        loginUser = settings.getString("KEY_LOGIN_USER", null);
        loginUserId = settings.getString("KEY_LOGIN_USER_ID", null);
        paramsA.put("loginUser", loginUser);
        paramsA.put("loginUserId", loginUserId);

        prgDialog = new ProgressDialog(this);
        // Set Cancelable as False
        prgDialog.setCancelable(false);

        mActTitle = (AutoCompleteTextView) findViewById(R.id.postTitle);

        Calendar c = Calendar.getInstance();
        int minutes = c.get(Calendar.MINUTE);
        int hours = c.get(Calendar.HOUR_OF_DAY);
        String s_minutes = Integer.toString(minutes);
        if (minutes < 10){
            s_minutes = "0"+s_minutes;
        }

        mActDate = (TextView) findViewById(R.id.activityDate);
        mActTime = (TextView) findViewById(R.id.currentTime);
        mActTime.setText(hours+":"+s_minutes);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        String timetodb = sdf.format(c.getTime());
        paramsA.put("activityTime", timetodb);
        paramsA.put("postTime", timetodb);

        mActDate.setOnClickListener(new View.OnClickListener() {

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

        mActAddress = (AutoCompleteTextView) findViewById(R.id.locationText);
        mActPhoneNum = (AutoCompleteTextView) findViewById(R.id.contactPhoneNumber);
        mActDescription = (EditText) findViewById(R.id.activityDescription);
        mActDescription.setFilters(new InputFilter[] { new InputFilter.LengthFilter(200) });

        imgView = (ImageView) findViewById(R.id.imgView);

        spinner1 = (Spinner) findViewById(R.id.spinner1);
        provinceArray = getResources().getStringArray(R.array.province_array);
        adapter1 = new ArrayAdapter<String>(
                this,
                R.layout.spinner_item,
                provinceArray);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner1.setAdapter(adapter1);
        spinner1.setOnItemSelectedListener (new OnItemSelectedListener()  {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int sid=spinner1.getSelectedItemPosition();
                mSProvince = provinceArray[sid];
                cityArray = new ArrayList<String>();
                city_adapter = new ArrayAdapter<String>( PostActivityDetailActivity.this,
                        R.layout.spinner_item,
                        cityArray);
                city_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner2.setAdapter(city_adapter);

                new FetchCityList(mSProvince).execute();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinner2 = (Spinner) findViewById(R.id.spinner2);
        spinner2.setOnItemSelectedListener (new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int sid=spinner2.getSelectedItemPosition();
                mSCity= cityArray.get(sid);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        TextView uploadImg = (TextView) findViewById(R.id.insertImage);
        uploadImg.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                openImageIntent();
            }
        });

        ImageView uploadImgButton = (ImageView) findViewById(R.id.insertImageButton);
        uploadImgButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                openImageIntent();
            }
        });

        ImageView postActivity = (ImageView) findViewById(R.id.postActivity);
        postActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptPostActivity();
            }
        });

        // do not use setDisplayHomeAsUpEnabled here, because the title in action bar can not be centered.
        ImageView backAction = (ImageView) findViewById(R.id.backButton);
        backAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent;
                myIntent = new Intent(PostActivityDetailActivity.this, TabHostActivity.class);
                myIntent.putExtra("tab", 1);
                startActivity(myIntent);
            }
        });

        getUserPostPreference();

    }

    private void getUserPostPreference(){
        new GetUserPostPreference().execute();
    }

    public class GetUserPostPreference extends AsyncTask<String,Void,String>{
        @Override
        protected String doInBackground(String... params) {

            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(url + "get-user-post-preference.php");

            try {
                // Add your data
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("userId", loginUserId));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                // Execute HTTP Post Request
                HttpResponse response = httpclient.execute(httppost);
                BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
                String json = reader.readLine();

                return json;

            } catch (ClientProtocolException e) {
                // TODO Auto-generated catch block
                return e.toString();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                return e.toString();
            }
        }

        @Override
        protected void onPostExecute(final String response) {

            if (response == null){
                return;
            }else if(response.equals("[]")){
                return;
            }else {
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    JSONObject jsonMainNode = jsonResponse.getJSONObject("activity_info");

                    phonePre = jsonMainNode.optString("phone_number");
                    addressPre = jsonMainNode.optString("activity_address");
                    cityPre = jsonMainNode.optString("city");
                    statePre = jsonMainNode.optString("state");

                    if (addressPre != null){
                        mActAddress.setText(addressPre);
                    }
                    if (phonePre != null){
                        mActPhoneNum.setText(phonePre);
                    }
                    if (statePre != null && !statePre.equals("NULL") && adapter1 != null){
                        int spinnerPosition = adapter1.getPosition(statePre);
                        spinner1.setSelection(spinnerPosition);
                    }

                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(), "Error" + e.toString(),
                            Toast.LENGTH_SHORT).show();
                }

            }

        }

    }

    // duplicate codes with register when upload image
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

                //Uri selectedImage;
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
                    fileName = fileNameSegments[fileNameSegments.length - 1];
                    // Put file name in Async Http Post Param which will used in Php web app
                    paramsA.put("filename", fileName);

                }
            }
            else {
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

    public void attemptPostActivity() {
        // Reset errors.
        mActTitle.setError(null);
        mActAddress.setError(null);
        mActPhoneNum.setError(null);
        mActDescription.setError(null);

        String title = mActTitle.getText().toString();
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

        if (mDuration == null) {
            paramsA.put("duration", "2");
        }

        if (mSProvince != null){
            paramsA.put("province", mSProvince);
        }

        if (mSCity != null){
            paramsA.put("city", mSCity);
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

        if (fileName !=null && !fileName.isEmpty()) {
            //int dimension = Utility.getSquareCropDimensionForBitmap(bm);
            //bm = ThumbnailUtils.extractThumbnail(bm, bm.getWidth()/2, bm.getHeight()/2);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            // Must compress the Image to reduce image size to make upload easy
            bm.compress(Bitmap.CompressFormat.PNG, 100, stream);
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
                // perform the post activity attempt.
                prgDialog.setMessage("图片上传中");
                prgDialog.show();

                postActivity();
            }

    }

    public void postActivity() {
        makeHTTPCall();
    }

    // Make Http call to upload Image to Php server
    public void makeHTTPCall() {
        AsyncHttpClient client = new AsyncHttpClient();
        // Don't forget to change the IP address to your LAN address. Port no as well.
        client.post(url+"post-event.php", paramsA, new AsyncHttpResponseHandler() {
                    // When the response returned by REST has Http
                    // response code '200'
                    @Override
                    public void onSuccess(String response) {
                        // Hide Progress Dialog
                        prgDialog.hide();
                        //showProgress(false);

                        if (response.equals("success")) {
                            Toast.makeText(getApplicationContext(), "成功",
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

            intent.putExtra("outputX", 600);
            intent.putExtra("outputY", 600);

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

    private class FetchCityList extends AsyncTask<Void, Void, String> {
        private String mProvince;
        public FetchCityList(String province){
            mProvince = province;
        }

        @Override
        protected String doInBackground(Void... params) {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(url+"get-cities.php");
            String jsonResult = null;
            //add name value pair for the country code
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
            nameValuePairs.add(new BasicNameValuePair("province",String.valueOf(mProvince)));

            try {
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            try {
                HttpResponse response = httpclient.execute(httppost);
                jsonResult = Utility.inputStreamToString(
                        response.getEntity().getContent()).toString();
            }

            catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return jsonResult;
        }

        @Override
        protected void onPostExecute(String jsonResult) {

            if (jsonResult == null || jsonResult.equals("[]")){

                return;
            }
            try {
                JSONObject jsonResponse = new JSONObject(jsonResult);
                JSONArray innerArray = jsonResponse.optJSONArray("cities_info");
                for (int j = 0; j < innerArray.length(); j++) {
                    JSONObject jsonChildNode = innerArray.getJSONObject(j);
                    String city = jsonChildNode.optString("city");
                    cityArray.add(city);
                }

            } catch (JSONException e) {
                Toast.makeText(getApplicationContext(), "Error" + e.toString(),
                        Toast.LENGTH_SHORT).show();
            }

            city_adapter.notifyDataSetChanged();

            int spinnerPosition = city_adapter.getPosition(cityPre);
            spinner2.setSelection(spinnerPosition);

            super.onPostExecute(jsonResult);

        }
    }
}
