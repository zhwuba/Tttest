package com.freeme.themeclub.wallpaper.local;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.freeme.themeclub.R;
import com.freeme.themeclub.wallpaper.ResourceHelper;
import com.freeme.themeclub.wallpaper.os.ExtraFileUtils;
import com.freeme.themeclub.wallpaper.resource.Resource;

public class ImageResourceFolder extends ResourceFolder {

    private Resource mEmptyTranslatePaper;
    private BitmapFactory.Options mOptions;

    public ImageResourceFolder(Context context, Bundle metaData, String folderPath) {
        this(context, metaData, folderPath, null);
    }

    public ImageResourceFolder(Context context, Bundle metaData, String folderPath, String keyword) {
        super(context, metaData, folderPath, keyword);
        mOptions = new BitmapFactory.Options();
        mOptions.inJustDecodeBounds = true;
    }

    private Resource getResource(String filePath) {
        File file = new File(filePath);
        Resource resource = new Resource();
        
        Bundle information = new Bundle();
        information.putString(Resource.NAME, file.getName());
        information.putString(Resource.SIZE, String.valueOf(file.length()));
        information.putLong(Resource.MODIFIED_TIME, file.lastModified());
        information.putString(Resource.LOCAL_PATH, filePath);
        ArrayList<String> previews = new ArrayList<String>(1);
        previews.add(filePath);
        information.putStringArrayList(Resource.LOCAL_PREVIEWS, previews);
        information.putStringArrayList(Resource.LOCAL_THUMBNAILS, previews);
        
        resource.setInformation(information);
        return resource;
    }

    @Override
    protected void addItem(String filePath) {
        BitmapFactory.decodeFile(filePath, mOptions);
        mFileFlags.put(filePath, (mOptions.outHeight == -1) ? 0 : 1);
    }

    @Override
    protected Resource buildResource(String filePath) {
        if (mFileFlags.get(filePath) == 0) {
            return null;
        }

        Resource resource = super.buildResource(filePath);
        Bundle information = resource.getInformation();
        ArrayList<String> previews = new ArrayList<String>(1);
        previews.add(filePath);
        information.putStringArrayList(Resource.LOCAL_PREVIEWS, previews);
        information.putStringArrayList(Resource.LOCAL_THUMBNAILS, previews);
        // ---
        BitmapFactory.decodeFile(filePath, mOptions);
        information.putString(Resource.RESOLUTION, 
        		String.format("%dx%d", new Object[] { mOptions.outWidth, mOptions.outHeight }));
        // +++
        resource.setInformation(information);
        
        return resource;
    }

    public void enableTransparentWallpaper(boolean enable) {
        String filePath = String.format("%s%s.png", new Object[] {
        		ExtraFileUtils.standardizeFolderPath(mFolderPath), 
        		mContext.getString(R.string.resource_transparent_wallpaper)
        		});
        File file = new File(filePath);
        if (!file.exists()) {
			ResourceHelper.writeTo(
					mContext.getResources().openRawResource(
							R.raw.empty_transparent), filePath);
        }
        
        mEmptyTranslatePaper = (file.exists() && enable) ? getResource(filePath) : null;
    }

    @Override
    public List<Resource> getHeadExtraResource() {
        if (mEmptyTranslatePaper == null) { 
        	return null;
        }
        
    	ArrayList<Resource> list = new ArrayList<Resource>();
    	list.add(mEmptyTranslatePaper);
        return list;
    }
}