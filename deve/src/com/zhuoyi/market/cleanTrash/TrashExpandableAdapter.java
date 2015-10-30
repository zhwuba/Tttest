package com.zhuoyi.market.cleanTrash;

import com.zhuoyi.market.R;
import com.zhuoyi.market.cleanTrash.TrashControl.ApkTrash;
import com.zhuoyi.market.cleanTrash.TrashControl.AppCache;
import com.zhuoyi.market.utils.AsyncImageCache;

import android.content.Context;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TrashExpandableAdapter extends BaseExpandableListAdapter {
    private static final String TAG = "TrashExpandableAdapter";
    
    private static final int GROUP_TYPE_SYSTEM = 0;
    private static final int GROUP_TYPE_APP_CACHE = GROUP_TYPE_SYSTEM + 1;
    private static final int GROUP_TYPE_APK_TRASH = GROUP_TYPE_APP_CACHE + 1;
    private static final int GROUP_NUM = GROUP_TYPE_APK_TRASH + 1;
    
    private Context mContext;
    private LayoutInflater mInflater;
    private TrashControl mTrashControl;
    private PackageManager mPkgManager;
    private AsyncImageCache mAsyncImageCache;
    
    private View.OnClickListener mSystemAllSelectListener;
    private View.OnClickListener mAppCacheAllSelectListener;
    private View.OnClickListener mApkTrashAllSelectListener;
    
    public TrashExpandableAdapter(Context context, TrashControl trashControl) {
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
        mTrashControl = trashControl;
        mPkgManager = mContext.getPackageManager();
        mAsyncImageCache = AsyncImageCache.from(context);
        
        mSystemAllSelectListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTrashControl.changeSystemCacheSelect();
                notifyDataSetChanged();
            }
        };
        
        mAppCacheAllSelectListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTrashControl.changeAllAppCacheSelect();
                notifyDataSetChanged();
            }
        };
        
        mApkTrashAllSelectListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTrashControl.changeAllApkTrashSelect();
                notifyDataSetChanged();
            }
        };
    }
    
    
    @Override
    public int getGroupCount() {
        return GROUP_NUM;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        if (mTrashControl.isCheckingTrashStatusNow()) {
            return 0;
        }
        
        switch(groupPosition) {
        case GROUP_TYPE_SYSTEM:
            return 0;
        case GROUP_TYPE_APP_CACHE:
            return mTrashControl.getAppCacheInfoList().size();
        case GROUP_TYPE_APK_TRASH:
            return mTrashControl.getApkDirsList().size();
        }
        
        return 0;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return null;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        try {
            switch(groupPosition) {
            case GROUP_TYPE_APP_CACHE:
                return mTrashControl.getAppCacheInfoList().get(childPosition);
            case GROUP_TYPE_APK_TRASH:
                return mTrashControl.getApkDirsList().get(childPosition);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return null;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        GroupHolder groupHolder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.trash_group_item, null);
            groupHolder = new GroupHolder();
            groupHolder.titleTv = (TextView) convertView.findViewById(R.id.title_tv);
            groupHolder.arrowIv = (ImageView) convertView.findViewById(R.id.arrow_iv);
            groupHolder.desTv = (TextView) convertView.findViewById(R.id.trash_des_tv);
            groupHolder.checkBoxTv = (TextView) convertView.findViewById(R.id.trash_check_tv);
            groupHolder.selectListenLl = (LinearLayout) convertView.findViewById(R.id.select_listen_ll);
            groupHolder.desAdjustV = convertView.findViewById(R.id.des_tv_adjust_view);
            convertView.setTag(groupHolder);
            
        } else {
            groupHolder = (GroupHolder) convertView.getTag();
        }
        
        setGroupHolderData(groupHolder, isExpanded, groupPosition);
        
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ItemHolder itemHolder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.trash_child_item, null);
            itemHolder = new ItemHolder();
            itemHolder.wholeLayout = (LinearLayout) convertView.findViewById(R.id.child_item_layout);
            itemHolder.appIconIv = (ImageView) convertView.findViewById(R.id.app_icon_iv);
            itemHolder.appNameTv = (TextView) convertView.findViewById(R.id.app_name_tv);
            itemHolder.trashSizeTv = (TextView) convertView.findViewById(R.id.trash_size_tv);
            itemHolder.checkBoxTv = (TextView) convertView.findViewById(R.id.check_box_tv);
            itemHolder.selectListenLl = (LinearLayout) convertView.findViewById(R.id.select_listen_ll);
            convertView.setTag(itemHolder);
            
        } else {
            itemHolder = (ItemHolder) convertView.getTag();
        }
        
        setItemHolderData(itemHolder, childPosition, groupPosition);
        
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        // TODO Auto-generated method stub
        return false;
    }

    
    
    class GroupHolder {
        public TextView titleTv;
        public ImageView arrowIv;
        public TextView desTv;
        public TextView checkBoxTv;
        public LinearLayout selectListenLl;
        public View desAdjustV;
    }
    
    private void setGroupHolderData(GroupHolder groupHolder, boolean isExpanded, int groupPosition) {
        switch(groupPosition) {
        case GROUP_TYPE_SYSTEM:
            groupHolder.arrowIv.setVisibility(View.INVISIBLE);
            groupHolder.titleTv.setText(R.string.trash_system_list_title);
            if (mTrashControl.isCheckingTrashStatusNow()) {
                groupHolder.selectListenLl.setVisibility(View.GONE);
                groupHolder.desAdjustV.setVisibility(View.VISIBLE);
                groupHolder.desTv.setText(R.string.trash_checking);
            } else if (mTrashControl.getSysTotalTrashSize() == 0) {
                groupHolder.selectListenLl.setVisibility(View.GONE);
                groupHolder.desAdjustV.setVisibility(View.VISIBLE);
                groupHolder.desTv.setText(R.string.trash_nothing_clean);
            } else {
                groupHolder.selectListenLl.setVisibility(View.VISIBLE);
                groupHolder.desAdjustV.setVisibility(View.GONE);
                groupHolder.desTv.setText(mTrashControl.getDisplaySizeText(mTrashControl.getSysTotalTrashSize()));
                if (mTrashControl.getSelectedSysTrashSize() == 0) {
                    groupHolder.checkBoxTv.setBackgroundResource(R.drawable.trash_selected_no);
                } else {
                    groupHolder.checkBoxTv.setBackgroundResource(R.drawable.trash_selected_all);
                }
            }
            groupHolder.selectListenLl.setOnClickListener(mSystemAllSelectListener);
            break;
            
        case GROUP_TYPE_APP_CACHE:
            groupHolder.titleTv.setText(R.string.trash_app_cache_list_title);
            if (isExpanded) {
                groupHolder.arrowIv.setBackgroundResource(R.drawable.trash_arrow_up);
            } else {
                groupHolder.arrowIv.setBackgroundResource(R.drawable.trash_arrow_down);
            }
            long totalCacheSize = mTrashControl.getTotalCacheSize();
            if (mTrashControl.isCheckingTrashStatusNow() ) {
                groupHolder.arrowIv.setVisibility(View.INVISIBLE);
                groupHolder.selectListenLl.setVisibility(View.GONE);
                groupHolder.desAdjustV.setVisibility(View.VISIBLE);
                groupHolder.desTv.setText(R.string.trash_checking);
            } else if (totalCacheSize == 0) {
                groupHolder.arrowIv.setVisibility(View.INVISIBLE);
                groupHolder.selectListenLl.setVisibility(View.GONE);
                groupHolder.desAdjustV.setVisibility(View.VISIBLE);
                groupHolder.desTv.setText(R.string.trash_nothing_clean);
            } else {
                groupHolder.arrowIv.setVisibility(View.VISIBLE);
                groupHolder.selectListenLl.setVisibility(View.VISIBLE);
                groupHolder.desAdjustV.setVisibility(View.GONE);
                groupHolder.desTv.setText(mTrashControl.getDisplaySizeText(totalCacheSize));
                long selectedCacheSize = mTrashControl.getSelectedTrashCacheSize();
                if (selectedCacheSize == 0) {
                    groupHolder.checkBoxTv.setBackgroundResource(R.drawable.trash_selected_no);
                } else if (selectedCacheSize == totalCacheSize) {
                    groupHolder.checkBoxTv.setBackgroundResource(R.drawable.trash_selected_all);
                } else {
                    groupHolder.checkBoxTv.setBackgroundResource(R.drawable.trash_selected_not_all);
                }
            }
            groupHolder.selectListenLl.setOnClickListener(mAppCacheAllSelectListener);
            break;
            
        case GROUP_TYPE_APK_TRASH:
            groupHolder.titleTv.setText(R.string.trash_apk_list_title);
            if (isExpanded) {
                groupHolder.arrowIv.setBackgroundResource(R.drawable.trash_arrow_up);
            } else {
                groupHolder.arrowIv.setBackgroundResource(R.drawable.trash_arrow_down);
            }
            long totalTrashApkSize = mTrashControl.getTotalTrashApkSize();
            if (mTrashControl.isCheckingTrashStatusNow() ) {
                groupHolder.arrowIv.setVisibility(View.INVISIBLE);
                groupHolder.selectListenLl.setVisibility(View.GONE);
                groupHolder.desAdjustV.setVisibility(View.VISIBLE);
                groupHolder.desTv.setText(R.string.trash_checking);
            } else if (totalTrashApkSize == 0) {
                groupHolder.arrowIv.setVisibility(View.INVISIBLE);
                groupHolder.selectListenLl.setVisibility(View.GONE);
                groupHolder.desAdjustV.setVisibility(View.VISIBLE);
                groupHolder.desTv.setText(R.string.trash_nothing_clean);
            } else {
                groupHolder.arrowIv.setVisibility(View.VISIBLE);
                groupHolder.selectListenLl.setVisibility(View.VISIBLE);
                groupHolder.desAdjustV.setVisibility(View.GONE);
                groupHolder.desTv.setText(mTrashControl.getDisplaySizeText(totalTrashApkSize));
                long selectedApkSize = mTrashControl.getSelectedTrashApkSize();
                if (selectedApkSize == 0) {
                    groupHolder.checkBoxTv.setBackgroundResource(R.drawable.trash_selected_no);
                } else if (selectedApkSize == totalTrashApkSize) {
                    groupHolder.checkBoxTv.setBackgroundResource(R.drawable.trash_selected_all);
                } else {
                    groupHolder.checkBoxTv.setBackgroundResource(R.drawable.trash_selected_not_all);
                }
            }
            groupHolder.selectListenLl.setOnClickListener(mApkTrashAllSelectListener);
            break;
        }
        
    }
    
    
    
    class ItemHolder {
        public LinearLayout wholeLayout;
        public ImageView appIconIv;
        public TextView appNameTv;
        public TextView trashSizeTv;
        public TextView checkBoxTv;
        public LinearLayout selectListenLl;
    }
    
    
    private void setItemHolderData(ItemHolder itemHolder, int childPosition, int groupPosition) {
        Object childObject = this.getChild(groupPosition, childPosition);
        if (childObject == null) {
            itemHolder.wholeLayout.setVisibility(View.GONE);
            return;
        } else {
            itemHolder.wholeLayout.setVisibility(View.VISIBLE);
        }
        switch(groupPosition) {
        case GROUP_TYPE_APP_CACHE:
            AppCache appCache = (AppCache)childObject;
            itemHolder.appNameTv.setText(appCache.appName);
            itemHolder.trashSizeTv.setText(mTrashControl.getDisplaySizeText(appCache.cacheSize));
            if (appCache.selected) {
                itemHolder.checkBoxTv.setBackgroundResource(R.drawable.trash_selected_all);
            } else {
                itemHolder.checkBoxTv.setBackgroundResource(R.drawable.trash_selected_no);
            }
            itemHolder.selectListenLl.setOnClickListener(new CacheCheckBoxListener(appCache.pkgName));
            
            mAsyncImageCache.displayImage(true,
                                          itemHolder.appIconIv,
                                          R.drawable.picture_bg1_big,
                                          new AsyncImageCache.UpdateAppImageGenerator(appCache.pkgName, mPkgManager),
                                          true);
            break;
            
        case GROUP_TYPE_APK_TRASH:
            ApkTrash apkTrash = (ApkTrash)childObject;
            itemHolder.appNameTv.setText(apkTrash.appName);
            itemHolder.trashSizeTv.setText(mTrashControl.getDisplaySizeText(apkTrash.trashSize));
            if (apkTrash.selected) {
                itemHolder.checkBoxTv.setBackgroundResource(R.drawable.trash_selected_all);
            } else {
                itemHolder.checkBoxTv.setBackgroundResource(R.drawable.trash_selected_no);
            }
            itemHolder.selectListenLl.setOnClickListener(new ApkDirCheckBoxListener(apkTrash.pkgName));
            
            mAsyncImageCache.displayImage(true,
                                          itemHolder.appIconIv,
                                          R.drawable.picture_bg1_big,
                                          new AsyncImageCache.UpdateAppImageGenerator(apkTrash.pkgName, mPkgManager),
                                          true);
            break;
        }
    }
    
    
    @Override
    public void onGroupCollapsed(int groupPosition) {
        // TODO Auto-generated method stub
        super.onGroupCollapsed(groupPosition);
    }


    @Override
    public void onGroupExpanded(int groupPosition) {
        // TODO Auto-generated method stub
        super.onGroupExpanded(groupPosition);
    }

    
    class ApkDirCheckBoxListener implements View.OnClickListener {
        
        private String pkgName;
        
        ApkDirCheckBoxListener(String packageName) {
            pkgName = packageName;
        }

        @Override
        public void onClick(View v) {
            mTrashControl.changeApkTrashSelect(pkgName);
            notifyDataSetChanged();
        }
    }
    
    
    class CacheCheckBoxListener implements View.OnClickListener {
        
        private String pkgName;
        
        CacheCheckBoxListener(String packageName) {
            pkgName = packageName;
        }
        
        @Override
        public void onClick(View v) {
            mTrashControl.changeAppCacheSelect(pkgName);
            notifyDataSetChanged();
        }
        
    }
}
