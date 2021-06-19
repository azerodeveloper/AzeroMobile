package com.yiang.phoneareacode;

import android.app.Activity;
import androidx.fragment.app.Fragment;

/**
 * 创建：yiang
 * <p>
 * 描述：
 */
public class SelectPhoneCode {

    private Fragment fragment;
    private int requestCode = 0x1110;//默认请求码
    private String titleColor = "#ffffff";//默认标题颜色
    private String stickHeaderColor = "#f5f5f5";//默认标题颜色
    private String title = "区号选择";//默认标题颜色
    private String titleTextColor;//默认标题字体颜色

    private SelectPhoneCode(Fragment fragment) {
        this.fragment = fragment;
    }

    public static SelectPhoneCode with(Fragment fragment) {
        return new SelectPhoneCode(fragment);
    }

    public SelectPhoneCode setRequestCode(int requestCode) {
        this.requestCode = requestCode;
        return this;
    }

    public SelectPhoneCode select() {
        fragment.startActivityForResult(PhoneAreaCodeActivity
                .newInstance(fragment.getContext(), title,titleTextColor, titleColor, stickHeaderColor), requestCode);
        return this;
    }

    public SelectPhoneCode setTitle(String title) {
        this.title = title;
        return this;
    }

    public SelectPhoneCode setTitleTextColor(String titleTextColor) {
        this.titleTextColor = titleTextColor;
        return this;
    }

    public SelectPhoneCode setStickHeaderColor(String stickHeaderColor) {
        this.stickHeaderColor = stickHeaderColor;
        return this;
    }

    public SelectPhoneCode setTitleBgColor(String titleColor) {
        this.titleColor = titleColor;
        return this;
    }
}
