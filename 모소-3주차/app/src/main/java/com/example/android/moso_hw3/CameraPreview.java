package com.example.android.moso_hw3;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

//미리보기 화면을 만들기 위해서 preview 클래스 만들어줌

public class CameraPreview extends SurfaceView
    implements SurfaceHolder.Callback {
    Camera mCamera;
    SurfaceHolder mHolder;
    // surfaceholder 클래스가 백그라운드에서 처리된 결과를 중간에서 주고 받아 관리함
    // surfaceview 에 영상을 표현하기 위해서 surfaceholder 가 필요함

    public CameraPreview(Context context, Camera camera) {
        super(context);

        mHolder = getHolder();
        mHolder.addCallback(this);

        mCamera = camera;
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void setCamera(Camera camera) {
        mCamera = camera;
    }

    // surfaceview 가 생성될 때 발생, 카메라와 surfaceholder를 연결하고 카메라 preview를 시작함

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.setDisplayOrientation(90);
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
        requestLayout();
        mCamera.setParameters(parameters);
        mCamera.startPreview();
    }

    //카메라 리소스 반환
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if(mCamera != null) {
            mCamera.stopPreview();
        }
    }
}