package com.market.net.response;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.market.net.data.CommentBto;

public class GetUserCommentResp extends BaseInfo{

	@Expose
	@SerializedName("commentInfoList")
	private List<CommentBto> commentInfoList;

	@Expose
	@SerializedName("total")
	private int total;

	public List<CommentBto> getCommentInfoList() {
		return commentInfoList;
	}

	public void setCommentInfoList(List<CommentBto> commentInfoList) {
		this.commentInfoList = commentInfoList;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}
}
