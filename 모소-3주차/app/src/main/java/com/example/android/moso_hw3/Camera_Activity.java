package com.example.android.moso_hw3;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Environment;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.xml.parsers.FactoryConfigurationError;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;

public class Camera_Activity extends AppCompatActivity {
    Camera mCamera;
    String name;
    CameraPreview mPreview;
    ArrayAdapter<String> adapter;
    ArrayList<String> list;

    File mediaStorageDir = new File(Environment.getExternalStorageDirectory(),
            "MyCamera");;
    Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            mCamera.stopPreview();
            File file = getOutputMediaFile(MEDIA_TYPE_IMAGE);

            //mCamera.stopPreview();

            try {
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(data);
                fos.close();

                list.add(name);
                adapter.notifyDataSetChanged();

            }catch (IOException e) {
                Log.d("MainActivity", e.getMessage());
            }

            mCamera.startPreview();
            //버튼을 두번재 누른 후에 다시 제대로 동작하지 않는 문제 때문에 넣은 코드
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_);

        // 2. 레이아웃 파일에 정의된 ListView를 자바 코드에서 사용할 수 있도록 합니다.
        // findViewById 메소드는 레이아웃 파일의 android:id 속성을 이용하여 뷰 객체를 찾아 리턴합니다.
        ListView listview = (ListView)findViewById(R.id.listview);


        // 3. 실제로 문자열 데이터를 저장하는데 사용할 ArrayList 객체를 생성합니다.
        list = new ArrayList<>();

        File myfile = new File(mediaStorageDir.getPath()+File.separator);

        String[] array = myfile.list();

        // 4. ArrayList 객체에 데이터를 집어넣습니다.
        for (int i = 0; i < array.length; i++) {
            list.add(array[i]);
        }

        // 5. ArrayList 객체와 ListView 객체를 연결하기 위해 ArrayAdapter객체를 사용합니다.
        // 우선 ArrayList 객체를 ArrayAdapter 객체에 연결합니다.
        adapter = new ArrayAdapter<String>(
                this, //context(액티비티 인스턴스)
                android.R.layout.simple_list_item_1, // 한 줄에 하나의 텍스트 아이템만 보여주는 레이아웃 파일
                // 한 줄에 보여지는 아이템 갯수나 구성을 변경하려면 여기에 새로만든 레이아웃을 지정하면 됩니다.
                list  // 데이터가 저장되어 있는 ArrayList 객체
        );

        // 6. ListView 객체에 adapter 객체를 연결합니다.
        listview.setAdapter(adapter);

        // 7. ListView 객체의 특정 아이템 클릭시 처리를 추가합니다.
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView,
                                    View view, int position, long id) {

                // 8. 클릭한 아이템의 문자열을 가져와서
                String selected_item = (String)adapterView.getItemAtPosition(position);
                //File myfile2 = new File(mediaStorageDir.getPath());


                Bitmap bitmap = BitmapFactory.decodeFile(mediaStorageDir.getAbsolutePath()+File.separator+selected_item);

                ImageView imageview = findViewById(R.id.imageView);
                imageview.setImageBitmap(bitmap);
            }
        });

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

        name = "IMG_"+ timeStamp + ".jpg";
        return mediaFile;
    }

}
