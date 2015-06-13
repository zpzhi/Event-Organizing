package com.example.pengzhizhou.meetup;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class UserProfileEditActivity extends ActionBarActivity {
    private ImageViewRounded ir;
    private ImageView imgV;
    private EditText realN, phone;
    private RequestParams params = new RequestParams();
    private Bitmap bm = null;
    private Uri outputFileUri;
    private static int RESULT_LOAD_IMG = 1;
    private String imgPath, imgfileName;
    private String encodedString;
    private ProgressDialog prgDialog;
    private String url = Utility.getServerUrl() + "/signin/update-user-from-android.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile_edit);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(R.layout.activity_user_profile_edit_actionbar);

        ir = new ImageViewRounded(this);
        prgDialog = new ProgressDialog(this);
        // Set Cancelable as False
        prgDialog.setCancelable(false);

        User user = (User) getIntent().getSerializableExtra("userInformation");
        if (user != null){
            String imageName = user.getImageName();
            String realName = user.getRealName();
            String userName = user.getName();
            String id = user.getId();
            String phoneNumber = user.getPhoneNumber();
            params.put("user", userName);

            imgV = (ImageView) findViewById(R.id.userImg);
            realN = (EditText) findViewById(R.id.realName);
            phone = (EditText) findViewById(R.id.phoneNumber);

            String imageUrl;
            if (!imageName.isEmpty() && imageName != null) {
                imageUrl = Utility.getServerUrl() + "/signin/imgupload/" + imageName;

                Bitmap bt = Utility.getBitmapFromURL(imageUrl);
                if(bt!=null) {
                    bt = ir.getCircledBitmap(bt);

                }
                imgV.setImageBitmap(bt);
            }
            else{

                Bitmap icon = BitmapFactory.decodeResource(this.getResources(),
                        R.drawable.default_activity);
                icon = ir.getCircledBitmap(icon);

                imgV.setImageBitmap(icon);
            }

            if (realName !=null && !realName.equals("null")){
                realN.setText(realName);
            }
            if (phoneNumber != null && !phoneNumber.equals("null")){
                phone.setText(phoneNumber);
            }
        }

        //TextView changePicture = (TextView) findViewById(R.id.changePic);
        TextView save = (TextView) findViewById(R.id.save);
        TextView cancel = (TextView) findViewById(R.id.cancel);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent;
                myIntent = new Intent(UserProfileEditActivity.this, TabHostActivity.class);
                myIntent.putExtra("tab", 2);
                startActivity(myIntent);
            }
        });

        save.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                attemptUpdate();
            }
        });

    }

    public void changePic(View v) {
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
                Bitmap bt = bm;
                bt = ir.getCircledBitmap(bt);
                imgV.setImageBitmap(bt);
                //if (bt!=null)bt.recycle();
                // Get the Image's file name
                String fileNameSegments[] = imgPath.split("/");
                imgfileName = fileNameSegments[fileNameSegments.length - 1];

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

    public void attemptUpdate(){
        params.put("realName", realN.getText().toString());
        params.put("phoneNumber", phone.getText().toString());
        params.put("filename", imgfileName);
        // Show a progress spinner, and kick off a background task to
        // perform the user login attempt.
        prgDialog.setMessage("Converting Image to Binary Data");
        prgDialog.show();

        if (imgPath !=null && !imgPath.isEmpty()) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            // Must compress the Image to reduce image size to make upload easy
            bm.compress(Bitmap.CompressFormat.PNG, 50, stream);
            byte[] byte_arr = stream.toByteArray();
            // Encode Image to String
            encodedString = Base64.encodeToString(byte_arr, 0);
            params.put("imageString", encodedString);
        }

        triggerUpdate();
    }

    public void triggerUpdate() {
        makeHTTPCall();
    }

    // Make Http call to upload Image to Php server
    public void makeHTTPCall() {
        prgDialog.setMessage("Invoking Php");
        AsyncHttpClient client = new AsyncHttpClient();
        // Don't forget to change the IP address to your LAN address. Port no as well.
        client.post(url, params, new AsyncHttpResponseHandler() {
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
                    myIntent = new Intent(UserProfileEditActivity.this, TabHostActivity.class);
                    myIntent.putExtra("tab", 2);
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
                    // extends the error time
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

}
