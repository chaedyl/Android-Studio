package com.example.mycamera;

import android.hardware.Camera;
import android.os.Environment;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;

public class MainActivity extends AppCompatActivity {
    Camera mCamera;
    CameraPreview mPreview;
    Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            File file = getOutputMediaFile(MEDIA_TYPE_IMAGE);

            try {
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(data);
                fos.close();
            }catch (IOException e) {
                Log.d("MainActivity", e.getMessage());
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    @Override
    protected void onResume() {
        super.onResume();

        ConstraintLayout rootView = findViewById(R.id.rootView);
        if(safeCameraOpen(0)) {
            mPreview = new CameraPreview(this, mCamera);
            ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(600, 600);
            rootView.addView(mPreview, params);
        }
    }

    private boolean safeCameraOpen(int id) {
        boolean qOpen = false;
        releaseCameraAndPreview();
        mCamera = Camera.open(id);
        qOpen = (mCamera != null);

        return qOpen;

    }

    private void releaseCameraAndPreview() {
        if(mPreview != null) {
            mPreview.setCamera(null);
        }
        if(mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    public void btnCamera(View view) {
        mCamera.takePicture(null, null, mPictureCallback);
    }


    private File getOutputMediaFile(int type){

        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(),
                "MyCamera");
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("CameraApp", "failed to create directory");
                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else {
            return null;
        }
        return mediaFile;
    }

}
