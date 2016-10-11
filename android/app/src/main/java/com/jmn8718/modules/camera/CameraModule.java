package com.jmn8718.modules.camera;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.Base64;
import android.util.Log;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by motoko on 11/10/16.
 */
public class CameraModule extends ReactContextBaseJavaModule implements ActivityEventListener {

    public static final String REACT_NAME = "CameraModule";
    public static final String TAG = "CameraModule";
    private static final int REQUEST_IMAGE_CAPTURE = 1;

    private Callback mCallback;

    public CameraModule(ReactApplicationContext reactContext) {
        super(reactContext);

        // Add the listener for `onActivityResult`
        reactContext.addActivityEventListener(this);
    }

    @Override
    public String getName() {
        return REACT_NAME;
    }

    @ReactMethod
    public void takePicture(Callback callback) {
        Activity currentActivity = getCurrentActivity();

        if (currentActivity == null) {
            Log.w(TAG, "Activity doesn't exist");
            return;
        }

        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            // Error occurred while creating the File
            callback.invoke(ex);
        }

        try {
            mCallback = callback;
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                Uri photoURI = FileProvider.getUriForFile(getReactApplicationContext(),
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                if (takePictureIntent.resolveActivity(currentActivity.getPackageManager()) != null) {
                    currentActivity.startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            }

        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            mCallback.invoke(e);
        }

    }

    private String mCurrentPhotoPath;

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getCurrentActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }

    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult");
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            WritableMap map = Arguments.createMap();
            map.putString("data", imageBitmap.toString());
            map.putString("pic", mCurrentPhotoPath);
            mCallback.invoke(map);
        }
    }

    @Override
    public void onNewIntent(Intent intent) {

    }
}
