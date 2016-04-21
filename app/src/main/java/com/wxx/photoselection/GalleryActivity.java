package com.wxx.photoselection;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.InjectView;

public class GalleryActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{

    GridView gallery;
    GalleryAdapter mAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        List<PhotoInfo> photoInfoList = getPhotos();
        mAdapter = new GalleryAdapter(GalleryActivity.this, photoInfoList);
        gallery = (GridView) findViewById(R.id.gallery);
        gallery.setAdapter(mAdapter);
        gallery.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mAdapter.changeSelection(view, position);
    }

    /**
     * 获取相册图片
     * @return
     */
    private ArrayList<PhotoInfo> getPhotos() {
        ArrayList<PhotoInfo> galleryList = new ArrayList<PhotoInfo>();
        try {
            final String[] columns = { MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID };
            final String orderBy = MediaStore.Images.Media._ID;

            @SuppressWarnings("deprecation")
            Cursor imagecursor = managedQuery(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null, null, orderBy);
            if (imagecursor != null && imagecursor.getCount() > 0) {

                while (imagecursor.moveToNext()) {
                    PhotoInfo item = new PhotoInfo();
                    int dataColumnIndex = imagecursor.getColumnIndex(MediaStore.Images.Media.DATA);
                    item.imgPath = imagecursor.getString(dataColumnIndex);
                    galleryList.add(item);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // show newest photo at beginning of the list
        Collections.reverse(galleryList);
        return galleryList;
    }

}
