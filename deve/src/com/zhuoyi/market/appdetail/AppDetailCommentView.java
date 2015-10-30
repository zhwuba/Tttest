package com.zhuoyi.market.appdetail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.market.net.MessageCode;
import com.market.net.SenderDataProvider;
import com.market.net.data.AppDetailInfoBto;
import com.market.net.data.CommentBto;
import com.market.net.request.CommitUserCommentReq;
import com.market.net.request.GetUserCommentReq;
import com.market.net.response.CommitUserCommentResp;
import com.market.net.response.GetUserCommentResp;
import com.market.net.utils.StartNetReqUtils;
import com.market.view.LoadingProgressDialog;
import com.market.view.PullListView;
import com.market.view.ScaleDialog;
import com.market.view.PullListView.OnRefreshListener;
import com.zhuoyi.market.R;
import com.zhuoyi.market.appResident.MarketApplication;
import com.zhuoyi.market.utils.DensityUtil;
import com.zhuoyi.market.utils.MarketUtils;


public class AppDetailCommentView implements OnClickListener{

	private final int GET_COMMENTS_NUM = 20;
	
	private TextView mCommentCountText;
	private PullListView mRatingList;
	private LinearLayout mCommentProgressBar;
	private View mCommentCountDivider;
	private LinearLayout mRefreshLayout;
	private Button mRefreshButton;
	private TextView mRefreshText;
	private LinearLayout mRefreshImg;

	private Context mContext;
	private Handler mHandler;
	private int mCommentIndex = 0;
	private boolean mIsLoadingComment = false;
	private int mConmentCount;
	private int mRefId;
	private String mPackageName;
	private String mVersionName;
	
	private View mRatingView;
	private LoadingProgressDialog mDialog = null;
	private GetUserCommentReq mCommentReq = null;
	private CommitUserCommentReq mCommitUserCommentReq = null;
	private List<CommentBto> mCommentBtos;
	private RatingListAdapter mRatingListAdapter;
	private String mCommentContent = null;
	private boolean mIsCommentEnable = false;
	
	public AppDetailCommentView(Context context, int refId,Handler handler) {
		mContext = context;
		mHandler = handler;
		mRefId = refId;
	}

	public void initView(View ratingView) {
		mRatingView = ratingView;
		mRatingView.setVisibility(View.GONE);
		mCommentCountText = (TextView) ratingView.findViewById(R.id.app_detail_comment_count);
		mRatingList = (PullListView) ratingView.findViewById(R.id.comments_list);
		mCommentProgressBar = (LinearLayout) ratingView.findViewById(R.id.comment_loadinglayout);
		mCommentCountDivider = (View) ratingView.findViewById(R.id.app_detail_count_divider);
		mRefreshLayout = (LinearLayout) ratingView.findViewById(R.id.refresh_linearLayout_id);
		mRefreshButton = (Button) ratingView.findViewById(R.id.refresh_btn);
		mRefreshText = (TextView) ratingView.findViewById(R.id.refresh_text);
		mRefreshImg = (LinearLayout) ratingView.findViewById(R.id.refresh_layout);
		initListener();
	}


	public void initListener() {
		mRefreshButton.setOnClickListener(this);
		mRatingList.setScrollLoadEnabled(true);
		mRatingList.setOnRefreshListener(new OnRefreshListener() {

			@Override
			public void onRefresh() {
				getCommentByIndex();
			}
		});
	}
	
	
	public void bindData(AppDetailInfoBto appDetailInfoBto) {
		if(appDetailInfoBto != null) {
			mRatingView.setVisibility(View.VISIBLE);
			mPackageName = appDetailInfoBto.getPackageName();
			mVersionName = appDetailInfoBto.getVersionName();
		}
	}

	
	public void getCommentByIndex() {
		if (mIsLoadingComment) {
			return;
		}
		String contents = "";
		mCommentReq = new GetUserCommentReq();
		mCommentReq.setResId(mRefId);
		mCommentReq.setStart(mCommentIndex);
		mCommentReq.setFixed(GET_COMMENTS_NUM);
		mCommentReq.setpName(mPackageName);
		contents = SenderDataProvider.buildToJSONData(mContext,
				MessageCode.GET_USER_COMMENT_REQ, mCommentReq);
		StartNetReqUtils.execListByPageRequest(mHandler, AppDetailInfoActivity.GET_APP_COMMENTS, MessageCode.GET_USER_COMMENT_REQ, contents);
		mIsLoadingComment = true;
	}
	
	
	public void bindCommentData(HashMap<String, Object> map) {
		mIsLoadingComment = false;
		mCommentProgressBar.setVisibility(View.GONE);
		GetUserCommentResp getUserCommentResp = null;
		
		if (map != null && map.size() > 0) {
			getUserCommentResp = (GetUserCommentResp) map.get("userCommentInfo");
			map.clear();
			List<CommentBto> commentBtos = getUserCommentResp.getCommentInfoList();
			mIsCommentEnable = true;
			mHandler.sendEmptyMessage(AppDetailInfoActivity.COMMENT_ENABLE);
			if (commentBtos != null) {
				if (commentBtos.size() > 0) {
					mCommentCountDivider.setVisibility(View.VISIBLE);
					mConmentCount = getUserCommentResp.getTotal();
					mCommentCountText.setText(String.format(mContext.getString(R.string.app_detail_comments_total), mConmentCount));
					int count = commentBtos.size();
					if (count > 0) {
						mCommentIndex += count;
						if (mCommentBtos == null || mCommentBtos.size() <= 0) {
							mCommentBtos = commentBtos;
							mRatingListAdapter = new RatingListAdapter(mContext, mCommentBtos);
							mRatingList.setAdapter(mRatingListAdapter);
						} else {
							mCommentBtos.addAll(commentBtos);
							mRatingListAdapter.setmCommentBto(mCommentBtos);
							mRatingListAdapter.notifyDataSetChanged();
						}
						mRatingList.onPullUpRefreshComplete();
						if (count < GET_COMMENTS_NUM) {
							mRatingList.setHasMoreData(false);
						}
					}
				} else if (mCommentBtos != null && mCommentBtos.size() > 0) {
					setCommentRefreshStatus(2);
					mRatingList.setHasMoreData(false);
				} else {
					setCommentRefreshStatus(0);
				}
			} else {
				if (mCommentBtos != null && mCommentBtos.size() > 0) {
					setCommentRefreshStatus(2);
					mRatingList.setHasMoreData(false);
				} else {
					setCommentRefreshStatus(0);
				}
			}
		} else {
			mRatingList.onPullUpRefreshComplete();
			setCommentRefreshStatus(1);
		}
	}

	
	public void addNewComment(HashMap<String, Object> map) {
		cancelDialog();
		CommitUserCommentResp commitUserCommentResp = null;
		if (map != null && map.size() > 0) {
			commitUserCommentResp = (CommitUserCommentResp) map.get("commitCommentResult");
			if (commitUserCommentResp != null) {
				String result = commitUserCommentResp.getResult();
				if (!TextUtils.isEmpty(result)) {
					if (result.equals("0")) {
						setCommentRefreshStatus(2);
						Toast.makeText(mContext,R.string.app_detail_comments_success,Toast.LENGTH_SHORT).show();
						CommentBto commentBto = commitUserCommentResp.getCommentInfo();
						if (mCommentBtos == null) {
							mCommentBtos = new ArrayList<CommentBto>();
						}
						mCommentBtos.add(0, commentBto);
						if (mRatingListAdapter == null) {
							mRatingListAdapter = new RatingListAdapter(mContext,mCommentBtos);
							mRatingList.setAdapter(mRatingListAdapter);
						} else {
							mRatingListAdapter.setmCommentBto(mCommentBtos);
							mRatingListAdapter.notifyDataSetChanged();
						}
						
						if(mCommentBtos.size() < GET_COMMENTS_NUM) {
							mRatingList.setHasMoreData(false);
						}
						mCommentCountDivider.setVisibility(View.VISIBLE);
						mCommentCountText.setText(String.format(mContext.getString(R.string.app_detail_comments_total), ++mConmentCount));
					} else if (result.equals("1")) {
						Toast.makeText(mContext, R.string.app_detail_had_comment, Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(mContext, R.string.app_detail_comments_failed, Toast.LENGTH_SHORT).show();
					}
				}
			}
		} else {
			Toast.makeText(mContext, R.string.app_detail_comments_failed, Toast.LENGTH_SHORT).show();
		}
	}
	
	
	protected void commitComment(final Context context) {
		if(context == null) return;
		final ScaleDialog commentDialog = new ScaleDialog(context, R.style.MyMarketDialog);
		View view = View.inflate(mContext, R.layout.layout_appdetail_comment_dilog, null);
		final RatingBar ratingBar = (RatingBar) view.findViewById(R.id.app_detail_comment_rating_star);
		final TextView ratingText = (TextView) view.findViewById(R.id.app_detail_comment_rating_tip);
		TextView commitText = (TextView) view.findViewById(R.id.app_detail_comment_commit);
		commentDialog.setContentView(view);
		commentDialog.setCanceledOnTouchOutside(true);
		ratingBar.setOnRatingBarChangeListener(new OnRatingBarChangeListener() {

			@Override
			public void onRatingChanged(RatingBar ratingBar, float rating,
					boolean fromUser) {
				switch ((int) rating) {
				case 1:
					mCommentContent = context.getString(R.string.app_detail_comments_rating_star_1);
					break;
				case 2:
					mCommentContent = context.getString(R.string.app_detail_comments_rating_star_2);

					break;
				case 3:
					mCommentContent = context.getString(R.string.app_detail_comments_rating_star_3);

					break;
				case 4:
					mCommentContent = context.getString(R.string.app_detail_comments_rating_star_4);

					break;
				case 5:
					mCommentContent = context.getString(R.string.app_detail_comments_rating_star_5);

					break;
				}
				ratingText.setText(mCommentContent);
			}
		});

		WindowManager.LayoutParams lp = commentDialog.getWindow().getAttributes();
		lp.width = DensityUtil.getScreenWidth(context) - (int) (mContext.getResources().getDimension(R.dimen.detail_comment_dialog_margin));
		commentDialog.getWindow().setAttributes(lp);
		commentDialog.show();
		commitText.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (ratingBar.getRating() == 0) {
					Toast.makeText(mContext, R.string.app_detail_comments_no_rating, Toast.LENGTH_SHORT).show();
					return;
				}
				String content = "";
				String contents = "";
				int versionCode = 0;
				try {
					versionCode = mContext.getPackageManager().getPackageInfo(mPackageName, 0).versionCode;
				} catch (Exception e) {
					e.printStackTrace();
				}
				mCommitUserCommentReq = new CommitUserCommentReq();
				mCommitUserCommentReq.setResId(mRefId);
				mCommitUserCommentReq.setOpenId(com.market.account.dao.UserInfo.get_openid(mContext));
				mCommitUserCommentReq.setCommentContent(TextUtils.isEmpty(content) ? mCommentContent : content);
				mCommitUserCommentReq.setStars((int) ratingBar.getRating());
				mCommitUserCommentReq.setVersion(versionCode);
				mCommitUserCommentReq.setVerName(mVersionName);
				mCommitUserCommentReq.setpName(mPackageName);
				mCommitUserCommentReq.setUuid(MarketApplication.mUUID);

				contents = SenderDataProvider.buildToJSONData(mContext, MessageCode.COMMIT_USER_COMMENT_REQ, mCommitUserCommentReq);
				StartNetReqUtils.execMarketRequest(mHandler, AppDetailInfoActivity.USER_COMMENTS_COMMIT, MessageCode.COMMIT_USER_COMMENT_REQ, contents);
				commentDialog.dismiss();
				showDialog(context, context.getString(R.string.app_detail_comments_commiting));
			}
		});
	}

	
	public void firstLoadCommont() {
		setCommentRefreshStatus(2);
		mCommentCountDivider.setVisibility(View.GONE);
		mCommentProgressBar.setVisibility(View.VISIBLE);
		getCommentByIndex();
	}
	
	
	/**
	 * @param type
	 *  	<li>0 : no data </li>
	 * 		<li>1 : load failed</li>
	 * 		<li>2 : hide</li>
	 */
	public void setCommentRefreshStatus(int type) {
		switch (type) {
		case 0: 
			mRefreshLayout.setVisibility(View.VISIBLE);
			mRefreshText.setVisibility(View.VISIBLE);
			mRefreshImg.setVisibility(View.GONE);
			break;
		case 1: 
			if (mCommentBtos != null && mCommentBtos.size() > 0) {
				AppDetailInfoActivity.showToast(mContext, R.string.no_connect_hint);
				setCommentRefreshStatus(2);
			} else {
				mRefreshLayout.setVisibility(View.VISIBLE);
				mRefreshImg.setVisibility(View.VISIBLE);
				mRefreshText.setVisibility(View.GONE);
			}
			break;
		case 2: 
			mRefreshLayout.setVisibility(View.GONE);
		default:
			break;
		}
	}
	
	
	private void showDialog(Context context, String message) {
		if (mDialog == null) {
			mDialog = new LoadingProgressDialog(context);
			mDialog.setMessage(context.getString(
					R.string.app_detail_comments_commiting));
			mDialog.setIndeterminate(true);
			mDialog.setCancelable(false);
		}
		mDialog.show();
	}


	private void cancelDialog() {
//		if (mInstance != null && .isFinishing()) {
			if (null != mDialog && mDialog.isShowing()) {
				mDialog.cancel();
			}
//		}
	}
	

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.refresh_btn:
			if (MarketUtils.getAPNType(mContext) != -1) {
				mCommentCountDivider.setVisibility(View.GONE);
				mCommentProgressBar.setVisibility(View.VISIBLE);
				setCommentRefreshStatus(2);
				getCommentByIndex();
			} else {
				Toast.makeText(mContext,R.string.no_connect_hint, Toast.LENGTH_SHORT).show();
			}
			break;
		}
	}
	
	
	public boolean isCommentEnable() {
		return mIsCommentEnable;
	}
}
