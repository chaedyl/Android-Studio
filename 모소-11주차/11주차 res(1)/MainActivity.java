package kr.ac.inha.lab11_ocv340demo3;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageView;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.DMatch;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.features2d.BRISK;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.Features2d;
import org.opencv.features2d.ORB;
import org.opencv.imgproc.Imgproc;
import org.opencv.xfeatures2d.DAISY;
import org.opencv.xfeatures2d.FREAK;
import org.opencv.xfeatures2d.SIFT;
import org.opencv.xfeatures2d.SURF;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener {
    /* Log 앞에 붙일 태그 */
    String TAG = "Android_SIFT";

    /* OpenCV Library 불러옴 */
    static {
        System.loadLibrary("opencv_java3");
        //System.loadLibrary("nonfree");
    }

    /* 이동 및 확대에 사용할 Matrix형 변수들 */
    Matrix matrix = new Matrix();
    Matrix savedMatrix = new Matrix();

    /* 정지, 드래그, 확대 세 가지 상태로 나눔 */
    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;
    int mode = NONE;

    /* 확대 시 사용할 변수들 */
    PointF start = new PointF();
    PointF mid = new PointF();
    float oldDist = 1f;

    /* 이미지뷰, Bitmap, Feature Detector, Descriptor, Keypoint들, 결과 값 행렬을 저장할 변수들 선언 */
    private ImageView imageView;
    private Bitmap lena, sunflower, plaza_l, plaza_r, rst_image;
    private SURF surf = SURF.create();
    private SIFT sift = SIFT.create();
    private ORB orb = ORB.create();
    private FREAK freak_descriptor = FREAK.create();
    private DAISY daisy_detector = DAISY.create();
    private BRISK brisk_detector = BRISK.create();
    private MatOfKeyPoint keyPoints, keyPoints2;
    private Mat result;

    /* Progress를 진행하고 있음을 보여주기 위한 dialog */
    private ProgressDialog p_dialog;

    /* 소요시간 측정을 위한 변수들 */
    private long nStart = 0, nEnd = 0;

    /* 여러가지 색상을 표현하기 위한 point들의 색상 값 */
    List<Scalar> point_color= new ArrayList<Scalar>(){{
        add(new Scalar(225, 0, 0)); add(new Scalar(0, 100, 0)); add(new Scalar(169, 169, 169)); add(new Scalar(0, 0, 200));
        add(new Scalar(255, 140, 0)); add(new Scalar(85, 107, 47)); add(new Scalar(30, 144, 255)); add(new Scalar(34, 139, 34));
        add(new Scalar(218, 165, 32)); add(new Scalar(173, 255, 47)); add(new Scalar(205, 92, 92)); add(new Scalar(75, 0, 130));
        add(new Scalar(152, 251, 150));
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /* 화면 항상 켜기 & 상태바, 액션바 제거 */
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        View decorView = getWindow().getDecorView();

        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        /* Layout 설정 및 ImageView 설정 */
        setContentView(R.layout.activity_main);
        imageView = this.findViewById(R.id.imageView);

        /* 크기를 조절할 수 있는 ImageView로 만듦 (왼쪽 상단으로 정렬) */
        imageView.setScaleType(ImageView.ScaleType.MATRIX);

        /* 왼쪽 상단에 위치한 ImageView를 가운데로 옮김 */
        imageView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                imageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                Drawable drawable = imageView.getDrawable();
                Rect rectDrawable = drawable.getBounds();

                float leftOffset = (imageView.getMeasuredWidth() - rectDrawable.width()) / 2f;
                float topOffset = (imageView.getMeasuredHeight() - rectDrawable.height()) / 2f;

                matrix = imageView.getImageMatrix();
                matrix.postTranslate(leftOffset, topOffset);
                imageView.setImageMatrix(matrix);
                imageView.invalidate();
            }
        });

        /* Bitmap 형식으로 사용할 영상들을 불러옴 */
        lena = BitmapFactory.decodeResource(getResources(), R.drawable.lena);
        sunflower = BitmapFactory.decodeResource(getResources(), R.drawable.sunflower);
        plaza_l = BitmapFactory.decodeResource(getResources(), R.drawable.plaza_l);
        plaza_r = BitmapFactory.decodeResource(getResources(), R.drawable.plaza_r);

        /* 최초 ImageView에는 feature detecting을 위한 영상을 불러옴 */
        imageView.setImageBitmap(sunflower);

        /* ImageView를 터치 동작에 반응하도록 함 */
        imageView.setOnTouchListener(this);
    }

    /* ProgressDialog를 비동기적으로 실행하기 위해 내부클래스 선언 */
    private class SIFT_tasks extends AsyncTask<Void, Void, Void> {
        /* 실행할 task를 지정해줄 변수 및 task의 이름 */
        int flag;
        String task_name;

        /* 생성자, 변수들 및 ProgressDialog 초기화 */
        public SIFT_tasks(int flag, String task_name) {
            this.flag = flag;
            this.task_name = task_name;

            p_dialog = new ProgressDialog(MainActivity.this);
            p_dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            p_dialog.setCancelable(false);
            p_dialog.setMessage(task_name);
        }

        /* 비동기화가 일어나기 전에 호출되는 함수, ProgressDialog를 출력 */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            p_dialog.show();
        }

        /* 비동기화가 일어나고 있는 과정에서 호출되는 함수 */
        @Override
        protected Void doInBackground(Void... arg0) {
            /* Task 번호에 따라 실행 */
            switch(flag) {
                case 1:
                    feature_detecting();
                    break;
                case 2:
                    feature_matching();
                    break;
                default:
                    break;
            }

            return null;
        }

        /* 비동기화가 끝난 뒤에 호출되는 함수, ImageView를 업데이트한 뒤 ProgressDialog를 종료 */
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            imageView.setImageBitmap(rst_image);
            p_dialog.dismiss();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();

        /* Activity가 비활성화되기 전에 ProgressDialog를 종료 */
        if (p_dialog != null && p_dialog.isShowing()){
            p_dialog.dismiss();
            p_dialog = null;
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        /* Activity가 비활성화되기 전에 ProgressDialog를 종료 */
        if (p_dialog != null && p_dialog.isShowing()){
            p_dialog.dismiss();
            p_dialog = null;
        }
    }

    /* 특징점들을 검출하여 출력해주는 함수 */
    private void feature_detecting() {
        /* 출력 영상 및 keypoint들을 저장할 Mat형 변수 */
        result = new Mat();
        keyPoints = new MatOfKeyPoint();

        /* Bitmap -> Mat */
        Utils.bitmapToMat(sunflower, result);

        /* RGB → Grayscale */
        Imgproc.cvtColor(result, result, Imgproc.COLOR_RGB2GRAY);

        /* Feature detecting */
        Log.i(TAG, "Finding features");
        sift.detect(result, keyPoints);

        /* 찾은 keypoint들을 모두 그림 */
        Log.i(TAG, "Drawing features");
        /* method :
         * Features2d.DRAW_OVER_OUTIMG = 1
         * Features2d.DRAW_RICH_KEYPOINTS = 4
         * Features2d.NOT_DRAW_SINGLE_POINTS = 2
         */
        Features2d.drawKeypoints(result, keyPoints, result, Scalar.all(-1), Features2d.DRAW_RICH_KEYPOINTS);

        /* 결과 영상을 저장, Mat -> Bitmap */
        rst_image = Bitmap.createBitmap(result.cols(), result.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(result, rst_image);
    }

    /* 특징점들을 검출한 뒤 이를 matching하고 그 결과를 출력해주는 함수 */
    private void feature_matching() {
        /* 출력 영상들을 저장할 Mat형 변수들 */
        Mat img1 = new Mat();
        Mat img2 = new Mat();

        /* Bitmap -> Mat */
        Utils.bitmapToMat(plaza_l, img1);
        Utils.bitmapToMat(plaza_r, img2);

        /* Feature descriptor를 저장할 Mat형 변수들 */
        Mat descriptor = new Mat();
        Mat descriptor2 = new Mat();

        /* 각 영상에서의 keypoint들을 저장할 변수들 */
        keyPoints = new MatOfKeyPoint();
        keyPoints2 = new MatOfKeyPoint();

        /* 시작 시간을 기록 */
        nStart = System.currentTimeMillis();

        /* Keypoint들을 찾아서 extractor를 이용하여 descriptor 행렬에 저장 */
        sift.detectAndCompute(img1, new Mat(), keyPoints, descriptor);
        sift.detectAndCompute(img2, new Mat(), keyPoints2, descriptor2); // detector 와 descriptor가 다를 경우 detect / compute 분리

        /* Feature matching을 실행하여 첫 번째 영상에서의 keypoint 순번에
           두 번째 영상의 keypoint 순번을 연결 → mapping */
        /* matcher type :
         * FLANNBASED
         * BruteForce
         * BruteForce-L1
         * BruteForce-Hamming
         */
        Log.i(TAG, "Feature matching starts");
        DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.FLANNBASED);

        List<MatOfDMatch> matches = new ArrayList<MatOfDMatch>(){{}};
        matcher.knnMatch(descriptor, descriptor2, matches, 2);  // knn 의 k = 2 // 2개까지 후보 추출

        Log.i(TAG, "Descriptor's Dimension: " + descriptor.cols());

        float ratio_thresh = .5f; // 1순위의 매칭 결과가 2순위 매칭 결과의 0.5배보다 더 가까운 값만을 취함
        MatOfDMatch good_matches = new MatOfDMatch();
        List<DMatch> good_match = new ArrayList<DMatch>();

        for(int i=0; i<matches.size(); i++){
            DMatch[] knn_matches = matches.get(i).toArray();
            if(knn_matches[0].distance < ratio_thresh * knn_matches[1].distance)
                good_match.add(knn_matches[0]);
        }
        good_matches.fromList(good_match);  // Thresholding 을 통과한 후보만 match point로 저장

        /* 종료 시간을 기록한 뒤 그 시간을 Log로 출력 */
        nEnd = System.currentTimeMillis();
        Log.i(TAG, "Feature-matching computing time : " + (nEnd - nStart) + "ms");

        /* Matching 된 점들 사이에 선분들을 모두 그어줌 */
        Log.i(TAG, "Draw lines");

        concatenate_img(img1, img2); // concatenate img for drawing

        /* 각 keypoint들을 List 형태로 저장 */
        List<KeyPoint> pp1;
        pp1 = keyPoints.toList();
        List<KeyPoint> pp2;
        pp2 = keyPoints2.toList();

        // draw matches as Line
        for(int i=0; i<good_match.size(); i++){
            double img1_x = pp1.get(good_match.get(i).queryIdx).pt.x;
            double img1_y = pp1.get(good_match.get(i).queryIdx).pt.y;
            double img2_x = pp2.get(good_match.get(i).trainIdx).pt.x + img1.size().width;
            double img2_y = pp2.get(good_match.get(i).trainIdx).pt.y;
            Imgproc.line(result, new Point(img1_x, img1_y), new Point(img2_x, img2_y), point_color.get(i % point_color.size()));
        }

        /* 결과 영상을 저장, Mat -> Bitmap */
        rst_image = Bitmap.createBitmap(result.cols(), result.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(result, rst_image);
    }

    /* 두 행렬을 이어주는 함수 */
    private void concatenate_img(Mat img1, Mat img2) {
        /* 받은 Mat형 변수들을 Size형으로 저장한 후 높이, 너비를 저장 */
        Size size1 = img1.size();
        int height1 = (int) size1.height;
        int width1 = (int) size1.width;

        Size size2 = img2.size();
        int height2 = (int) size2.height;
        int width2 = (int) size2.width;

        /* 두 영상을 합치는 것이므로 결과 영상의 높이는 두 영상 중 최대값을,
           너비는 두 영상의 너비의 합으로 설정 */
        int height_c = Math.max(height1, height2);
        int width_c = width1 + width2;

        /* 결과 영상 높이, 너비를 이용하여 생성해준 후 초기화 */
        result = new Mat(height_c, width_c, CvType.CV_8UC4);
        result.setTo(new Scalar(0, 0, 0, 0));

        /* 시작 시간을 기록 */
        nStart = System.currentTimeMillis();

        /* 두 영상을 결과 영상 행렬에 복사 */
        img1.copyTo(result.submat(0, height1, 0, width1));
        img2.copyTo(result.submat(0, height2, width1, width_c));

        /* 종료 시간을 기록한 뒤 그 시간을 Log로 출력 */
        nEnd = System.currentTimeMillis();
        Log.i(TAG, "Concatenate image computing time : " + (nEnd - nStart) + "ms");
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        /* 크기 저장 */
        float scale;

        /* 터치에 관한 이벤트들 처리 */
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            /* 첫 손가락 하나만 터치했을 때 */
            case MotionEvent.ACTION_DOWN:
                /* 현재 행렬을 저장하고 드래그 모드로 전환 */
                savedMatrix.set(matrix);
                start.set(event.getX(), event.getY());
                Log.d(TAG, "mode: DRAG");
                mode = DRAG;
                break;
            /* 첫 손가락을 떼었을 때 */
            case MotionEvent.ACTION_UP:
                break;
            /* 두 번째 손가락을 떼었을 때 */
            case MotionEvent.ACTION_POINTER_UP:
                Log.d(TAG, "mode: NONE");
                mode = NONE;
                break;
            /* 두 번째 손가락도 터치했을 때 */
            case MotionEvent.ACTION_POINTER_DOWN:
                /* 두 손가락이 터치한 지점 사이의 거리 측정 */
                oldDist = spacing(event);

                /* 최소 거리보다 클 경우 두 터치 지점의 중간 지점을 저장한 후
                   확대, 축소 모드로 전환 */
                if (oldDist > 5f) {
                    savedMatrix.set(matrix);
                    midPoint(mid, event);
                    Log.d(TAG, "mode: ZOOM");
                    mode = ZOOM;
                }
                break;
            /* 터치한 손가락이 움직일 때 */
            case MotionEvent.ACTION_MOVE:
                /* 모드에 따라 드래그 또는 확대, 축소 실행 */
                if (mode == DRAG) {
                    matrix.set(savedMatrix);

                    matrix.postTranslate(event.getX() - start.x, event.getY() - start.y);
                }
                else if (mode == ZOOM) {
                    float newDist = spacing(event);

                    if (newDist > 5f) {
                        matrix.set(savedMatrix);

                        scale = newDist / oldDist;

                        matrix.postScale(scale, scale, mid.x, mid.y);
                    }
                }
                break;
        }

        /* 바뀐 값 적용 */
        imageView.setImageMatrix(matrix);

        /* 이벤트가 성공적으로 처리되었음을 알림 */
        return true;
    }

    /* 두 점의 거리 측정 */
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    /* 두 점의 중간 점 획득 */
    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    /* Feature detecting 버튼을 눌렀을 때 호출 */
    public void feature_detecting_click(View v) {
        /* 비동기화를 위해 클래스 변수 선언 및 실행 */
        SIFT_tasks feature_detecting = new SIFT_tasks(1, "Feature detecting");
        feature_detecting.execute();
    }

    /* Feature matching 버튼을 눌렀을 때 호출 */
    public void feature_matching_click(View v) {
        /* 비동기화를 위해 클래스 변수 선언 및 실행 */
        SIFT_tasks feature_detecting = new SIFT_tasks(2, "Feature matching");
        feature_detecting.execute();
    }
}