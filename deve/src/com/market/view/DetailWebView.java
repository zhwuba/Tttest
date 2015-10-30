package com.market.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.webkit.WebView;
import android.widget.Toast;

public class DetailWebView extends WebView {

	public DetailWebView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			applyAfterMoveFix();
			break;

		default:
			break;
		}
		
		return super.onTouchEvent(event);
		
	}

	
	public void applyAfterMoveFix() { 
		onScrollChanged(getScrollX(), getScrollY(), getScrollX(), getScrollY()); 
		} 
	
	
}
