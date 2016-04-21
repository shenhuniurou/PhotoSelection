package com.wxx.photoselection;

/**
 * Created by Administrator on 2016/4/21.
 */
public class PhotoInfo {

    public String imgPath;

    public boolean isSelected = false;

    public String getImgPath() {
        return imgPath;
    }

    public void setImgPath(String imgPath) {
        this.imgPath = imgPath;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
