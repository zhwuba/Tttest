package com.freeme.themeclub.wallpaper.util;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipFile;

public class InputStreamLoader {

    ByteArrayInputStream mByteArrayInputStream = null;
    private Context mContext = null;
    private InputStream mInputStream = null;
    private String mPath = null;
    private Uri mUri = null;
    private ZipFile mZipFile = null;
    private String mZipPath = null;

    public InputStreamLoader(Context context, Uri uri) {
        if (ContentResolver.SCHEME_FILE.equals(uri.getScheme())) {
            mPath = uri.getPath();
        } else {
            mContext = context;
            mUri = uri;
        }
    }

    public InputStreamLoader(String path) {
        mPath = path;
    }

    public InputStreamLoader(String zipPath, String entry) {
        mZipPath = zipPath;
        mPath = entry;
    }

    public InputStreamLoader(byte[] data) {
        mByteArrayInputStream = new ByteArrayInputStream(data);
    }

    public void close() {
        if (mInputStream != null) {
			try {
				mInputStream.close();
			} catch (IOException e) {
			}
        }
        if (mZipFile != null) {
			try {
				mZipFile.close();
			} catch (IOException e) {
			}
        }
    }

    public InputStream get() {
        close();
        if (mUri == null) {
        	try {
                if (mZipPath != null) {
                    mZipFile = new ZipFile(mZipPath);
                    mInputStream = mZipFile.getInputStream(mZipFile.getEntry(mPath));
                } else if (mPath != null) {
                    mInputStream = new FileInputStream(mPath);
                } else if (mByteArrayInputStream != null) {
                    mByteArrayInputStream.reset();
                    mInputStream = mByteArrayInputStream;
                }
            } catch (Exception e) {
            }
        } else {
        	try {
				mInputStream = mContext.getContentResolver().openInputStream(mUri);
			} catch (FileNotFoundException e) {
			}
        }
        if (mInputStream != null && !(mInputStream instanceof ByteArrayInputStream)) {
            mInputStream = new BufferedInputStream(mInputStream, 16384);
        }
        return mInputStream;
    }
}
