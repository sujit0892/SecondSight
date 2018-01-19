package com.example.marinex.secondsight;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Camera;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.security.Policy;
import java.util.List;

@SuppressWarnings("deprecation")
public class CameraActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2{

    private static final String TAGs = "MainActivity";

    static {
        if(!OpenCVLoader.initDebug()){
            Log.d(TAGs, "OpenCV not loaded");
        } else {
            Log.d(TAGs, "OpenCV loaded");
        }
    }

    private static final String TAG = CameraActivity.class.getSimpleName();

    private static final String STATE_CAMERA_INDEX = "cameraIndex";

    private static final String STATE_IMAGE_SIZE_INDEX = "imageSizeIndex";
    private static final int MENU_GROUP_ID_SIZE = 2;

    private int mCameraIndex;

    private int mImageSizeIndex;

    private boolean mIsCameraFrontFacing;

    private int mNumCamera;

    private CameraBridgeViewBase mCameraView;

    private List<android.hardware.Camera.Size> mSupportedImageSizes;

    private boolean mIsPhotoPending;

    private Mat mBgr;

    private boolean mIsMenuLocked;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    Log.d(TAG, "OpenCv loaded successfully");
                    mCameraView.enableView();
                    mBgr = new Mat();
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }

        }

    };


    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        while(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA,Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE}, 50);
            //ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 50);
        }

        /*while(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 50);

        while(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 50);*/


        final Window window = getWindow();
        window.addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (savedInstanceState != null) {
            mCameraIndex = savedInstanceState.getInt(
                    STATE_CAMERA_INDEX, 0);
            mImageSizeIndex = savedInstanceState.getInt(
                    STATE_IMAGE_SIZE_INDEX, 0);
        } else {
            mCameraIndex = 0;
            mImageSizeIndex = 0;
        }

        final android.hardware.Camera camera;
        if (Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.GINGERBREAD) {
            android.hardware.Camera.CameraInfo cameraInfo = new android.hardware.Camera.CameraInfo();
            android.hardware.Camera.getCameraInfo(mCameraIndex, cameraInfo);
            mIsCameraFrontFacing =
                    (cameraInfo.facing ==
                            android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT);
            mNumCamera = android.hardware.Camera.getNumberOfCameras();
            camera = android.hardware.Camera.open(mCameraIndex);
        } else { // pre-Gingerbread
            // Assume there is only 1 camera and it is rear-facing.
            mIsCameraFrontFacing = false;
            mNumCamera = 1;
            camera = android.hardware.Camera.open();
        }
        final android.hardware.Camera.Parameters parameters = camera.getParameters();
        camera.release();
        mSupportedImageSizes =
                parameters.getSupportedPreviewSizes();
        final android.hardware.Camera.Size size = mSupportedImageSizes.get(mImageSizeIndex);
       //mCameraView = findViewById(R.id.showCamera);
        mCameraView = new JavaCameraView(this, mCameraIndex);

        mCameraView.setMaxFrameSize(size.width, size.height);
        mCameraView.setCvCameraViewListener(this);
        setContentView(mCameraView);
     //   setContentView(R.layout.show_camera);
    }



    @Override
    public  void onSaveInstanceState(Bundle savedInstanceState)
    {
        savedInstanceState.putInt(STATE_CAMERA_INDEX, mCameraIndex);
        savedInstanceState.putInt(STATE_IMAGE_SIZE_INDEX, mImageSizeIndex);

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onCameraViewStarted(int width, int height) {



    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        final Mat rgba = inputFrame.rgba();
        if(mIsPhotoPending)
        {
            mIsPhotoPending = false;
           takePhoto(rgba);
        }
        if(mIsCameraFrontFacing)
            Core.flip(rgba,rgba,1);
        return rgba;
    }

    @SuppressLint("NewApi")
    @Override
    public void recreate()
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            super.recreate();
        else
        {
            finish();
            startActivity(getIntent());
        }
    }



    @Override
    public  void onPause() {
        if (mCameraView != null)
            mCameraView.disableView();
        super.onPause();

    }

    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_9, this, mLoaderCallback);
        mIsMenuLocked = false;

    }

    @Override
    public  void onDestroy()
    {
        if (mCameraView != null)
        {
            mCameraView.disableView();
        }
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu)
    {
        getMenuInflater().inflate(R.menu.camera_activity,menu);
        if(mNumCamera<2)
        {
            menu.removeItem(R.id.menu_next_camera);
        }
        int numSupportedImagesSizes =mSupportedImageSizes.size();
        if(numSupportedImagesSizes > 1 )
        {
            final SubMenu sizeSubMenu = menu.addSubMenu(R.string.menu_image_size);
            for (int i = 0;i < numSupportedImagesSizes; i++)
            {
                final android.hardware.Camera.Size size = mSupportedImageSizes.get(i);
                sizeSubMenu.add(MENU_GROUP_ID_SIZE, i, Menu.NONE, String.format("%dx%d", size.width, size.height));
            }
        }
        return true;

    }

    @SuppressLint("NewApi")
    @Override
    public boolean onOptionsItemSelected(final MenuItem item)
    {
        if(mIsMenuLocked)
        {
            return true;
        }

        if(item.getGroupId() == MENU_GROUP_ID_SIZE)
        {
            mImageSizeIndex = item.getItemId();
            recreate();

            return true;
        }

        switch (item.getItemId())
        {
            case R.id.menu_next_camera:
                mIsMenuLocked = true;
                mCameraIndex++;
                if(mCameraIndex == mNumCamera)
                    mCameraIndex = 0;

                mImageSizeIndex = 0;
                recreate();
                return true;
            case R.id.menu_take_photo:
                mIsMenuLocked = true;
                mIsPhotoPending = true;

                return true;

            default:
                return  super.onOptionsItemSelected(item);
        }
    }

    private void takePhoto(final Mat rgba)
    {   mCameraView.disableView();
        final  long currentTimeMillis = System.currentTimeMillis();
        final String appName = getString(R.string.app_name);
        final String gallaryPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
        final String albumPath = gallaryPath+File.separator+appName;
        final String photoPath = albumPath+File.separator+currentTimeMillis+LabActivity.PHOTO_FILE_EXTENSION;
        final ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DATA, photoPath);
        values.put(MediaStore.Images.Media.TITLE, appName);
        values.put(MediaStore.Images.Media.DESCRIPTION, appName);
        values.put(MediaStore.Images.Media.DATE_TAKEN, currentTimeMillis);

        File album = new File(albumPath);
        if(!album.isDirectory() && !album.mkdir())
        {
            Log.e(TAG, "FAiled to create directory at"+albumPath);
            onTakePhotoFailed();
            return;
        }

        Imgproc.cvtColor(rgba,mBgr, Imgproc.COLOR_RGBA2BGR);
        if(!Imgcodecs.imwrite(photoPath,mBgr))
        {
            Log.e(TAG, "Failed to Save photo to"+photoPath);
            onTakePhotoFailed();
        }


        Log.d(TAG,"Photo Saved successfully to"+photoPath);

        Uri uri;
        try {
            uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        }catch (final Exception e)
        {
            Log.e(TAG,"Failed to insert photo into Media Store");
            e.printStackTrace();
            File photo = new File(photoPath);
            if(!photo.delete())
            {
                Log.e(TAG,"Failed to delete non-inserted photo");
            }

            onTakePhotoFailed();
            return;

        }


        final Intent intent = new Intent(this, LabActivity.class);
        intent.putExtra(LabActivity.EXTRA_PHOTO_URI,uri);
        intent.putExtra(LabActivity.EXTRA_PHOTO_DATA_PATH,photoPath);
        startActivity(intent);


    }

    private void onTakePhotoFailed() {
        mIsMenuLocked = false;
        final String errorMessage = getString(R.string.photo_error_message);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(CameraActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }


}


