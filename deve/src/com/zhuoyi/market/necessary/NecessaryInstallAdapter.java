package com.zhuoyi.market.necessary;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.market.download.util.Util;
import com.market.net.data.AppInfoBto;
import com.market.net.data.CornerIconInfoBto;
import com.zhuoyi.market.R;
import com.zhuoyi.market.commonInterface.DownloadCallBackInterface;
import com.zhuoyi.market.utils.AppOperatorUtils;
import com.zhuoyi.market.utils.AsyncImageCache;
import com.zhuoyi.market.utils.MarketUtils;

/**
 * {必备相关适配器}
 * 
 * @author pc
 */
public class NecessaryInstallAdapter extends BaseExpandableListAdapter {

    private static final int OFFICAL_VISIBLE = 1;
    private LayoutInflater inflater;
    private Context context;
    private boolean mIsRefrashing = false;
    private AsyncImageCache mAsyncImageCache;
    private int defaultImageId; // 默认图片
    private String mReportFlag = "";
    private String mCountM = "";
    private String mDownloadStr = "";
    private Map<String, List<AppInfoBto>> list;
    private ArrayList<String> groupList;
    private ExpandableListView mInstallAppListview;
    private WeakReference<DownloadCallBackInterface> mDownloadCallBack;


    public NecessaryInstallAdapter(Context context, ArrayList<String> groupList, Map<String, List<AppInfoBto>> list,
        ExpandableListView mInstallAppListview, DownloadCallBackInterface callback) {
        this.context = context;
        this.mDownloadCallBack = new WeakReference<DownloadCallBackInterface>(callback);
        this.list = list;
        this.groupList = groupList;
        this.mInstallAppListview = mInstallAppListview;
        inflater = LayoutInflater.from(context);
        mAsyncImageCache = AsyncImageCache.from(context);
        defaultImageId = R.drawable.picture_bg1_big;
        mCountM = context.getResources().getString(R.string.ten_thousand);
        mDownloadStr = context.getResources().getString(R.string.download_str);
    }


    public void setInstallList(Map<String, List<AppInfoBto>> list, ArrayList<String> groupList) {
        this.list = list;
        this.groupList = groupList;
    }


    public void freeImageCache() {
        if (mAsyncImageCache != null)
            mAsyncImageCache.stop();
        if (list != null)
            list.clear();
        if (groupList != null) {
            groupList.clear();
        }
    }


    public void setReportFlag(String flag) {
        mReportFlag = flag;
    }

    private int mTopicId = -1;


    public void setTopicId(int topicId) {
        mTopicId = topicId;
    }


    @Override
    public int getGroupCount() {
        return groupList.size();
    }


    @Override
    public int getChildrenCount(int groupPosition) {
        return list.get(groupList.get(groupPosition)).size();
    }


    @Override
    public Object getGroup(int groupPosition) {
        return null;
    }


    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return null;
    }


    @Override
    public long getGroupId(int groupPosition) {
        return 0;
    }


    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 0;
    }


    @Override
    public boolean hasStableIds() {
        return false;
    }


    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        GroupHolder groupHolder = null;
        if (convertView == null) {
            groupHolder = new GroupHolder();
            convertView = inflater.inflate(R.layout.necessary_group_item, null);
            groupHolder.group_title = (TextView) convertView.findViewById(R.id.group_title);
            convertView.setTag(groupHolder);
        } else {
            groupHolder = (GroupHolder) convertView.getTag();
        }
        groupHolder.group_title.setText(groupList.get(groupPosition));
        mInstallAppListview.expandGroup(groupPosition);
        return convertView;
    }


    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView,
        ViewGroup parent) {
        String packageName = "";
        String versionCode = "";
        String imageUrl = "";
        ChildHolder childHolder = null;
        if (mIsRefrashing)
            return null;
        else mIsRefrashing = true;
        try {
            if (convertView == null) {
                childHolder = new ChildHolder();
                convertView = inflater.inflate(Util.getWaterFlowLayoutId(), null);
                childHolder.rlParent = (RelativeLayout) convertView.findViewById(R.id.rlParent);
                childHolder.app_icon = (ImageView) convertView.findViewById(R.id.app_icon_img);
                childHolder.app_name = (TextView) convertView.findViewById(R.id.app_name_txt);
                childHolder.app_state = (TextView) convertView.findViewById(R.id.state_app_btn);
                childHolder.app_size = (TextView) convertView.findViewById(R.id.app_size_text);
                childHolder.download_time = (TextView) convertView.findViewById(R.id.download_times_txt);
                childHolder.text_sort = (TextView) convertView.findViewById(R.id.text_sort);
                childHolder.app_desc = (TextView) convertView.findViewById(R.id.app_desc);
                childHolder.appRatingStar = (RatingBar) convertView.findViewById(R.id.app_ratingview);
                childHolder.officialIcon = (ImageView) convertView.findViewById(R.id.official_icon);
                childHolder.cornerIcon = (ImageView) convertView.findViewById(R.id.corner_icon);
                convertView.setTag(childHolder);
            } else {
                childHolder = (ChildHolder) convertView.getTag();
            }
            childHolder.text_sort.setVisibility(View.GONE);
            final AppInfoBto appInfo = list.get(groupList.get(groupPosition)).get(childPosition);
            if (appInfo != null) {
                packageName = appInfo.getPackageName();
                versionCode = String.valueOf(appInfo.getVersionCode());
                imageUrl = appInfo.getImgUrl();
                mAsyncImageCache.displayImage(true, childHolder.app_icon, defaultImageId, new AsyncImageCache.NetworkImageGenerator(appInfo.getPackageName(), imageUrl), true);

                childHolder.app_name.setText(appInfo.getName());
                childHolder.app_size.setText(appInfo.getFileSizeString());
                childHolder.download_time.setText(setDownString(appInfo.getDownTimes()));
                childHolder.appRatingStar.setRating(appInfo.getStars());
                childHolder.app_desc.setText(appInfo.getBriefDesc());
                if (TextUtils.isEmpty(appInfo.getBriefDesc())) {
                    if (childHolder.app_desc.getVisibility() == View.VISIBLE)
                        childHolder.app_desc.setVisibility(View.GONE);
                } else {
                    if (childHolder.app_desc.getVisibility() == View.GONE)
                        childHolder.app_desc.setVisibility(View.VISIBLE);
                    childHolder.app_desc.setText(appInfo.getBriefDesc());
                }
                
                AppOperatorUtils.initBtnState(context, childHolder.app_state, packageName, appInfo.getVersionCode(), childHolder.app_icon);
                
                childHolder.app_state.setTag(R.id.water_flow_tag, true);
                childHolder.rlParent.setTag(R.id.water_flow_tag, true);
                childHolder.rlParent.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MarketUtils.startAppDetailActivity(v.getContext(), appInfo, mReportFlag, mTopicId);
                    }

                });
                
                childHolder.app_state.setOnClickListener(new AppOperatorUtils.CommonAppClick(context, appInfo, mDownloadCallBack, Integer.toString(mTopicId), mReportFlag,false));
                
                mIsRefrashing = false;
                //动态添加官方标识到 应用名后
                if(appInfo.getOfficialLogo() == OFFICAL_VISIBLE) {
                    childHolder.officialIcon.setVisibility(View.VISIBLE);
                    childHolder.officialIcon.setImageResource(R.drawable.official);
                } else {
                    childHolder.officialIcon.setVisibility(View.GONE);
                    childHolder.officialIcon.setImageResource(0);
                }
                
                CornerIconInfoBto cornerIconInfo = appInfo.getCornerMarkInfo();
                if(cornerIconInfo!=null && cornerIconInfo.getType() > 0) {
                    childHolder.cornerIcon.setVisibility(View.VISIBLE);
                    AsyncImageCache.from(context).displayImage(childHolder.cornerIcon, cornerIconInfo, 1);
                } else {
                    if(childHolder.cornerIcon.getVisibility() != View.GONE)
                        childHolder.cornerIcon.setVisibility(View.GONE);
                }
            }
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        } catch (Exception e) {
            mIsRefrashing = false;
            e.printStackTrace();
        }

        return convertView;
    }


    private String setDownString(long count) {
        StringBuilder count_string = new StringBuilder();
        if (count < 100000) {
            count_string.append(count);
        } else {
            if (count > 600000)
                count_string.append(">100" + mCountM);
            else if (count > 500000)
                count_string.append(">50" + mCountM);
            else if (count > 300000)
                count_string.append(">30" + mCountM);
            else if (count > 200000)
                count_string.append(">20" + mCountM);
            else count_string.append(">10" + mCountM);
        }
        return count_string.append(mDownloadStr).toString();
    }


    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    class GroupHolder {
        TextView group_title;
    }

    class ChildHolder {
        RelativeLayout rlParent;
        ImageView app_icon;
        TextView app_name;
        TextView download_time;
        TextView app_size;
        TextView app_state;
        TextView text_sort;
        TextView app_desc;
        RatingBar appRatingStar;
        ImageView officialIcon;
        ImageView cornerIcon;
        
    }

}
