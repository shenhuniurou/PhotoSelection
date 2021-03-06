package com.wxx.photoselection;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

/**
 * Created by Administrator on 2016/4/21.
 */
public class GalleryAdapter extends BaseAdapter {

    Context mContext;
    LayoutInflater infalter;
    List<PhotoInfo> photoInfoList;
    ImageLoader imageLoader;
    private int mScreenWidth;
    int selectCount = 0;
    int maxSelectCount = 5;

    public GalleryAdapter(Context context, List<PhotoInfo> photoInfoList) {
        mContext = context;
        infalter = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.photoInfoList = photoInfoList;
        imageLoader = imageLoader.getInstance();
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        mScreenWidth = outMetrics.widthPixels;
    }

    @Override
    public int getCount() {
        return photoInfoList == null ? 0 : photoInfoList.size();
    }

    @Override
    public PhotoInfo getItem(int position) {
        return photoInfoList == null ? null : photoInfoList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void changeSelection(View view, int position) {
        if (photoInfoList.get(position).isSelected) {
            photoInfoList.get(position).isSelected = false;
            selectCount--;
        } else {
            //判断选中数量有没有超过限制数
            if (selectCount >= maxSelectCount) {
                Snackbar.make(view, "最多不超过" + maxSelectCount + "张", Snackbar.LENGTH_SHORT).show();
                return;
            }
            photoInfoList.get(position).isSelected = true;
            selectCount++;
        }
        ((ViewHolder) view.getTag()).imgQueueMultiSelected.setSelected(photoInfoList.get(position).isSelected);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = infalter.inflate(R.layout.gallery_item, null);
            holder = new ViewHolder();
            holder.imgQueue = (ImageView) convertView.findViewById(R.id.imgQueue);
            setImageViewHeight(holder.imgQueue);
            holder.imgQueueMultiSelected = (ImageView) convertView.findViewById(R.id.imgQueueMultiSelected);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.imgQueue.setTag(position);
        try {
            holder.imgQueue.setImageResource(R.drawable.ic_default);
            imageLoader.displayImage("file://" + photoInfoList.get(position).imgPath, holder.imgQueue);
            holder.imgQueueMultiSelected.setSelected(photoInfoList.get(position).isSelected());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return convertView;
    }

    public void setImageViewHeight(ImageView imageView) {
        int imgHeight = (mScreenWidth - 4) / 3;
        ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
        layoutParams.height = imgHeight;
    }

    static class ViewHolder {
        ImageView imgQueue;
        ImageView imgQueueMultiSelected;
    }

}
