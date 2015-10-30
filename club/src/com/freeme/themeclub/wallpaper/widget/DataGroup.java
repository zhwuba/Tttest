package com.freeme.themeclub.wallpaper.widget;

import java.util.ArrayList;

public class DataGroup<T> extends ArrayList<T> {

    private static final long serialVersionUID = 1L;
    
    private String mTitle = null;

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
    	mTitle = title;
    }
}