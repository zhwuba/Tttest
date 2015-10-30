package com.market.net.response;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.market.net.data.SubjectInfoBto;

public class GetSubjectResp {
	@Expose
	@SerializedName("index")
	private int index;
	@Expose
	@SerializedName("subject")
	private List<SubjectInfoBto> subject;

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public List<SubjectInfoBto> getSubject() {
		return subject;
	}

	public void setSubject(List<SubjectInfoBto> subject) {
		this.subject = subject;
	}

}