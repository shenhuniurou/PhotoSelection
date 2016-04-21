package com.wxx.photoselection;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    /** 打开相机 */
    public static final int ACTION_TYPE_PHOTO = 0;
    /** 打开相册single */
    public static final int ACTION_TYPE_ALBUM_SINGLE = 1;
    /** 打开相册multiple */
    public static final int ACTION_TYPE_ALBUM_MULTIPLE = 2;
    /** 请求相册 */
    public static final int REQUEST_CODE_GETIMAGE_BY_SDCARD = 3;
    /** 请求相机 */
    public static final int REQUEST_CODE_GETIMAGE_BY_CAMERA = 4;
    /** 请求裁剪 */
    public static final int REQUEST_CODE_GETIMAGE_BYCROP = 5;
    /** 请求自定义多选图片 */
    public static final int REQUEST_CODE_GETIMAGE_BY_SDCARD_MULTIPLE = 6;

    @InjectView(R.id.llImageContainer)
    LinearLayout llImageContainer;

    private String theLarge, theThumbnail;
    private File imgFile;

    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1 && msg.obj != null) {
                // 显示图片
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                params.setMargins(0, 0, 10, 0);
                ImageView imageView = new ImageView(MainActivity.this);
                imageView.setImageBitmap((Bitmap) msg.obj);
                llImageContainer.addView(imageView, params);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ButterKnife.inject(this);
        initImageLoader();
    }

    private void initImageLoader() {
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheOnDisc().imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
                .bitmapConfig(Bitmap.Config.RGB_565).build();
        ImageLoaderConfiguration.Builder builder = new ImageLoaderConfiguration.Builder(
                this).defaultDisplayImageOptions(defaultOptions).memoryCache(
                new WeakMemoryCache());

        ImageLoaderConfiguration config = builder.build();
        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.init(config);
    }

    @OnClick(R.id.btnShowDialog)
    public void showDialog() {
        String[] arrays = new String[] {"拍照", "相册（单选）", "相册（多选）"};
        getSelectDialog(MainActivity.this, arrays, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                selectPicture(which);
            }
        }).show();
    }

    public AlertDialog.Builder getSelectDialog(Context context, String[] arrays, DialogInterface.OnClickListener onClickListener) {
        return getSelectDialog(context, "", arrays, onClickListener);
    }

    public AlertDialog.Builder getSelectDialog(Context context, String title, String[] arrays, DialogInterface.OnClickListener onClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.MyAlertDialogStyle);
        builder.setItems(arrays, onClickListener);
        if (!TextUtils.isEmpty(title)) {
            builder.setTitle(title);
        }
        builder.setPositiveButton("取消", null);
        return builder;
    }

    private void selectPicture(int which) {
        Intent intent;
        switch (which) {
            case ACTION_TYPE_PHOTO://拍照
                // 判断是否挂载了SD卡
                String savePath = "";
                String storageState = Environment.getExternalStorageState();
                if (storageState.equals(Environment.MEDIA_MOUNTED)) {
                    savePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/camera/";
                    File savedir = new File(savePath);
                    if (!savedir.exists()) {
                        savedir.mkdirs();
                    }
                }

                // 没有挂载SD卡，无法保存文件
                if (savePath == null || "".equals(savePath)) {
                    System.out.println("无法保存照片，请检查SD卡是否挂载");
                    return;
                }

                String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
                //照片命名
                String fileName = timeStamp + ".jpg";
                File out = new File(savePath, fileName);
                Uri uri = Uri.fromFile(out);
                //该照片的绝对路径
                theLarge = savePath + fileName;
                intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                startActivityForResult(intent, REQUEST_CODE_GETIMAGE_BY_CAMERA);
                break;
            case ACTION_TYPE_ALBUM_SINGLE://系统相册
                if (Build.VERSION.SDK_INT < 19) {
                    intent = new Intent();
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");
                    startActivityForResult(Intent.createChooser(intent, "选择要使用的应用"), REQUEST_CODE_GETIMAGE_BY_SDCARD);
                } else {
                    intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(Intent.createChooser(intent, "选择要使用的应用"), REQUEST_CODE_GETIMAGE_BY_SDCARD);
                }
                break;
            case ACTION_TYPE_ALBUM_MULTIPLE:
                intent = new Intent(MainActivity.this, GalleryActivity.class);
                startActivityForResult(intent, REQUEST_CODE_GETIMAGE_BY_SDCARD_MULTIPLE);
                break;
        }
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent imageReturnIntent) {

        if (resultCode != Activity.RESULT_OK)
            return;

        new Thread() {
            private String selectedImagePath;

            @Override
            public void run() {
                Bitmap bitmap = null;
                if (requestCode == REQUEST_CODE_GETIMAGE_BY_SDCARD_MULTIPLE) {

                }else if (requestCode == REQUEST_CODE_GETIMAGE_BY_SDCARD) {
                    if (imageReturnIntent == null)
                        return;
                    Uri selectedImageUri = imageReturnIntent.getData();
                    if (selectedImageUri != null) {
                        selectedImagePath = ImageUtils.getImagePath(selectedImageUri, MainActivity.this);
                    }

                    if (selectedImagePath != null) {
                        theLarge = selectedImagePath;
                    } else {
                        bitmap = ImageUtils.loadPicasaImageFromGalley(selectedImageUri, MainActivity.this);
                    }
                    /*if (AppContext.isMethodsCompat(android.os.Build.VERSION_CODES.ECLAIR_MR1)) {
                        String imaName = FileUtil.getFileName(theLarge);
                        if (imaName != null)
                            bitmap = ImageUtils.loadImgThumbnail(getActivity(), imaName, MediaStore.Images.Thumbnails.MICRO_KIND);
                    }*/
                    if (bitmap == null && !StringUtils.isEmpty(theLarge))
                        bitmap = ImageUtils.loadImgThumbnail(theLarge, 300, 300);
                } else if (requestCode == REQUEST_CODE_GETIMAGE_BY_CAMERA) {
                    // 拍摄图片
                    if (bitmap == null && !StringUtils.isEmpty(theLarge)) {
                        bitmap = ImageUtils.loadImgThumbnail(theLarge, 300, 300);
                    }
                }

                if (bitmap != null) {// 存放照片的文件夹
                    String savePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/camera/";
                    File savedir = new File(savePath);
                    if (!savedir.exists()) {
                        savedir.mkdirs();
                    }

                    String largeFileName = theLarge.substring(theLarge.lastIndexOf(File.separator) + 1);
                    String largeFilePath = savePath + largeFileName;
                    // 判断是否已存在缩略图
                    if (largeFileName.startsWith("thumb_") && new File(largeFilePath).exists()) {
                        theThumbnail = largeFilePath;
                        imgFile = new File(theThumbnail);
                    } else {
                        // 生成上传的800宽度图片
                        String thumbFileName = "thumb_" + largeFileName;
                        theThumbnail = savePath + thumbFileName;
                        if (new File(theThumbnail).exists()) {
                            imgFile = new File(theThumbnail);
                        } else {
                            try {
                                // 压缩上传的图片
                                ImageUtils.createImageThumbnail(MainActivity.this, theLarge, theThumbnail, 800, 50);
                                imgFile = new File(theThumbnail);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    Message msg = new Message();
                    msg.what = 1;
                    msg.obj = bitmap;
                    handler.sendMessage(msg);
                }
            };
        }.start();
    }

}
