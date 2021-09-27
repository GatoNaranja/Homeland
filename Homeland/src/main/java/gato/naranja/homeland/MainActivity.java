package gato.naranja.homeland;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.zxy.recovery.core.Recovery;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import gato.naranja.globalexcaugh.GlobalExCaught_2_0;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {
    private GNWebView webView;
    private SwipeRefreshLayout srl;

    public static SharedPreferences sp;
    private static final String url = "http://spixii.cn/";
    public static final int REQUEST_SELECT_FILE_CODE = 100;
    private static final int REQUEST_FILE_CHOOSER_CODE = 101;
    private static final int REQUEST_FILE_CAMERA_CODE = 102;
    /** 默认图片压缩大小（单位：K） */
    public static final int IMAGE_COMPRESS_SIZE_DEFAULT = 400;
    /** 压缩图片最小高度 */
    public static final int COMPRESS_MIN_HEIGHT = 900;
    /** 压缩图片最小宽度 */
    public static final int COMPRESS_MIN_WIDTH = 675;

    private ValueCallback<Uri> mUploadMsg;
    private ValueCallback<Uri[]> mUploadMsgs;
    /** 相机拍照返回的图片文件 */
    private File mFileFromCamera;
    private BottomSheetDialog selectPicDialog;
    private boolean mIsOpenCreateWindow;
    private WindowWebFragment mNewWindowWebFragment;


    @SuppressLint("JavascriptInterface")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Recovery.getInstance()
                .debug(true)
                .recoverInBackground(false)
                .recoverStack(true)
                .mainPage(MainActivity.class)
                .recoverEnabled(true)
                .callback(new GlobalExCaught_2_0(MainActivity.this))
                .silent(true, Recovery.SilentMode.RECOVER_ACTIVITY_STACK)
                .init(this);


        sp = getSharedPreferences("Spixii", MODE_PRIVATE);

        initViews();
    }

    private void initViews(){
        webView = findViewById(R.id.webView);
        srl = findViewById(R.id.srl);

        srl.setOnRefreshListener(new OnRefresh());

        webView.setOpenFileChooserCallBack(new GNWebChromeClient.OpenFileChooserCallBack() {
            @Override
            public void openFileChooserCallBack(ValueCallback<Uri> uploadMsg, String acceptType) {
                mUploadMsg = uploadMsg;
                showSelectPictrueDialog(0, null);
            }

            @Override
            public void showFileChooserCallBack(ValueCallback<Uri[]> filePathCallback,
                                                WebChromeClient.FileChooserParams fileChooserParams) {
                if (mUploadMsgs != null) {
                    mUploadMsgs.onReceiveValue(null);
                }
                mUploadMsgs = filePathCallback;
                showSelectPictrueDialog(1, fileChooserParams);
            }
        });
        webView.setCreateWindowCallBack((view, isDialog, isUserGesture, resultMsg) -> {
            mIsOpenCreateWindow = true;
            mNewWindowWebFragment = WindowWebFragment.newInstance();
            mNewWindowWebFragment.setMessage(resultMsg);
            getSupportFragmentManager().beginTransaction().
                    add(R.id.container_for_fragment, mNewWindowWebFragment).commit();
        });
        webView.setSwipeRefreshLayout(srl);

        webView.loadUrl(url);//加载论坛
    }

    private void syncCookies(Context c, String cookie){
        CookieSyncManager.createInstance(c);
        CookieManager cm = CookieManager.getInstance();
        cm.setAcceptCookie(true);
        cm.setCookie(cookie, null);
        CookieSyncManager.getInstance().sync();
    }

    /**
     * 选择图片弹框
     */
    private void showSelectPictrueDialog(final int tag, final WebChromeClient.FileChooserParams fileChooserParams) {
        selectPicDialog = new BottomSheetDialog(this);
        selectPicDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                if(mUploadMsgs != null){
                    mUploadMsgs.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(0, null));
                    mUploadMsgs = null;
                }
            }
        });

        View view = LayoutInflater.from(this).inflate(R.layout.upload_selector, null);
        ImageButton shoot = view.findViewById(R.id.shoot);
        ImageButton gallery = view.findViewById(R.id.gallery);

        gallery.setOnClickListener(view12 -> {
            if (tag == 0) {
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("*/*");
                startActivityForResult(Intent.createChooser(i, "File Browser"), REQUEST_FILE_CHOOSER_CODE);
            } else {
                try {
                    Intent intent = fileChooserParams.createIntent();
                    startActivityForResult(intent, REQUEST_SELECT_FILE_CODE);
                } catch (ActivityNotFoundException e) {
                    mUploadMsgs = null;
                }
            }
            selectPicDialog.dismiss();
        });
        shoot.setOnClickListener(view1 -> {
            takeCameraPhoto();
            selectPicDialog.dismiss();
        });
//        cancel.setOnClickListener(view13 -> selectPicDialog.dismiss());

        selectPicDialog.setContentView(view);
        selectPicDialog.show();
    }

    public void takeCameraPhoto() {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            Toast.makeText(this, "设备无摄像头", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!EasyPermissions.hasPermissions(this, Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            RequirePermission_EXTERNAL_STORAGE_();
        } else {
            startTakePhoto();
        }
    }

    private void startTakePhoto(){
            mFileFromCamera = getFileFromCamera();
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            Uri imgUrl;
            if (getApplicationInfo().targetSdkVersion > Build.VERSION_CODES.M) {
                String authority = "gato.naranja.homeland.UploadFileProvider";
                imgUrl = FileProvider.getUriForFile(this, authority, mFileFromCamera);
            } else {
                imgUrl = Uri.fromFile(mFileFromCamera);
            }
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imgUrl);
            startActivityForResult(intent, REQUEST_FILE_CAMERA_CODE);
    }

    private File getFileFromCamera() {
        File imageFile = null;
        String storagePath;
        File storageDir;
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        try {
            storagePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath();
            storageDir = new File(storagePath);
            storageDir.mkdirs();
            imageFile = File.createTempFile(timeStamp, ".jpg", storageDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return imageFile;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_CANCELED) {
            if (mUploadMsg != null) {
                mUploadMsg.onReceiveValue(null);
                mUploadMsg = null;
                return;
            }
            if (mUploadMsgs != null) {
                mUploadMsgs.onReceiveValue(null);
                mUploadMsgs = null;
                return;
            }
        }
        switch (requestCode) {
            case REQUEST_SELECT_FILE_CODE:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (mUploadMsgs == null) {
                        return;
                    }
                    mUploadMsgs.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data));
                    mUploadMsgs = null;
                }
                break;
            case REQUEST_FILE_CHOOSER_CODE:
                if (mUploadMsg == null) {
                    return;
                }
                Uri result = data == null || resultCode != RESULT_OK ? null : data.getData();
                mUploadMsg.onReceiveValue(result);
                mUploadMsg = null;
                break;
            case REQUEST_FILE_CAMERA_CODE:
                takePictureFromCamera();
                break;
            default:
                break;
        }
    }

    /**
     * 处理相机返回的图片
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void takePictureFromCamera() {
        if (mFileFromCamera != null && mFileFromCamera.exists()) {
            String filePath = mFileFromCamera.getAbsolutePath();
            // 压缩图片到指定大小
            File imgFile = GNImageUtils.compressImage(this, filePath, COMPRESS_MIN_WIDTH, COMPRESS_MIN_HEIGHT, IMAGE_COMPRESS_SIZE_DEFAULT);

            Uri localUri, result;
            if(imgFile == null) {
                localUri = Uri.EMPTY;
                result = Uri.EMPTY;
            }
            else {
                localUri = Uri.fromFile(imgFile);
                result = Uri.fromFile(imgFile);
            }
            Intent localIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, localUri);
            this.sendBroadcast(localIntent);

            if (mUploadMsg != null) {
                mUploadMsg.onReceiveValue(Uri.parse(filePath));
                mUploadMsg = null;
            }
            if (mUploadMsgs != null) {
                mUploadMsgs.onReceiveValue(new Uri[] { result });
                mUploadMsgs = null;
            }
        }
    }


    private class OnRefresh implements SwipeRefreshLayout.OnRefreshListener{
        @Override
        public void onRefresh() {
            webView.reload();
        }
    }

    private static final int RC_EXTERNAL_STORE = 0x1;
    private static final int RC_CAMERA = 0x3;
    @AfterPermissionGranted(RC_EXTERNAL_STORE)
    private void RequirePermission_EXTERNAL_STORAGE_() {
        String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(this, perms)) {
            // Already have permission, do the thing
            // ...
            RequirePermission_CAMERA_();
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    RC_EXTERNAL_STORE, perms);
        }
    }

    @AfterPermissionGranted(RC_CAMERA)
    private void RequirePermission_CAMERA_() {
        String[] perms = {Manifest.permission.CAMERA};
        if (EasyPermissions.hasPermissions(this, perms)) {
            // Already have permission, do the thing
            // ...
            startTakePhoto();

        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(MainActivity.this, Manifest.permission.CAMERA,
                    RC_CAMERA, perms);
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {

    }




    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.i("Naranja","是否有上一个页面:"+webView.canGoBack());
        if (webView.canGoBack() && keyCode == KeyEvent.KEYCODE_BACK){//点击返回按钮的时候判断有没有上一页
            webView.goBack(); // goBack()表示返回webView的上一页面
            return true;
        }
        return super.onKeyDown(keyCode,event);
    }

    @Override
    protected void onStart() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        super.onStart();
    }

    @Override
    protected void onStop() {
        syncCookies(MainActivity.this, sp.getString("cookie", ""));
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        //释放资源
        webView.destroy();
        webView=null;

        super.onDestroy();
    }
}