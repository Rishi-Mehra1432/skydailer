package com.multitech.skydailer.constantvalues;

import android.app.Activity;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;




public class OnCallLogScrollListener implements OnScrollListener {

	private MainActivity activity;

	public OnCallLogScrollListener(Activity activity) {
		this.activity = (MainActivity)activity;
	}

	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
	
	}

	public void onScrollStateChanged(AbsListView view, int scrollState) {
		/*if (scrollState != SCROLL_STATE_IDLE && activity.isNumpadVisible()) {
			activity.hideNumpad();
		}*/
	}
}
