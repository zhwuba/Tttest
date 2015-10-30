package com.freeme.themeclub.individualcenter;

import java.io.File;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.internal.util.AsyncImageCache;
import com.freeme.themeclub.BackScrollFragment;
import com.freeme.themeclub.MainActivity;
import com.freeme.themeclub.R;
public class IndividualFontFragment extends BackScrollFragment{
    private static final String TAG = "FontManager";

    private static String SYSTEM_CUSTOM_FONT_PATH = "/system/fonts/custom/";

    private static String DEFAULT = "default";

    private static final int MENU_ID_APPLY = Menu.FIRST;
    private static final int MENU_ID_DELETE = Menu.FIRST + 1;
    private static final int MENU_ID_INSTALL = Menu.FIRST + 2;

    private File mUserFontsDir;

    private HashMap<String, String> mCustomTypeface =
            new HashMap<String, String>(5);

    private GridView mGrid;
    private FontAdapter mAdapter;
    private ArrayList<FontInfo> mFonts = new ArrayList<FontInfo>();

    private Handler mHandler;
    private String mCurrentFont = Configuration.getFont();
    private Object mLock = new Object();

    private AsyncImageCache mAsyncImageCache;
    private boolean fresh;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.fragment_individual_font,
                container, false);
        mGrid = (GridView) contentView.findViewById(R.id.grid_view);
        mAdapter = new FontAdapter(inflater, getActivity());
        mGrid.setAdapter(mAdapter);
        return contentView;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mAsyncImageCache = AsyncImageCache.from(this.getActivity());

        mUserFontsDir = new File(getActivity().getFilesDir(), "fonts");
        mHandler = new Handler();
        initData();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
        case 1:
            Log.w("yzy", "font activity result");
            initData();
            break;

        default:
            break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onResume() {

        mCurrentFont = Configuration.getFont();
        if (TextUtils.isEmpty(mCurrentFont))
            mCurrentFont = DEFAULT;

//        initData();

        super.onResume();
    }

    public void loadData(){
        if(!fresh){
            fresh=true;
            //            initData();
        }
    }

    @Override
    public void onDestroy() {
        mAsyncImageCache.stop();
        super.onDestroy();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(Menu.NONE, MENU_ID_INSTALL, 0, R.string.load_font)
        //.setIcon(R.drawable.font_install)
        .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) { 
        case MENU_ID_INSTALL: 
            Intent intent = new Intent(this.getActivity(),FontInstallActivity.class);
            startActivityForResult(intent, 1);
            break; 
        } 
        return super.onOptionsItemSelected(item);
    }

    private void initData() {
        new LoadFontInfosTask(getActivity()).executeOnExecutor(MainActivity.fixedThreadPool);
    }

    private ArrayList<FontInfo> loadFonts() {

        if (null != mFonts) {
            mFonts.clear();
        }

        if (null != mCustomTypeface) {
            mCustomTypeface.clear();
        }

        loadCustomTypeface(SYSTEM_CUSTOM_FONT_PATH, mCustomTypeface);
        loadCustomTypeface(mUserFontsDir.getAbsolutePath(), mCustomTypeface);
        mCustomTypeface.put(DEFAULT, "");

        for (Map.Entry<String, String> entry: mCustomTypeface.entrySet()) {

            FontInfo fontInfo = new FontInfo(
                    getActivity(), entry.getValue());

            fontInfo.setFontKey(entry.getKey());
            mFonts.add(fontInfo);
        }

        Collections.sort(mFonts, new Comparator<FontInfo>(){

            @Override
            public int compare(FontInfo lhs, FontInfo rhs) {
                if (lhs.getFontKey().equals(DEFAULT))
                    return -1;
                else if (rhs.getFontKey().equals(DEFAULT))
                    return 1;
                else if (lhs.getFontPath().startsWith(SYSTEM_CUSTOM_FONT_PATH))
                    return -1;
                else if (rhs.getFontPath().startsWith(SYSTEM_CUSTOM_FONT_PATH))
                    return 1;
                else
                    return rhs.getFontKey().compareToIgnoreCase(lhs.getFontKey());
            }

        });

        return mFonts;
    }

    public void onApply(int pos) {

        final Context context = getActivity();        
        final String font = mFonts.get(pos).getFontKey();

        if (!font.equals(mCurrentFont)) { 
            context.sendBroadcast(new Intent("com.android.music.command.theme.change"));
            Intent intent = new Intent(IndividualThemeFragment.ACTION_THEME_MANAGER_SERVICE);
            intent.setPackage("com.freeme.themeclub.core");
            intent.putExtra("action", "change_font");
            intent.putExtra("font", font);
            getActivity().startService(intent);

            mHandler.postDelayed(new Runnable() {
                public void run() {
                    getActivity().finish();
                }}, 500);
        }

    }

    private void onDelete(int pos) {
        new DeleteFontTask(getActivity()).executeOnExecutor(MainActivity.fixedThreadPool,mFonts.get(pos));
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

    class DeleteFontTask extends AsyncTask<FontInfo,String,String>{  
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
        protected String doInBackground(FontInfo... pramas) {  
            FontInfo fontInfo = pramas[0];  
            publishProgress(fontInfo.mFontName);
            File font = new File(fontInfo.mFontPath);
            font.delete();
            return null;  
        }  
        @Override
        protected void onProgressUpdate(String... values) {
            dialog.setMessage(message + " " + values[0]);
            super.onProgressUpdate(values);
        }
        protected void onPostExecute(String result){  
            dialog.dismiss();
            initData();
        }  
    } 

    public class FontAdapter extends ArrayAdapter<FontInfo> {
        private LayoutInflater mInflater;
        private Context mContext;

        public FontAdapter(LayoutInflater inflater, Context context) {
            super(context, 0);
            mInflater = inflater;
            mContext = context;
        }

        public void setData(List<FontInfo> data) {
            clear();
            if (data != null) {
                addAll(data);
            }
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.grid_view_item, null);
                holder = new ViewHolder();
                holder.icon = (ImageView) convertView.findViewById(R.id.icon);
                holder.status = (ImageView) convertView
                        .findViewById(R.id.status);
                holder.text = (TextView) convertView.findViewById(R.id.text);
                holder.type = (ImageView) convertView
                        .findViewById(R.id.type);
                holder.apply = (Button) convertView.findViewById(R.id.apply);
                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            FontInfo fontInfo = getItem(position);

            //			holder.icon.setImageDrawable(fontInfo.getPreview());

            mAsyncImageCache.displayImage(holder.icon, R.drawable.font_preview_default,
                    mContext.getResources().getDimensionPixelSize(R.dimen.font_preview_w),
                    mContext.getResources().getDimensionPixelSize(R.dimen.font_preview_h),
                    new AsyncImageCache.GeneralImageGenerator(
                            fontInfo.mFontPath, fontInfo.getPreview().getBitmap()));

            holder.type.setVisibility(View.GONE);
            holder.status.setImageResource(R.drawable.status_using);
            if (mCurrentFont.equals(fontInfo.getFontKey())) {
                holder.status.setVisibility(View.VISIBLE);
            } else {
                holder.status.setVisibility(View.INVISIBLE);
            }

            holder.text.setText(fontInfo.getFontName());
            holder.apply.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    onApply(position);
                }
            });
            return convertView;
        }

        class ViewHolder {
            ImageView icon;
            ImageView status;
            ImageView type;
            TextView text;
            Button apply;
        }
    }

    class LoadFontInfosTask extends AsyncTask<Void, Void, List<FontInfo>>{  
        public LoadFontInfosTask(Context context){  
            //ThemeManager.this.setListShown(false); 
        }  

        @Override  
        protected List<FontInfo> doInBackground(Void... pramas) {  

            return IndividualFontFragment.this.loadFonts();  
        }  

        @Override
        protected void onPostExecute(List<FontInfo> result){  
            //ThemeManager.this.setListShown(true); 
            mAdapter.setData(result);
        }  
    }

    class FontInfo {

        private Context mContext;
        private BitmapDrawable mPreview;
        private String mFontPath;
        private String mFontKey;
        private String mFontName;


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

                int width = mContext.getResources().getDimensionPixelSize(R.dimen.font_preview_w);
                int height = mContext.getResources().getDimensionPixelSize(R.dimen.font_preview_h);
                Bitmap result =Bitmap.createBitmap(width, height, Config.ARGB_8888);
                Canvas canvas=new Canvas(result);

                canvas.drawColor(Color.parseColor("#ffffff"));

                Paint textPaint=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.DEV_KERN_TEXT_FLAG);
                canvas.save();

                textPaint.setColor(getResources().getColor(R.color.primary_text_color));
                textPaint.setTypeface(tf);

                textPaint.setTextSize(15.6f * getResources().getDisplayMetrics().density);

                canvas.drawText(mContext.getString(R.string.freeme_font_preview_cn), 
                        (width-textPaint.measureText(
                                mContext.getString(R.string.freeme_font_preview_cn)))/2, 
                                height * (62/222f), textPaint);
                canvas.drawText(mContext.getString(R.string.freeme_font_preview_en), 
                        (width-textPaint.measureText(
                                mContext.getString(R.string.freeme_font_preview_en)))/2, 
                                height * (120/222f), textPaint);

                canvas.save(Canvas.ALL_SAVE_FLAG);
                canvas.restore();

                mPreview = new BitmapDrawable(getResources(), result);
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


