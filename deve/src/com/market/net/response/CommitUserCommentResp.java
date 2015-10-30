package com.market.net.response;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.market.net.data.CommentBto;

public class CommitUserCommentResp extends BaseInfo{
	@Expose
	@SerializedName("result")
	private String result;

	@Expose
	@SerializedName("commentInfo")
	private CommentBto commentInfo;
	
	public CommentBto getCommentInfo() {
		return commentInfo;
	}

	public void setCommentInfo(CommentBto commentInfo) {
		this.commentInfo = commentInfo;
	}
	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}
}
