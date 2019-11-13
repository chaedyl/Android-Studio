package com.example.mycamera;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceView;
import android.view.SurfaceHolder;

import java.io.IOException;

public class CameraPreview extends SurfaceView
        implements SurfaceHolder.Callback {
    Camera mCamera;
    SurfaceHolder mHolder;

    public CameraPreview(Context context, Camera camera) {
        super(context);

        mHolder = getHolder();
        mHolder.addCallback(this);

        mCamera = camera;
    }

    public void setCamera(Camera camera) {
        mCamera = camera;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        }catch (IOException e) {
            Log.d("CameraPreview", e.getMessage());
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mCamera.stopPreview();
        Camera.Parameters parameters = mCamera.getParameters();
        Camera.Size previewSize = parameters.getSupportedPreviewSizes().get(0);
        parameters.setPreviewSize(previewSize.width, previewSize.height);
        mCamera.setParameters(parameters);
        mCamera.startPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if(mCamera != null) {
            mCamera.stopPreview();
        }
    }
}
