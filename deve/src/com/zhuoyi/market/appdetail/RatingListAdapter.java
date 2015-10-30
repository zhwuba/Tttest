package com.zhuoyi.market.appdetail;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RatingBar;
import android.widget.TextView;

import com.zhuoyi.market.R;
import com.market.net.data.CommentBto;

public class RatingListAdapter extends BaseAdapter {

	private List<CommentBto> mCommentBto;
	private Context mContext;
	public void setmCommentBto(List<CommentBto> mCommentBto) {
		this.mCommentBto = mCommentBto;
	}
	
	public RatingListAdapter(Context context, List<CommentBto> comments){
		mContext = context;
		mCommentBto = comments;
	}
	
	@Override
	public int getCount() {
		return mCommentBto.size();
	}

	@Override
	public Object getItem(int position) {
		return mCommentBto.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		if(convertView == null){
			viewHolder = new ViewHolder();
			convertView = View.inflate(mContext, com.zhuoyi.market.R.layout.layout_appdetail_rating_item, null);
			viewHolder.ratingBar = (RatingBar) convertView.findViewById(R.id.rating_star);
			viewHolder.content = (TextView) convertView.findViewById(R.id.rating_content);
			viewHolder.ver = (TextView) convertView.findViewById(R.id.rating_ver);
			viewHolder.name = (TextView) convertView.findViewById(R.id.rating_user_name);
			viewHolder.date = (TextView) convertView.findViewById(R.id.rating_date);
			viewHolder.type = (TextView) convertView.findViewById(R.id.rating_type);
			convertView.setTag(viewHolder);
		}else{
			viewHolder = (ViewHolder) convertView.getTag();
		}
		CommentBto commentBto = mCommentBto.get(position);
		if(commentBto != null){
			viewHolder.ratingBar.setRating(commentBto.getStars());
			viewHolder.content.setText(commentBto.getCommentContent());
			viewHolder.ver.setText(commentBto.getVersion());
			viewHolder.name.setText(commentBto.getNickName());
			viewHolder.date.setText(commentBto.getTime());
			viewHolder.type.setText(commentBto.getHstype());
		}
		return convertView;
	}

	
	public String getVersionDesByType(String ver){
		if(ver.equals("0")){
			return mContext.getResources().getString(R.string.app_detail_old_version);
		}else if(ver.equals("1")){
			return mContext.getResources().getString(R.string.app_detail_new_version);
		}
		return "";
	}
	
	static class ViewHolder{
		RatingBar ratingBar;
		TextView content;
		TextView ver;
		TextView name;
		TextView date;
		TextView type;
	}



	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
	}
	
	
	
}
