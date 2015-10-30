package com.zhuoyi.market.setting;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.market.account.constant.Constant;
import com.market.download.common.DownloadSettings;
import com.market.download.userDownload.DownloadManager;
import com.market.view.MultiButton;
import com.market.view.MultiButton.OnMultiBtnClickListener;
import com.market.view.SingleButton;
import com.market.view.SingleButton.OnSingleBtnClickListener;
import com.zhuoyi.market.R;
import com.zhuoyi.market.appResident.SettingData;
import com.zhuoyi.market.utils.MarketUtils;

public class SettingExpandableAdapter extends BaseExpandableListAdapter {
	
	private Context mContext = null;
	private ExpandableListView mExpandableListView;
	private List<Map<String, Object>> groupData = new ArrayList<Map<String,Object>>();
	private List<List<Map<String, Object>>> childData = new ArrayList<List<Map<String,Object>>>();
	
	public final static String SETTING_ITEM_TYPE               = "item_type";          //item类型
	public final static String SETTING_ITEM_TYPE_TEXT          = "item_type_text";     //纯文本
	public final static String SETTING_ITEM_TYPE_BTN           = "item_type_btn";      //单个按钮
	public final static String SETTING_ITEM_TYPE_MULTI_BTN     = "item_type_multi_btn";//三个按钮
	public final static String SETTING_ITEM_TYPE_IMAGE         = "item_type_image";    //单个图片
	
	public final static String SETTING_ITEM_UPDATE_SHOW        = "item_update_show";   //自更新红点
	
	public final static String SETTING_ITEM_NAME               = "item_name";          //item主名字
	public final static String SETTING_ITEM_NAME_SMALL         = "item_name_small";    //item解释
	public final static String SETTING_ITEM_NAME_TEXT          = "item_name_text";     //item右侧text
	public final static String SETTING_ITEM_IMAGE_ID           = "item_image_id";      //image id
	
	public final static String SETTING_BTN_FLAG                = "btn_flag";           //单个按钮标识
	public final static String SETTING_BTN_FLAG_SAVE           = "btn_flag_save";      //只能无图
	public final static String SETTING_BTN_FLAG_UPDATE         = "btn_flag_update";    //零流量更新
	public final static String SETTING_BTN_FLAG_DELAY          = "btn_flag_delay";     //稍后下载
	public final static String SETTING_BTN_FLAG_PUSH           = "btn_flag_push";      //允许推送更新提醒
	public final static String SETTING_BTN_FLAG_DEL            = "btn_flag_del";       //安装后自动删除安装包
	
	public SettingExpandableAdapter(Context context, List<List<Map<String, Object>>> childData, List<Map<String, Object>> groupData, ExpandableListView mExpandableListView) {
		this.mContext = context;
		this.childData = childData;
		this.groupData = groupData;
		this.mExpandableListView = mExpandableListView;
	}

	@Override
	public int getGroupCount() {
		return groupData.size();
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return childData.get(groupPosition).size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		
		return groupData.get(groupPosition);
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return childData.get(groupPosition).get(childPosition);
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
		return false;
	}
	
    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		WrapperGroup wrapperGroup = null;
		
		if (convertView != null && convertView.getTag() instanceof WrapperGroup) {
			wrapperGroup = (WrapperGroup) convertView.getTag();
		} else {
			convertView = LayoutInflater.from(mContext).inflate(R.layout.layout_setting_parent_item, parent, false);
			wrapperGroup = new WrapperGroup(convertView);
			convertView.setTag(wrapperGroup);
		}
		
		TextView tv = wrapperGroup.getTextview();
		tv.setText((String)groupData.get(groupPosition).get("classify"));
		mExpandableListView.expandGroup(groupPosition);//设置展开
		return convertView;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
		WrapperChild wrapperChild = null;
		if(convertView == null) {
		    convertView = LayoutInflater.from(mContext).inflate(R.layout.layout_setting_item, parent, false);
			wrapperChild = new WrapperChild(convertView);
			convertView.setTag(wrapperChild);
		} else {
		    if (convertView.getTag() instanceof WrapperChild) {
		        wrapperChild = (WrapperChild) convertView.getTag();
			} else {
			    convertView = LayoutInflater.from(mContext).inflate(R.layout.layout_setting_item, parent, false);
	            wrapperChild = new WrapperChild(convertView);
	            convertView.setTag(wrapperChild);
			}
		}
		
        TextView leftName = wrapperChild.getLeftName();
        TextView leftText = wrapperChild.getLeftText();
        ImageView update = wrapperChild.getUpdate();
        View divider = wrapperChild.getDivider();
        
		MultiButton multiBtn = wrapperChild.getRightMultiButton();
		SingleButton rightBtn = wrapperChild.getRightBtn();
		TextView rightText = wrapperChild.getRightText();
		ImageView rightImage = wrapperChild.getRightImage();

		String type = (String) childData.get(groupPosition).get(childPosition).get(SETTING_ITEM_TYPE);
		String smallName = (String) childData.get(groupPosition).get(childPosition).get(SETTING_ITEM_NAME_SMALL);
		
		//该条item名字
		leftName.setText((String)childData.get(groupPosition).get(childPosition).get(SETTING_ITEM_NAME));
		
		//该条item功能简介
		if (!TextUtils.isEmpty(smallName)) {
		    leftText.setVisibility(View.VISIBLE);
		    leftText.setText(smallName);
		} else {
		    leftText.setVisibility(View.GONE);
		}
		
		//版本更新
		if(childData.get(groupPosition).get(childPosition).get(SETTING_ITEM_UPDATE_SHOW) != null){
		    update.setVisibility(View.VISIBLE);
		} else 
		    update.setVisibility(View.GONE);
		
		//右侧是image
		if (SETTING_ITEM_TYPE_IMAGE.equals(type)) {
		    rightImage.setBackgroundResource((Integer) childData.get(groupPosition).get(childPosition).get(SETTING_ITEM_IMAGE_ID));
		    rightImage.setVisibility(View.VISIBLE);
		} else {
		    rightImage.setVisibility(View.GONE);
		}
		
		//右侧是text
        if (SETTING_ITEM_TYPE_TEXT.equals(type)) {
            rightText.setVisibility(View.VISIBLE);
            rightText.setText((String) childData.get(groupPosition).get(childPosition).get(SETTING_ITEM_NAME_TEXT));
        } else {
            rightText.setVisibility(View.GONE);
        }
		
		//右侧单个button
		if(SETTING_ITEM_TYPE_BTN.equals(type)){
		    String btnType = (String) childData.get(groupPosition).get(childPosition).get(SETTING_BTN_FLAG);
		    rightBtn.setVisibility(View.VISIBLE);
		    rightBtn.setBtnIdentification(btnType);
		    rightBtn.setSingleBtnState(getInitState(btnType));
		    rightBtn.setOnSingleBtnClickListener(new OnSingleBtnClickListener() {

                @Override
                public void onBtnClick(View view, String identification, int state) {
                    // TODO Auto-generated method stub
                    doDiffThingForSingleBtn(identification, state);
                }
		        
		    });
		} else {
		    rightBtn.setVisibility(View.GONE);
		}
		
		//右侧三个button
		if(SETTING_ITEM_TYPE_MULTI_BTN.equals(type)){
			multiBtn.setVisibility(View.VISIBLE);
			multiBtn.setBtnName("1", "2", "3");
			multiBtn.setInitBtn(SettingData.mDownloadMaxNum - 1);
			multiBtn.setOnMultiBtnClickListener(new OnMultiBtnClickListener() {

                @Override
                public void onBtnClick(View view, int curBtn) {
                    // TODO Auto-generated method stub
                    try {
                        if (DownloadManager.getDownloadingNumber() > 0) {
                            ((MultiButton)view).setInitBtn(SettingData.mDownloadMaxNum - 1);
                            Toast.makeText(mContext, mContext.getString(R.string.set_download_tasks_max_count_warning), Toast.LENGTH_SHORT).show();
                        } else {
                            SettingData.setDownloadMaxNum(mContext, curBtn + 1);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
			    
			});
		} else {
		    multiBtn.setVisibility(View.GONE);
		}
		
		//item之间分界线
		if(isLastChild) {
			divider.setVisibility(View.GONE);
		} else {
			divider.setVisibility(View.VISIBLE);
		}
		
		//item本身是否需要按下效果
		if(groupPosition == 0 || (groupPosition == 1 && childPosition < 4)) {
			convertView.setBackgroundColor(0xffffffff);
		} else {
			convertView.setBackgroundResource(R.drawable.sort_bg_select);
		}
		
		return convertView;
	}
	
	/**
	 * 初始化单个按钮状态
	 * @param identification
	 * @return 打开或关闭
	 */
    private int getInitState(String identification) {
        if (SETTING_BTN_FLAG_SAVE.equals(identification)) {
            if (SettingData.mNoShowImage) {
                return SingleButton.OPEN;
            } else {
                return SingleButton.CLOSE;
            }
        } else if (SETTING_BTN_FLAG_UPDATE.equals(identification)) {
            if (DownloadSettings.getUserUpdateAutoFlag(mContext)) {
                return SingleButton.OPEN;
            } else {
                return SingleButton.CLOSE;
            }
        } else if (SETTING_BTN_FLAG_PUSH.equals(identification)) {
            if (SettingData.mIsNotify) {
                return SingleButton.OPEN;
            } else {
                return SingleButton.CLOSE;
            }
        } else if (SETTING_BTN_FLAG_DEL.equals(identification)) {
            if (SettingData.mDeleteInstallPackage) {
                return SingleButton.OPEN;
            } else {
                return SingleButton.CLOSE;
            }
        } else {
            return SingleButton.CLOSE;
        }
    }
	
	/**
	 * 点击单个按钮之后做的事情
	 * @param identification
	 * @param state
	 */
	private void doDiffThingForSingleBtn(String identification, int state) {
	    if (SETTING_BTN_FLAG_SAVE.equals(identification)) {
	        if (state == SingleButton.OPEN) {
	            SettingData.setNoShowImage(mContext, true);
	            Constant.SHOW_IMAGE = true;
	            
	        } else {
	            SettingData.setNoShowImage(mContext, false);
	            Constant.SHOW_IMAGE = false;     //web页面同步设置
	        }
	        MarketUtils.setSaveModeTipCount(mContext, 0);
	    } else if (SETTING_BTN_FLAG_UPDATE.equals(identification)) {
	        if (state == SingleButton.OPEN) {
	            DownloadSettings.setUserUpdateAutoFlag(mContext, true);
	        } else {
	            DownloadSettings.setUserUpdateAutoFlag(mContext, false);
	        }
	    } else if (SETTING_BTN_FLAG_DELAY.equals(identification)) {
	        if (state == SingleButton.OPEN) {
	            SettingData.setDelayDownload(mContext, true);
	        } else {
	            SettingData.setDelayDownload(mContext, false);
	        }
	    } else if (SETTING_BTN_FLAG_PUSH.equals(identification)) {
	        if (state == SingleButton.OPEN) {
                SettingData.setIsNotify(mContext, true);
	        } else {
                SettingData.setIsNotify(mContext, false);
	        }
	    } else if (SETTING_BTN_FLAG_DEL.equals(identification)) {
	        if (state == SingleButton.OPEN) {
	            SettingData.setDeleteInstallPkg(mContext, true);
	        } else {
	            SettingData.setDeleteInstallPkg(mContext, false);
	        }
	    }
	}
	
	
    /**
     * 父item
     * @author dream.zhou
     *
     */
	private  class WrapperGroup {
		View view;
		TextView tv;
		
		public WrapperGroup(View view) {
			this.view = view;
		}
		
		public TextView getTextview() { 
			if(tv == null) 
				tv = (TextView) view.findViewById(R.id.textView1);
			return tv;
		}
		
	}
	
	
	/**
	 * 子item
	 * @author dream.zhou
	 * 
	 */
    private class WrapperChild {
        View parent = null;

        //左侧名字、简介、更显红点、item分割线
        TextView leftName = null;
        TextView leftText = null;
        ImageView update = null;
        View divider = null;
        
        //右侧类型：三个按钮、单个按钮、text、image
        MultiButton rightMultiBtn = null;
        SingleButton rightBtn = null;
        TextView rightText = null;
        ImageView rightImage = null;
        
        public WrapperChild(View view) {
            this.parent = view;
        }
        
        
        public ImageView getRightImage() {
            if (rightImage == null)
                rightImage = (ImageView) parent.findViewById(R.id.right_image);
            return rightImage;
        }


        public SingleButton getRightBtn() {
            if (rightBtn == null)
                rightBtn = (SingleButton) parent.findViewById(R.id.right_btn);
            return rightBtn;
        }


        public ImageView getUpdate() {
            if (update == null)
                update = (ImageView) parent.findViewById(R.id.left_update);
            return update;
        }


        public MultiButton getRightMultiButton() {
            if (rightMultiBtn == null)
                rightMultiBtn = (MultiButton) parent
                        .findViewById(R.id.right_multi_btn);
            return rightMultiBtn;
        }


        public TextView getLeftName() {
            if (leftName == null)
                leftName = (TextView) parent.findViewById(R.id.left_name);
            return leftName;
        }


        public TextView getLeftText() {
            if (leftText == null)
                leftText = (TextView) parent.findViewById(R.id.left_text);
            return leftText;
        }


        public TextView getRightText() {
            if (rightText == null)
                rightText = (TextView) parent.findViewById(R.id.right_text);
            return rightText;
        }


        public View getDivider() {
            if (divider == null)
                divider = (View) parent.findViewById(R.id.divider);
            return divider;
        }
    }

}
