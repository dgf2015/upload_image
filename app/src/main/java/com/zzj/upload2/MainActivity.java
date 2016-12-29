package com.zzj.upload2;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UpProgressHandler;
import com.qiniu.android.storage.UploadManager;
import com.qiniu.android.storage.UploadOptions;

import org.json.JSONObject;

import java.io.File;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";
    private static final int PHOTO_REQUEST_CAMERA = 1;// 拍照
    private static final int PHOTO_REQUEST_GALLERY = 2;// 从相册中选择
    private static final int PHOTO_REQUEST_CUT = 3;// 结果

    private ImageView mFace;
    private Button btnGallery, btnCamera, btnUpload;
    private Bitmap bitmap;

    /* 头像名称 */
    private static final String PHOTO_FILE_NAME = "temp_photo.jpg";
    private File tempFile;

    Uri uri = null;

    //服务器地址
    String baseUrl = "http://oh0vbg8a6.bkt.clouddn.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.mFace = (ImageView) this.findViewById(R.id.iv_image);

        uri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), PHOTO_FILE_NAME));

        btnGallery = (Button) findViewById(R.id.gallery);
        btnCamera = (Button) findViewById(R.id.camera);
        btnUpload = (Button) findViewById(R.id.upload);

        btnGallery.setOnClickListener(this);
        btnCamera.setOnClickListener(this);
        btnUpload.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.camera:
                camera();
                break;
            case R.id.gallery:
                gallery();
                break;
            case R.id.upload:
                upload();
                break;
        }
    }

    /*
     * 上传图片
     */
    public void upload( ) {

        //上传路径


        //上传进度
        final UpProgressHandler handler = new UpProgressHandler() {
            @Override
            public void progress(String key, double percent) {
                Log.i("=========", percent + ">>>>");
            }
        };


        UploadOptions options = new UploadOptions(null, null, false, handler, null);
        UploadManager uploadManager = QiNiuUpHelper.getUploadManager();
        String uploadToken = QiNiuUpHelper.getUploadToken();
        File f = new File(Environment.getExternalStorageDirectory(), PHOTO_FILE_NAME);

        String absolutePath = f.getAbsolutePath();
        Log.i(TAG, "absolutePath:===== " + absolutePath);
        uploadManager.put(f, baseUrl, uploadToken, new UpCompletionHandler() {
            @Override
            public void complete(String key, ResponseInfo info, JSONObject response) {
                if (info.isOK()) {
                    //上传完成
                    Log.i(TAG, "长传完成===============================: ");
                    Toast.makeText(MainActivity.this, "长传完成", Toast.LENGTH_SHORT).show();
                }
            }
        }, options);


    }

    /*
     * 从相册获取
     */

    public void gallery( ) {
        // 激活系统图库，选择一张图片
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(intent, PHOTO_REQUEST_GALLERY);
    }

    /*
     * 从相机获取
     */
    public void camera( ) {
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        // 判断存储卡是否可以用，可用进行存储
        if (hasSdcard()) {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        }
        startActivityForResult(intent, PHOTO_REQUEST_CAMERA);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PHOTO_REQUEST_GALLERY) {
            if (data != null) {
                // 得到图片的全路径
                Uri uri = data.getData();
                crop(uri);
            }

        } else if (requestCode == PHOTO_REQUEST_CAMERA) {
            if (hasSdcard()) {
                tempFile = new File(Environment.getExternalStorageDirectory(),
                        PHOTO_FILE_NAME);
                crop(Uri.fromFile(tempFile));
            } else {
                Toast.makeText(MainActivity.this, "未找到存储卡，无法存储照片！", Toast.LENGTH_SHORT).show();
            }

        } else if (requestCode == PHOTO_REQUEST_CUT) {
            try {


                bitmap = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory() + "/" + PHOTO_FILE_NAME);


//                bitmap = BitmapFactory.decodeFile(getContentResolver().openInputStream(uri2));
//                Bitmap bitmap = decodeUriAsBitmap(uri);


                mFace.setImageBitmap(bitmap);


                //System.out.println("delete = " + delete);


            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 剪切图片
     *
     * @param uri
     * @function:
     * @author:Jerry
     * @date:2013-12-30
     */
    private void crop(Uri uri) {
        // 裁剪图片意图
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        // 裁剪框的比例，1：1
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // 裁剪后输出图片的尺寸大小
        intent.putExtra("outputX", 250);
        intent.putExtra("outputY", 250);
        // 图片格式
        intent.putExtra("outputFormat", "JPEG");
        intent.putExtra("noFaceDetection", true);// 取消人脸识别
        //路径
        intent.putExtra("return-data", false);// true:不返回uri，false：返回uri
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(Environment.getExternalStorageDirectory(), PHOTO_FILE_NAME)));

        startActivityForResult(intent, PHOTO_REQUEST_CUT);
    }

    private boolean hasSdcard() {
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            return true;
        } else {
            return false;
        }
    }


}
