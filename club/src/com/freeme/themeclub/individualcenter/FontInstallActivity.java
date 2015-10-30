package com.freeme.themeclub.individualcenter;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.lang.ref.SoftReference;
import java.nio.channels.FileChannel;

import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.storage.StorageManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.InflateException;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.webkit.WebView.FindListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SimpleCursorTreeAdapter.ViewBinder;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.StatusBarManager;
import android.app.WallpaperManager;
import android.content.res.Resources.NotFoundException;
import android.content.res.Resources.ThemeInfo;
import android.app.IActivityManager;
import android.app.ActivityManagerNative;

import com.android.internal.util.AsyncImageCache;
import com.freeme.themeclub.R;

/**
 * @auther tyd Jack 20130726 for, support font manager
 */
public class FontInstallActivity extends Activity {
    private static String DEFAULT = "default";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        if (getFragmentManager().findFragmentById(android.R.id.content) == null) {
            FontSetupFragment fragment = new FontInstallActivity.FontSetupFragment();
            getFragmentManager().beginTransaction().add(android.R.id.content, fragment).commit();
        }

    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            setResult(1);
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * @auther tyd Jack 20130726 for, support font manager
     */
    public static class FontSetupFragment extends Fragment {
        private static final String TAG = "FontSetup";

        private static String SYSTEM_CUSTOM_FONT_PATH = "/system/fonts/custom/";

        private static String SDCARD_FONT_PATH = Environment.getExternalStorageDirectory()+"/fonts/";

        private File mUserFontsDir;
        private String mCurrentFont = Configuration.getFont();;
        private static String DEFAULT = "default";

        private static final int MENU_ID_INSTALL = Menu.FIRST;
        private static final int MENU_ID_DELETE = Menu.FIRST + 1;
        private static final int MENU_ID_INTRO = Menu.FIRST + 2;

        private GridView mGrid;
        private FontAdapter mAdapter;
        private HashMap<String, String> mInstallFonts = new HashMap<String, String>(5);
        private HashMap<String, String> mSDCardFontMap = new HashMap<String, String>(5);

        private ArrayList<FontInfo> mSDCardFonts = new ArrayList<FontInfo>();

        private AsyncImageCache mAsyncImageCache;
        private static StorageManager sm ;
        private Button installBtn;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View contentView = inflater.inflate(R.layout.font_grid_view1,
                    container, false);
            installBtn  = (Button)contentView.findViewById(R.id.install_btn);
            installBtn.setOnClickListener(new View.OnClickListener() {
                
                @Override
                public void onClick(View v) {
                    install();
                }
            });
            
            mGrid = (GridView) contentView.findViewById(R.id.grid_view);
            View emptyView = contentView.findViewById(R.id.sd_font_notfound_view);
            mGrid.setEmptyView(emptyView);
            mAdapter = new FontAdapter(inflater, getActivity());
            mGrid.setAdapter(mAdapter);
            mGrid.setOnItemClickListener(new OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View v,
                        int position, long id) {

                    FontInfo fontInfo = mSDCardFonts.get(position);

                    if (!fontInfo.mInstalled && !fontInfo.mUsing) {
                        FontAdapter.ViewHolder holder = (FontAdapter.ViewHolder) v.getTag();
                        holder.check.toggle();

                        fontInfo.mChecked = holder.check.isChecked();

                        boolean enable = false;
                        for (FontInfo fontInfo2 : mSDCardFonts) {
                            if (fontInfo2.mChecked) {
                                enable = true;
                            }
                        }

                        installBtn.setEnabled(enable);
                    }
                }
            });
            
            
            return contentView;
        }

        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setHasOptionsMenu(true);

            mAsyncImageCache = AsyncImageCache.from(this.getActivity());

            mUserFontsDir = new File(getActivity().getFilesDir(), "fonts");
        }

        private static String[] getSDPaths(Context context){
            sm = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
            String[] paths=null;
            try {
                paths=(String[]) sm.getClass().getMethod("getVolumePaths", null).invoke(sm, null);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return paths;
        }

        @Override
        public void onResume() {

            mCurrentFont = Configuration.getFont();
            if (TextUtils.isEmpty(mCurrentFont))
                mCurrentFont = DEFAULT;

            refreshView();
            boolean enable = false;
            for (FontInfo fontInfo2 : mSDCardFonts) {
                if (fontInfo2.mChecked) {
                    enable = true;
                }
            }

            installBtn.setEnabled(enable);
            super.onResume();
        }

        @Override
        public void onDestroy() {
            mAsyncImageCache.stop();
            super.onDestroy();
        }

        private void refreshView() {
            loadFonts();
            loadSDCardFonts();
            mAdapter.notifyDataSetChanged();
            getActivity().invalidateOptionsMenu();
        }

        private void loadFonts() {

            if (null != mInstallFonts) {
                mInstallFonts.clear();
            }

            loadCustomTypeface(SYSTEM_CUSTOM_FONT_PATH, mInstallFonts);
            loadCustomTypeface(mUserFontsDir.getAbsolutePath(), mInstallFonts);
            mInstallFonts.put(DEFAULT, "");

        }

        public static void loadCustomTypeface(String fontPath, HashMap<String, String> map) {
            File dir = new File(fontPath); 
            File[] files = dir.listFiles(); 
            if (files == null) 
                return; 
            for (File file: files) { 
                String filePath = file.getAbsolutePath();
                String fontKey = file.getName();
                if (fontKey.matches("^[a-zA-Z\\d_-]+\\.ttf$")){ 
                    map.put(fontKey, filePath);
                } 
            } 
        }

        private void loadSDCardFonts() {

            if (null != mSDCardFonts) {
                mSDCardFonts.clear();
            }
            if (null != mSDCardFontMap) {
                mSDCardFontMap.clear();
            }

            String[] paths = getSDPaths(getActivity());
            if(paths!=null){
                for(int i=0;i<paths.length;i++){
                    loadCustomTypeface(paths[i]+"/fonts/", mSDCardFontMap);
                }
            }


            for (Map.Entry<String, String> entry: mSDCardFontMap.entrySet()) {
                FontInfo fontInfo = new FontInfo(
                        getActivity(), entry.getValue());

                fontInfo.setFontKey(entry.getKey());

                fontInfo.mInstalled = (mInstallFonts.get(fontInfo.mFontKey) != null);
                fontInfo.mUsing = (mCurrentFont.equals(fontInfo.mFontKey));

                mSDCardFonts.add(fontInfo);
            }

            Collections.sort(mSDCardFonts, new Comparator<FontInfo>(){

                @Override
                public int compare(FontInfo lhs, FontInfo rhs) {
                    if (lhs.mInstalled)
                        return -1;
                    else if (rhs.mInstalled)
                        return 1;
                    else
                        return rhs.getFontKey().compareToIgnoreCase(lhs.getFontKey());
                }

            });
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

//            MenuItem item1 = menu.add(Menu.NONE, MENU_ID_INSTALL, 0, R.string.install);
//            item1.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            menu.add(Menu.NONE, MENU_ID_INTRO, 0, R.string.install_intro)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            super.onCreateOptionsMenu(menu, inflater);
        }

//        @Override
//        public void onPrepareOptionsMenu(Menu menu) {
//            boolean enable = false;
//            for (FontInfo fontInfo : mSDCardFonts) {
//                if (fontInfo.mChecked) {
//                    enable = true;
//                }
//            }
//
//            menu.findItem(MENU_ID_INSTALL).setEnabled(enable);
//
//            super.onPrepareOptionsMenu(menu);
//        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().setResult(1);
                getActivity().finish();
                break;
            /*case MENU_ID_INSTALL:
                install();
                return true;

            case MENU_ID_DELETE:
                delete();
                return true;*/
            case MENU_ID_INTRO:
                new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.install_intro_title))
                .setMessage(getString(R.string.install_intro_info))
                .setPositiveButton(android.R.string.ok, null)
                .show();
                return true;
            }
            return super.onOptionsItemSelected(item);
        }

        private void install() {

            if (!mUserFontsDir.exists())
            {
                mUserFontsDir.mkdirs();
                FileUtils.setPermissions(
                        mUserFontsDir.getAbsolutePath(),
                        FileUtils.S_IRWXU|FileUtils.S_IRWXG|FileUtils.S_IRWXO,
                        -1, -1);
            }

            new InstallFontTask(getActivity()).execute(mSDCardFonts);  

        }

        private void delete() {
            new DeleteFontTask(getActivity()).execute(mSDCardFonts);
        }

        class InstallFontTask extends AsyncTask<ArrayList<FontInfo>,String,String>{  
            boolean mCanceled = false;  
            ProgressDialog dialog;  
            String message;
            public InstallFontTask(Context context){  
                message = context.getString(R.string.installing);
                dialog = new ProgressDialog(context);  
                dialog.setMessage(message + "...");
                dialog.setCancelable(false);  
                dialog.show();  
            }  

            @Override  
            protected String doInBackground(ArrayList<FontInfo>... pramas) {  
                ArrayList<FontInfo> fonts = pramas[0];  
                for (FontInfo fontInfo : fonts) {
                    if (fontInfo.mChecked) {
                        publishProgress(fontInfo.mFontName);
                        File font = new File(mUserFontsDir, fontInfo.mFontKey);
                        FileUtils.copyFile(new File(fontInfo.mFontPath), font);
                        FileUtils.setPermissions(
                                font.getAbsolutePath(),
                                FileUtils.S_IRWXU|FileUtils.S_IRWXG|FileUtils.S_IRWXO,
                                -1, -1);
                    }
                }
                return null;  
            }  
            @Override
            protected void onProgressUpdate(String... values) {
                dialog.setMessage(message + " " + values[0]);
                super.onProgressUpdate(values);
            }
            protected void onPostExecute(String result){  
                dialog.dismiss();
                refreshView();
            }  
        } 

        class DeleteFontTask extends AsyncTask<ArrayList<FontInfo>,String,String>{  
            boolean mCanceled = false;  
            ProgressDialog dialog;  
            String message;
            public DeleteFontTask(Context context){  
                message = context.getString(R.string.deleting);
                dialog = new ProgressDialog(context);
                dialog.setMessage(message + "...");
                dialog.setCancelable(false);  
                dialog.show();  
            }  

            @Override  
            protected String doInBackground(ArrayList<FontInfo>... pramas) {  
                ArrayList<FontInfo> fonts = pramas[0];  
                for (FontInfo fontInfo : fonts) {
                    if (fontInfo.mChecked) {
                        publishProgress(fontInfo.mFontName);
                        File font = new File(fontInfo.mFontPath);
                        font.delete();
                    }
                }
                return null;  
            }  
            @Override
            protected void onProgressUpdate(String... values) {
                dialog.setMessage(message + " " + values[0]);
                super.onProgressUpdate(values);
            }
            protected void onPostExecute(String result){  
                dialog.dismiss();
                refreshView();
            }  
        } 

        public class FontAdapter extends BaseAdapter {
            private LayoutInflater mInflater;
            private Context mContext;

            public FontAdapter(LayoutInflater inflater, Context context) {
                mInflater = inflater;
                mContext = context;
            }

            public View getView(int position, View convertView, ViewGroup parent) {
                ViewHolder holder;

                if (convertView == null) {
                    convertView = mInflater.inflate(R.layout.grid_view_item_install_font, null);
                    holder = new ViewHolder();
                    holder.icon = (ImageView) convertView.findViewById(R.id.icon);
                    holder.status = (ImageView) convertView
                            .findViewById(R.id.status);
                    holder.text = (TextView) convertView.findViewById(R.id.text);
                    holder.type = (ImageView) convertView
                            .findViewById(R.id.type);

                    holder.check = (CheckBox) convertView.findViewById(R.id.check);

                    convertView.setTag(holder);

                } else {
                    holder = (ViewHolder) convertView.getTag();
                }

                FontInfo fontInfo = mSDCardFonts.get(position);

                //holder.icon.setImageDrawable(getFontPreview(fontInfo));

                mAsyncImageCache.displayImage(holder.icon, R.drawable.theme_default,
                        mContext.getResources().getDimensionPixelSize(R.dimen.theme_preview_w),
                        mContext.getResources().getDimensionPixelSize(R.dimen.theme_preview_h),
                        new AsyncImageCache.GeneralImageGenerator(fontInfo.mFontPath, fontInfo.getPreview().getBitmap()));



                holder.type.setVisibility(View.GONE);
                holder.text.setText(fontInfo.getFontName());

                holder.check.setChecked(fontInfo.mChecked);

                if (fontInfo.mUsing) {
                    holder.status.setImageResource(R.drawable.status_using);
                    holder.status.setVisibility(View.VISIBLE);
                    holder.check.setVisibility(View.INVISIBLE);
                    holder.check.setEnabled(false);
                } else if (fontInfo.mInstalled){
                    holder.status.setImageResource(R.drawable.status_installed);
                    holder.status.setVisibility(View.VISIBLE);
                    holder.check.setVisibility(View.INVISIBLE);
                    holder.check.setEnabled(false);
                } else {
                    holder.status.setVisibility(View.INVISIBLE);
                    holder.check.setVisibility(View.VISIBLE);
                    holder.check.setEnabled(true);
                }


                return convertView;
            }

            public final int getCount() {
                return mSDCardFonts.size();
            }

            public final Object getItem(int position) {
                return position;
            }

            public final long getItemId(int position) {
                return position;
            }

            class ViewHolder {
                ImageView icon;
                ImageView type;
                ImageView status;
                CheckBox check;
                TextView text;

            }
        }

        class FontInfo {

            private Context mContext;
            private BitmapDrawable mPreview;
            private String mFontPath;
            private String mFontKey;
            private String mFontName;
            private boolean mChecked;
            private boolean mInstalled;
            private boolean mUsing;


            public FontInfo(Context context, String fontPath) {
                mContext = context;
                mFontPath = fontPath;
            }

            public BitmapDrawable getPreview() {
                if (mPreview == null)
                {
                    Typeface tf;
                    if (DEFAULT.equals(mFontKey)) {
                        tf = Typeface.DEFAULT;
                    } else {
                        tf = Typeface.createFromFile(mFontPath);
                    }

                    int width = mContext.getResources().getDimensionPixelSize(R.dimen.theme_preview_w);
                    int height = mContext.getResources().getDimensionPixelSize(R.dimen.theme_preview_h);
                    Bitmap result =Bitmap.createBitmap(width, height, Config.ARGB_8888);
                    Canvas canvas=new Canvas(result);

                    canvas.drawColor(Color.parseColor("#DED6B2"));

                    Paint textPaint=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.DEV_KERN_TEXT_FLAG);
                    canvas.save();

                    textPaint.setColor(Color.BLACK);
                    textPaint.setTypeface(tf);

                    textPaint.setTextSize(15f * mContext.getResources().getDisplayMetrics().density);

                    canvas.drawText(getFontName(), 10, height * (1/4f), textPaint);

                    canvas.drawText(mContext.getString(R.string.freeme_font_preview_cn), (width-textPaint.getTextSize()/2*mContext.getString(R.string.freeme_font_preview_cn).length())/2, height * (1/3f), textPaint);
                    canvas.drawText(mContext.getString(R.string.freeme_font_preview_en), (width-textPaint.getTextSize()*mContext.getString(R.string.freeme_font_preview_en).length())/2, height * (9/17f), textPaint);

                    canvas.save(Canvas.ALL_SAVE_FLAG);
                    canvas.restore();

                    mPreview = new BitmapDrawable(mContext.getResources(), result);
                }

                return mPreview;
            }

            public String getFontKey() {
                return mFontKey;
            }

            public String getFontPath() {
                return this.mFontPath;
            }

            public String getFontName() {
                if (mFontName == null) {
                    if (DEFAULT.equals(mFontKey))
                        mFontName = mContext.getString(R.string.font_name_default);
                    else {
                        try {
                            TTFParser parser = new TTFParser();
                            parser.parse(mFontPath);
                            mFontName = parser.getFontName();
                        } catch (IOException e) {
                            mFontName = mContext.getString(R.string.font_name_unknow);
                        }
                    }
                }
                return this.mFontName;
            }

            public void setPreview(BitmapDrawable paramDrawable) {
                this.mPreview = paramDrawable;
            }

            public void setFontKey(String paramString) {
                this.mFontKey = paramString;
            }

            public void setFontPath(String paramString) {
                this.mFontPath = paramString;
            }
        }

    }
}
