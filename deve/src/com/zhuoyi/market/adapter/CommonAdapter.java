package com.zhuoyi.market.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

/**
 * listView 和 gridView 的通用抽象adapter,
 * 该adapter的实现类可以只复写convert方法,实现item布局数据的装载
 * @author JLu
 *
 * @param <T>
 */
public abstract class CommonAdapter<T> extends BaseAdapter {
	protected Context mContext;
	protected List<T> mDatas;
	protected LayoutInflater mInflater;
	protected int mLayoutId;
	
	protected String mExisted = "end";
	
	public CommonAdapter(Context context,int layoutId) {
		this.mContext = context;
		mInflater = LayoutInflater.from(context);
		mLayoutId = layoutId;
	}
	
	public CommonAdapter(Context context, int layoutId, List<T> datas) {
		this.mContext = context;
		this.mDatas = datas;
		mInflater = LayoutInflater.from(context);
		mLayoutId = layoutId;
	}
	
	public void setDatas(List<T> datas) {
		mDatas = datas;
	}
	
	public void addDatas(List<T> newDatas) {
		if(mDatas != null) {
			mDatas.addAll(newDatas);
		}
	}
	
	
    public void setExisited(String exisited) {
        mExisted = exisited + ";" + mExisted;
    }
    
    
    public String getExisited() {
        return mExisted;
    }
	

	@Override
	public int getCount() {
		return mDatas==null ? 0 : mDatas.size();
	}

	@Override
	public T getItem(int position) {
		try {
			return mDatas==null ? null : mDatas.get(position);
		} catch(IndexOutOfBoundsException e) {
			return null;
		}
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		ViewHolder holder = ViewHolder.get(mContext, convertView, mLayoutId, parent, position);
		
		convert(holder,getItem(position),position);
		
		return holder.getConvertView();
	}
	
	
	/**
	 * 完成item数据装载的方法, 在getView()中被调用
	 * @param holder getView中被初始化的viewHolder
	 * @param t 某条item布局对应的数据对象
	 * @param position
	 */
	public abstract void convert(ViewHolder holder, T bean, int position);
	

}
