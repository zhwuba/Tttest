package com.freeme.themeclub.individualcenter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.android.internal.util.AsyncImageCache;
import com.freeme.themeclub.BackScrollFragment;
import com.freeme.themeclub.MainActivity;
import com.freeme.themeclub.NoScrollViewPager;
import com.freeme.themeclub.R;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.FileUtils;
import android.os.Handler;
import android.os.PowerManager;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

public class IndividualLockScreenFragment extends BackScrollFragment{
    private static final String TAG = "LockscreenManager";

    private static final String LOCKSCREEN_PACKAGE = "tyd_lockscreen_package";
    private static final String LOCKSCREEN_DEFAULT = "android";

    public static final String KEY_THEME_LOCKSCREEN_FUN_UX_VALUE = "key_theme_lockscreen_fun_ux_value";

    public static final String FUN_UX_DIR = "fun_ux";
    public static final String FUN_UX_DEFAULT_NAME = "fun_ux.ux";
    public static final String FUN_UX_ASSET_NAME = "w.ux";

    private File mFunUXDir;

    private GridView mGrid;
    private LockscreenPackageAdapter mAdapter;
    private ArrayList<LockscreenInfo> mLockscreenPackages = new ArrayList<LockscreenInfo>();
    private String[] mPackageNames;
    private String[] mPackagePaths;
    private String[] mTitles;

    private String mLockscreenPackage;

    private AsyncImageCache mAsyncImageCache;

    private static final int MENU_ID_ONLINE = Menu.FIRST;
    private final static String ONLINE_LOCKSCREENS_ACTION = "android.intent.action.OnlineLockscreens";

    private NoScrollViewPager mViewPager;
    private LinearLayout navigationLayout;
    private boolean fresh;

    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAsyncImageCache = AsyncImageCache.from(this.getActivity());
        initData();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addDataScheme("package");
        getActivity().registerReceiver(mPMSReceiver, filter);

        mFunUXDir = new File(getActivity().getFilesDir(), FUN_UX_DIR);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.fragment_individual_lockscreen, container,
                false);
        mGrid = (GridView) contentView.findViewById(R.id.grid_view);
        mAdapter = new LockscreenPackageAdapter(inflater, getActivity());
        mGrid.setAdapter(mAdapter);

        mGrid.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                    int position, long id) {

                Intent intent = new Intent(getActivity(),LockscreenDetailActivity.class);
                intent.putExtra(LockscreenDetailActivity.PACKAGENAME, mPackageNames);
                intent.putExtra(LockscreenDetailActivity.LOCKSCREENPATH, mPackagePaths);
                intent.putExtra(LockscreenDetailActivity.LOCKSCREENTITLE, mTitles);
                intent.putExtra(LockscreenDetailActivity.SELECTPOSITION, position);
                startActivityForResult(intent, 0);
            }
        });

        return contentView;
    }

    public void loadData(){
        if(!fresh){
            fresh=true;
//            initData();
        }
    }

    //    @Override
    //    public void onActivityCreated(Bundle savedInstanceState) {
    //        super.onActivityCreated(savedInstanceState);
    //        initData();
    //    }

    @Override
    public void onResume() {
        //        initData();
        super.onResume();
    }

    private void initFunUXDir() {
        try {
            if (mFunUXDir.exists()) {
                mFunUXDir.delete();
                Process p = Runtime.getRuntime().exec("rm -rf " + mFunUXDir.getAbsolutePath());
                p.waitFor();
            }

            mFunUXDir.mkdirs();
            chmodFileAccess(mFunUXDir.getAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        getActivity().unregisterReceiver(mPMSReceiver);
        mAsyncImageCache.stop();
        super.onDestroy();
    }

    /*
     *modify by huangyiquan,set the custom lockscreen wallpaper such as cd lcoskreen, 
     * when there is no pic named "default_wallpaper_lockscreen"
     */
    private static final int MSG_LCOKSREEN_WALLPAPER_CHANGE = 0;
    private Handler mHandler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
            case MSG_LCOKSREEN_WALLPAPER_CHANGE:
                Intent intent = new Intent("WallpaperManager.ACTION_LOCKSCREEN_WALLPAPER_CHANGED");
                getActivity().sendBroadcast(intent);
                break;
            default:
                break;
            }
        }

    };

    private BroadcastReceiver mPMSReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            initData();
            mAdapter.notifyDataSetChanged();
        }
    };


    private LoadLockscreenPackageTask mLoadTask = null;
    private void initData() {
        if(mLoadTask != null && !mLoadTask.isCancelled()){
            mLoadTask.cancel(true);
            mLoadTask = null;
        }
        mLoadTask = new LoadLockscreenPackageTask(getActivity());
        mLoadTask.executeOnExecutor(MainActivity.fixedThreadPool);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        int pos = -1;
        switch (resultCode) {
        case LockscreenDetailActivity.APPLY_OK:
            pos = data.getIntExtra(LockscreenDetailActivity.APPLY_INDEX, 0);
            break;
        }

        if (pos != -1) {
            onApply(pos);
        }
    }

    public String getLockscreenPackage() {
        if(getActivity() == null){
            return LOCKSCREEN_DEFAULT;
        }
        String result = Settings.System.getString(getActivity()
                .getContentResolver(), LOCKSCREEN_PACKAGE);
        return android.text.TextUtils.isEmpty(result) ? LOCKSCREEN_DEFAULT
                : result;
    }

    private synchronized ArrayList<LockscreenInfo> loadLockscreenPackages() {

        if (null != mLockscreenPackages) {
            mLockscreenPackages.clear();
        }

        if(getActivity()==null){
            return null;
        }
        List<PackageInfo> packages = getActivity().getPackageManager()
                .getInstalledPackages(0);
        for (int i = 0; i < packages.size(); i++) {
            PackageInfo packageInfo = packages.get(i);

            if (packageInfo.packageName.startsWith("com.freeme.lockscreen.")
                    || packageInfo.packageName.equals(LOCKSCREEN_DEFAULT)) {

                LockscreenInfo lockscreenInfo = new LockscreenInfo(
                        getActivity(), packageInfo.packageName, packageInfo.applicationInfo.sourceDir);

                if (lockscreenInfo.isLockscreenPackage())
                    mLockscreenPackages.add(lockscreenInfo);
            }
        }

        mLockscreenPackage = getLockscreenPackage();

        mPackageNames = new String[mLockscreenPackages.size()];
        mPackagePaths = new String[mLockscreenPackages.size()];
        mTitles = new String[mLockscreenPackages.size()];
        Collections.sort(mLockscreenPackages, new Comparator<LockscreenInfo>(){

            @Override
            public int compare(LockscreenInfo lhs, LockscreenInfo rhs) {
                //                if (mLockscreenPackage.equals(lhs.getPackageName()))
                //                    return -1;
                //                else if(mLockscreenPackage.equals(rhs.getPackageName()))
                //                    return 1;
                //                else 
                if(lhs.getTitle().equals(android.os.Build.MODEL)){
                    return -1;
                }else if(rhs.getTitle().equals(android.os.Build.MODEL)){
                    return 1;
                }else if(lhs.getTitle().equals(android.os.Build.MODEL) && !rhs.getPackageName().equals(SystemProperties.get("ro.sys.default.lockscreen", ""))){
                    return 1;
                }else if(rhs.getTitle().equals(android.os.Build.MODEL) && !lhs.getPackageName().equals(SystemProperties.get("ro.sys.default.lockscreen", ""))){
                    return -1;
                }
                return 0;
            }

        });
        int i = 0;
        for (LockscreenInfo lockscreenInfo : mLockscreenPackages) {
            mPackageNames[i] = lockscreenInfo.getPackageName();
            mPackagePaths[i] = lockscreenInfo.getPackagePath();
            mTitles[i] = lockscreenInfo.getTitle();
            i++;
        }

        return mLockscreenPackages;
    }

    public void onApply(int pos) {
        if(mLockscreenPackages.size()!=0){
            if(mLockscreenPackage.equals(mLockscreenPackages.get(pos).getPackageName())){
                Toast.makeText(getActivity(), R.string.in_use, Toast.LENGTH_SHORT).show();
            }else{
                LockscreenInfo lockscreenInfo = mLockscreenPackages.get(pos);
                /*
                 *modify by huangyiquan,set the custom lockscreen wallpaper such as cd lcoskreen, 
                 * when there is no pic named "default_wallpaper_lockscreen"
                 */
                String lockscreenPackage = lockscreenInfo.getPackageName();
                Log.w("yzy", "yangzy"+"mPackageName"+lockscreenInfo.getPackageName());
                Log.w("yzy", "yangzy"+"mLocalFilePath"+lockscreenInfo.getPackagePath());
                Settings.System.putString(getActivity().getContentResolver(),
                        LOCKSCREEN_PACKAGE, lockscreenPackage);
                //Log.i("huangyiquan1", "lockscreenPackage = " + lockscreenPackage);
                mLockscreenPackage = lockscreenPackage;
                new ApplyLockscreenTask(getActivity()).executeOnExecutor(MainActivity.fixedThreadPool,lockscreenInfo);
            }
        }
        

    }

    class ApplyLockscreenTask extends AsyncTask<LockscreenInfo, String, String>{  
        boolean mCanceled = false;  
        ProgressDialog dialog;  
        String message;
        boolean uxLockscreen = false;

        public ApplyLockscreenTask(Context context){  
            message =context.getString(R.string.apply);
            dialog = new ProgressDialog(context);  
            dialog.setMessage(message + "...");
            dialog.setCancelable(false);  
            dialog.show();  
        }  

        @Override  
        protected String doInBackground(LockscreenInfo... pramas) {  
            LockscreenInfo lockscreenInfo = pramas[0];  
            publishProgress(lockscreenInfo.getTitle());

            // extract ux
            String uxPath = mFunUXDir.getAbsolutePath();
            String uxName = FUN_UX_DEFAULT_NAME;
            uxLockscreen = extractUXObject(getActivity(), lockscreenInfo.getPackageName(), FUN_UX_ASSET_NAME, uxPath, uxName);

            // wallpaper
            Bitmap bitmap = lockscreenInfo.getLockscreenWallpaper();

            if (bitmap !=null)
            {
                try {
                    WallpaperManager mWallpaperManager = WallpaperManager
                            .getInstance(getActivity());

                    mWallpaperManager.setLockscreenBitmap(bitmap);
                } catch (Exception e) {
                    // TODO: handle exception
                }
            }else{
                mHandler.sendEmptyMessage(MSG_LCOKSREEN_WALLPAPER_CHANGE);

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

            mAdapter.notifyDataSetInvalidated();
//            locknow();
        }  
    } 

    private void locknow() {
        /*IPowerManager mIPowerManager = IPowerManager.Stub
                .asInterface(ServiceManager.getService(Context.POWER_SERVICE));
        try {
            mIPowerManager.goToSleep(SystemClock.uptimeMillis(), 0);
        } catch (RemoteException localRemoteException) {
            Log.w(TAG, localRemoteException.toString());
        }*/
        
        PowerManager pm = (PowerManager)getActivity().getSystemService(Context.POWER_SERVICE);  
        pm.goToSleep(SystemClock.uptimeMillis()); 
    }

    public class LockscreenPackageAdapter extends ArrayAdapter<LockscreenInfo> {
        private LayoutInflater mInflater;
        private Context mContext;

        public LockscreenPackageAdapter(LayoutInflater inflater, Context context) {
            super(context, 0);
            mInflater = inflater;
            mContext = context;
        }

        public void setData(List<LockscreenInfo> data) {
            clear();
            if (data != null) {
                addAll(data);
            }
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.grid_view_item_theme, null);
                holder = new ViewHolder();
                holder.icon = (ImageView) convertView.findViewById(R.id.icon);
                holder.status = (ImageView) convertView
                        .findViewById(R.id.status);
                holder.text = (TextView) convertView.findViewById(R.id.text);
                holder.type = (ImageView) convertView.findViewById(R.id.type);
                holder.apply = (Button) convertView.findViewById(R.id.apply);
                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            LockscreenInfo lockscreenInfo = getItem(position);

            //holder.icon.setImageDrawable(getLockscreenPreview(lockscreenInfo));
            if(lockscreenInfo.getPreviewThumb()==null){
                mAsyncImageCache.displayImage(holder.icon, R.drawable.theme_default,
                        new AsyncImageCache.GeneralImageGenerator(lockscreenInfo.getPackagePath()+ "_preview_thumb", BitmapFactory.decodeResource(mContext.getResources(), R.drawable.theme_default)));
            }else{
                mAsyncImageCache.displayImage(holder.icon, R.drawable.theme_default,
                        new AsyncImageCache.GeneralImageGenerator(lockscreenInfo.getPackagePath() + "_preview_thumb", lockscreenInfo.getPreviewThumb().getBitmap()));
            }
            holder.type.setVisibility(View.GONE);
            holder.status.setImageResource(R.drawable.status_using);
            if (mLockscreenPackage.equals(lockscreenInfo.getPackageName())) {
                holder.status.setVisibility(View.VISIBLE);
            } else {
                holder.status.setVisibility(View.INVISIBLE);
            }
            holder.text.setText(lockscreenInfo.getTitle());
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
            RelativeLayout deleteView;
        }
    }

    class LoadLockscreenPackageTask extends AsyncTask<Void, Void, List<LockscreenInfo>>{  
        public LoadLockscreenPackageTask(Context context){  
        }  

        @Override  
        protected List<LockscreenInfo> doInBackground(Void... pramas) {  

            return IndividualLockScreenFragment.this.loadLockscreenPackages();  
        }  

        @Override
        protected void onPostExecute(List<LockscreenInfo> result){  
            mAdapter.setData(result);
            mAdapter.notifyDataSetChanged();
        }  
    }

    private boolean extractUXObject(Context context, String packageName, String assetName,
            String uxPath, String uxName) {

        ContentResolver resolver = context.getContentResolver();
        Settings.System.putString(resolver, KEY_THEME_LOCKSCREEN_FUN_UX_VALUE, "");

        initFunUXDir(); 

        String fileName = uxPath + File.separator + uxName;

        try {
            Context mPackageContext = context.createPackageContext(packageName,
                    Context.CONTEXT_IGNORE_SECURITY);
            InputStream is = mPackageContext.getResources().getAssets()
                    .open(assetName);
            FileOutputStream fos = new FileOutputStream(fileName);
            byte[] buffer = new byte[10240];
            int count = 0;
            while ((count = is.read(buffer)) > 0) {
                fos.write(buffer, 0, count);
            }
            fos.close();
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        chmodFileAccess(fileName);

        return true;
    }

    private void chmodFileAccess(String filePath) {
        FileUtils.setPermissions(
                filePath,
                FileUtils.S_IRWXU|FileUtils.S_IRWXG|FileUtils.S_IRWXO,
                -1, -1);
    }
}
