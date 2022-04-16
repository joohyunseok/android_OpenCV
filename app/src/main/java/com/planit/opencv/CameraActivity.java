package com.planit.opencv;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Camera;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.planit.opencv.databinding.ActivityCameraBinding;
import com.planit.opencv.databinding.ActivityMainBinding;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class CameraActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG = "MainActivity";

    private ActivityCameraBinding binding;
    private Mat mRgba;
    private Mat mGray;
    private CameraBridgeViewBase mOpenCvCameraView;
    // 카메라 호출 정의 하기
    // 0 - 후면 카메라
    // 1 - 전면 카메라
    // initally 카메라는 후면
    private int mCameraId = 0;

    private BaseLoaderCallback mLoderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch(status){
                case LoaderCallbackInterface
                        .SUCCESS:{
                    Log.i(TAG, "OpenCV is Loaded");
                    mOpenCvCameraView.enableView();
                }
                default:
                {
                    super.onManagerConnected(status);
                }
                break;
            }

        }
    };

    public CameraActivity () {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        int MY_PERMISSIONS_REQUEST_CAMERA = 0;
        if(ContextCompat.checkSelfPermission(CameraActivity.this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(CameraActivity.this, new String[] {Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
        }
        binding = ActivityCameraBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.frameSurface.setVisibility(SurfaceView.VISIBLE);
        binding.frameSurface.setCvCameraViewListener(this);

        binding.ivFlipCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                swapCamera();
            }
        });
    }

    private void swapCamera() {
        // 카메라 번호 변경하기
        // if 0 change it to 1
        // if 1 change it to 0
        mCameraId = mCameraId^1; //basic not operation
        // 카메라뷰 잠시 멈추기
        mOpenCvCameraView.disableView();
        // 카메라번호 입력하기 후에 다시 카메라뷰 다시 시작
        mOpenCvCameraView.setCameraIndex(mCameraId);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!OpenCVLoader.initDebug()){
            Log.d(TAG, "OpenCV initailization is done");
            mLoderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
        else{
            Log.d(TAG, "OpenCv is not Loaded, try again");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0,this, mLoderCallback);
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        if(mOpenCvCameraView!= null){
            mOpenCvCameraView.disableView();
        }
    }

    public void onDetroy(){
        super.onDestroy();
        if(mOpenCvCameraView != null){
            mOpenCvCameraView.disableView();
        }
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height,width, CvType.CV_8UC4);
        mGray = new Mat(height,width, CvType.CV_8UC1);
    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();

        //to get Bgra display use this
        //Imgproc.cvtColor(mRgba,mRgba, Imgproc.COLOR_RGBA2BGRA);


        //카메라 전환 정의학
        //카메라 전면,후면 전환할 때 카메라 화면 rotation되는 문제가 있어 이를 해결하기 위해 아래 코드 작성
        // front camera is rotated by 180 : 전면에서는 180도 회전하는 문제
        // camera가 1일 때는 180도 돌려 주어야 한다.
        if(mCameraId==1){
            Core.flip(mRgba,mRgba, -1);
            Core.flip(mGray,mGray, -1);
        }

        return mRgba;
    }
}