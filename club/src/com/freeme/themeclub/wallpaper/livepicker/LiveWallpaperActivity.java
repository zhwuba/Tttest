/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.freeme.themeclub.wallpaper.livepicker;

import java.util.List;

import com.freeme.themeclub.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.WallpaperInfo;
import android.os.Bundle;
import android.preference.Preference;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;

public class LiveWallpaperActivity extends Activity {
	private static final String LOG_TAG = "LiveWallpapersPicker";
	private static final int REQUEST_PREVIEW = 100;

	static final String EXTRA_LIVE_WALLPAPER_INTENT = "android.live_wallpaper.intent";
	static final String EXTRA_LIVE_WALLPAPER_SETTINGS = "android.live_wallpaper.settings";
	static final String EXTRA_LIVE_WALLPAPER_PACKAGE = "android.live_wallpaper.package";

	public static final String LIVE_PICKER_PKG = "com.android.wallpaper.livepicker";
	public static final String LIVE_PICKER_PKG_ENTRY = "com.android.wallpaper.livepicker.LiveWallpaperPreview";
	// video live picker app
	public static final String VIDEO_LIVE_PICKER_PKG = "com.mediatek.vlw";
	public static final String VIDEO_LIVE_PICKER_PKG_ENTRY = "com.mediatek.vlw.VideoEditor";
	// video live wallpaper service
	public static final String VIDEO_LIVE_WALLPAPER_PKG = "com.mediatek.vlw";
	public static final String VIDEO_LIVE_WALLPAPER_PKG_ENTRY = "com.mediatek.vlw.VideoLiveWallpaper";

	private static int videoWallpaperNum=0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.live_wallpaper_base);

		Fragment fragmentView = getFragmentManager().findFragmentById(R.id.live_wallpaper_fragment);
		if (fragmentView == null) {
			/* When the screen is XLarge, the fragment is not included in the layout, so show it
			 * as a dialog
			 */
			/* M: ALPS00425492, support rotation change case*/
			if (savedInstanceState == null) {
				DialogFragment fragment = WallpaperDialog.newInstance();
				fragment.show(getFragmentManager(), "dialog");
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == REQUEST_PREVIEW) {
			if (resultCode == RESULT_OK) finish();
		}
	}

	public static class WallpaperDialog extends DialogFragment implements
	AdapterView.OnItemClickListener{
		private static final String EMBEDDED_KEY = "com.android.wallpaper.livepicker."
				+ "LiveWallpaperActivity$WallpaperDiaLog.wMBEDDED_KEY";
		private LiveWallpaperListAdapter mAdapter;
		private boolean mEmbedded;

		public static WallpaperDialog newInstance() {
			WallpaperDialog dialog = new WallpaperDialog();
			dialog.setCancelable(true);
			return dialog;
		}

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			if (savedInstanceState != null && savedInstanceState.containsKey(EMBEDDED_KEY)) {
				mEmbedded = savedInstanceState.getBoolean(EMBEDDED_KEY);
			} else {
				mEmbedded = isInLayout();
			}
			videoWallpaperNum=getVideoWallpaperNumber();
		}

		private int getVideoWallpaperNumber() {
			Intent checkIntent = new Intent();
			checkIntent.setClassName(
					VIDEO_LIVE_WALLPAPER_PKG, 
					VIDEO_LIVE_PICKER_PKG_ENTRY);
			List<ResolveInfo> list = getActivity().getPackageManager().queryIntentActivities(checkIntent, 0);
			if (list == null || list.isEmpty()) {
				return 0;
			}
			else{
				return list.size();
			}

		}

		@Override
		public void onSaveInstanceState(Bundle outState) {
			outState.putBoolean(EMBEDDED_KEY, mEmbedded);
		}

		@Override
		public void onDismiss(DialogInterface dialog) {
			/* On orientation changes, the dialog is effectively "dismissed" so this is called
			 * when the activity is no longer associated with this dying dialog fragment. We
			 * should just safely ignore this case by checking if getActivity() returns null
			 */
			Activity activity = getActivity();
			if (activity != null) {
				activity.finish();
			}
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			final int contentInset = getResources().getDimensionPixelSize(
					R.dimen.dialog_content_inset);
			View view = generateView(getActivity().getLayoutInflater(), null);

			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setNegativeButton(R.string.wallpaper_cancel, null);
			builder.setTitle(R.string.live_wallpaper_picker_title);
			builder.setView(view, contentInset, contentInset, contentInset, contentInset);
			return builder.create();
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			/* If this fragment is embedded in the layout of this activity, then we should
			 * generate a view to display. Otherwise, a dialog will be created in
			 * onCreateDialog()
			 */
			if (mEmbedded) {
				return generateView(inflater, container);
			}
			return null;
		}

		@SuppressWarnings("unchecked")
		private View generateView(LayoutInflater inflater, ViewGroup container) {
			View layout = inflater.inflate(R.layout.live_wallpaper_list, container, false);

			mAdapter = new LiveWallpaperListAdapter(getActivity());
			AdapterView<BaseAdapter> adapterView =
					(AdapterView<BaseAdapter>) layout.findViewById(R.id.list);
			adapterView.setAdapter(mAdapter);
			adapterView.setOnItemClickListener(this);
			/*/Removed by tyd yuanchengye 20140813, for emptyView showing when wallpaper was loading
            adapterView.setEmptyView(layout.findViewById(android.R.id.empty));
			/*/
			return layout;
		}

		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			LiveWallpaperListAdapter.LiveWallpaperInfo wallpaperInfo =
					(LiveWallpaperListAdapter.LiveWallpaperInfo) mAdapter.getItem(position);
			final Intent intent = wallpaperInfo.intent;
			final WallpaperInfo info = wallpaperInfo.info;
			Intent intent2 = new Intent();
			if(position<videoWallpaperNum){
				intent2.setClassName(
						VIDEO_LIVE_PICKER_PKG, 
						VIDEO_LIVE_PICKER_PKG_ENTRY);
			}else{
				intent2.setComponent(new ComponentName(LIVE_PICKER_PKG, LIVE_PICKER_PKG_ENTRY));
				intent2.putExtra(EXTRA_LIVE_WALLPAPER_INTENT, intent);
				intent2.putExtra(EXTRA_LIVE_WALLPAPER_SETTINGS, info.getSettingsActivity());
				intent2.putExtra(EXTRA_LIVE_WALLPAPER_PACKAGE, info.getPackageName());
			}
			startActivity(intent2);
			//LiveWallpaperPreview.showPreview(getActivity(), REQUEST_PREVIEW, intent, info);
		}
	}
}
