package com.zhuoyi.market.adapter;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * 通用的viewHolder
 * 封装了viewHolder自身的实例化,convertView的实例化以及获取控件的方法
 * @author JLu
 *
 */
public class ViewHolder {

	private View mConvertView;
	private int mPosition;
	private SparseArray<View> mViews;
	
	private ViewHolder(Context context,View convertView,int layoutId,ViewGroup parent,int position) {
		this.mPosition = position;
		this.mViews = new SparseArray<View>();
		
		mConvertView = LayoutInflater.from(context).inflate(layoutId, parent,false);
		mConvertView.setTag(this);
	}
	
	
	public static ViewHolder get(Context context,View convertView,int layoutId,ViewGroup parent,int position) {
		
		if(convertView == null) {
			return new ViewHolder(context, convertView, layoutId, parent,position);
		} else {
			ViewHolder viewHolder = (ViewHolder)convertView.getTag();
			viewHolder.mPosition = position;
			return viewHolder;
		}
	}
	
	
	/**
	 * 通过viewId获取控件
	 * @param viewId
	 * @return
	 */
	public <T extends View> T getView(int viewId) {
		View view = mViews.get(viewId);
		if(view == null) {
			view = mConvertView.findViewById(viewId);
			mViews.put(viewId, view);
		}
		
		return (T)view;
	}
	
	
	public View getConvertView() {
		return mConvertView;
	}
	
	public ViewHolder setText(int viewId,String text) {
		TextView tv = getView(viewId);
		tv.setText(text);
		return this;
	}
	
}
