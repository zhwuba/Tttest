package com.freeme.themeclub.wallpaper;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.freeme.themeclub.R;
import com.freeme.themeclub.wallpaper.base.IntentConstants;
import com.freeme.themeclub.wallpaper.base.ResourceAdapter;
import com.freeme.themeclub.wallpaper.base.ResourceConstants;
import com.freeme.themeclub.wallpaper.base.ResourceListFragment;
import com.freeme.themeclub.wallpaper.util.LogUtils;
import com.freeme.themeclub.wallpaper.util.WallpaperUtils;


public class LocalResourceListFragment extends ResourceListFragment {

	private static final String TAG = "LocalResourceListFragment";
	private static final boolean THIRD_PARTY_PICKER_ENABLE = false;
	
	/**
	 * This flag indicates whether using the koobee style.
	 */
	private static final boolean USE_KOOBEE_STYLE = true;
	
	
	protected int mResourceType;
	
	private Intent mPickerIntent = null;
	private ArrayList<ResolveInfo> mThirdAppInfoList = null;
	
	@Override
	public void onResume() {
		super.onResume();
		mAdapter.loadData();
		
		refreshCurrentUsingFlags();
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case WallpaperUtils.REQUEST_CODE_PICK_IMAGE: {
				if (data != null) {
                    Uri pickedItem = data.getData();
                    LogUtils.d(TAG, "pickedItem : " + pickedItem.toString());
                    WallpaperUtils.cropAndApplyWallpaper(mActivity, this, mResourceType, 
                    		pickedItem, false, true);
                }
				break;
			}
			case WallpaperUtils.REQUEST_CODE_CROP_APPL_DESKTOP:
			case WallpaperUtils.REQUEST_CODE_CROP_APPL_LOCKSCREEN: {
				WallpaperUtils.dealCropWallpaperResult(mActivity, requestCode, resultCode);
				break;
			}
			
			default: 
				super.onActivityResult(requestCode, resultCode, data);
				break;
		}
	}

	@Override
	protected void onVisiableChanged(boolean visiable) {
		super.onVisiableChanged(visiable);
		if (visiable) {
			refreshCurrentUsingFlags();
		}
	}

	protected int getContentView() {
		return R.layout.resource_local_list;
	}
	
	protected ResourceAdapter getAdapter() {
		return new LocalResourceAdapter(this, mMetaData);
	}
	
	@Override
	protected void pickMetaData(Bundle metaData) {
		super.pickMetaData(metaData);
		mResourceType = metaData.getInt(IntentConstants.EXTRA_RESOURCE_FLAG, ResourceConstants.R_NONE);
	}
	
	@Override
	protected void addMetaData(Bundle metaData) {
		super.addMetaData(metaData);
		metaData.putString(IntentConstants.EXTRA_RESOURCE_SET_SUBPACKAGE, ".local" + mResourceType);
	}

	@Override
	public List<Integer> onFragmentCreateOptionsMenu(Menu menu) {
		// only one
		ArrayList<Integer> items = new ArrayList<Integer>(1);
		
		int id = -1;
		switch (mResourceType) {
			default:
			case ResourceConstants.R_DESKTOP_WALLPAER:
			case ResourceConstants.R_LOCKSCREEN_WALLPAER: {
				//*/
				if (USE_KOOBEE_STYLE) break;
				//*/
				
				id = R.string.theme_select_others;
				
				MenuItem item = null;
				int iconResId = 0;
				if (THIRD_PARTY_PICKER_ENABLE) {
//					item = menu.add(0, id, 0, id);
					iconResId = R.drawable.picker;
				} else {
					resolveIntent();
					
					final ArrayList<ResolveInfo> list = mThirdAppInfoList;
					for (int i = list.size() - 1; i >= 0; --i) {
						ResolveInfo ri = list.get(i);
						if (ri.activityInfo.packageName.toLowerCase().contains("gallery")) {
							mPickerIntent.setComponent(new ComponentName(
									ri.activityInfo.packageName, ri.activityInfo.name));
//							item = menu.add(0, id, 0, 
//									ri.loadLabel(mActivity.getPackageManager()));
							iconResId = R.drawable.picker;
							break;
						}
					}
				}
		        
				if (item != null) {
			        item.setShowAsAction(
			        		MenuItem.SHOW_AS_ACTION_ALWAYS |
			        		MenuItem.SHOW_AS_ACTION_WITH_TEXT |
			        		0x80000000);
			        item.setIcon(iconResId);
				} else {
					id = -1;
				}
				break;
			}
		}
		
		if (id != -1) {
//			items.add(id);
		}
		return items;
	}

	@Override
	public void onFragmentOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.string.theme_select_others: {
				Intent pickerChsr;
				if (THIRD_PARTY_PICKER_ENABLE) {
					resolveIntent();
					
					pickerChsr = new Intent(mActivity, ThirdPartyPickersActivity.class);
					pickerChsr.setAction(Intent.ACTION_MAIN);
					pickerChsr.putExtra(Intent.EXTRA_INTENT, mPickerIntent);
					pickerChsr.putExtra(IntentConstants.EXTRA_RESOURCE_FLAG, mResourceType);
					pickerChsr.putParcelableArrayListExtra(
							IntentConstants.EXTRA_RESOLVE_INFO_LIST, mThirdAppInfoList);
				} else {
					// go into Gallery directly
					pickerChsr = mPickerIntent;
				}
				
				if (mResourceType == ResourceConstants.R_DESKTOP_WALLPAER
						|| mResourceType == ResourceConstants.R_LOCKSCREEN_WALLPAER) {
					startActivityForResult(pickerChsr, WallpaperUtils.REQUEST_CODE_PICK_IMAGE);
				} else {
					startActivity(pickerChsr);
				}
				break;
			}
			default: {
				super.onFragmentOptionsItemSelected(item);
				break;
			}
		}
	}
	 
	protected void setupUI(){
	    super.setupUI();
	    
	    setHasOptionsMenu(true);
	}
//	@Override
//	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
////	    menu.clear();
//	    if (USE_KOOBEE_STYLE) {
//            resolveIntent();
//            final ArrayList<ResolveInfo> list = mThirdAppInfoList;
//            for (int i = list.size() - 1; i >= 0; --i) {
//                ResolveInfo ri = list.get(i);
//                if (ri.activityInfo.packageName.toLowerCase().contains("gallery")) {
//                    mPickerIntent.setComponent(new ComponentName(
//                            ri.activityInfo.packageName, ri.activityInfo.name));
////                    MenuItem item=menu.add(0, 1, 1, ri.loadLabel(mActivity.getPackageManager()));
////                    item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
//                    
////                    menu.add(Menu.NONE,Menu_Refresh,Menu.NONE,R.string.refresh).
////                        setIcon(com.android.internal.R.drawable.ic_menu_refresh).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
//                    break;
//                }
//            }
//        }
//	    super.onCreateOptionsMenu(menu, inflater);
//	}
//		
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//	    if(item.getItemId()==2)
//	    {
//	        Intent pickerChsr = mPickerIntent;
//	        if (mResourceType == ResourceConstants.R_DESKTOP_WALLPAER
//	                 || mResourceType == ResourceConstants.R_LOCKSCREEN_WALLPAER) {
//	            startActivityForResult(pickerChsr, WallpaperUtils.REQUEST_CODE_PICK_IMAGE);
//	        } else {
//	            startActivity(pickerChsr);
//	         }
//	    }
//       
//	    return super.onOptionsItemSelected(item);
//	}
	
	private void refreshCurrentUsingFlags() {
        String currentUsingPath = ResourceHelper.computeCurrentUsingPath(
        		mActivity, mMetaData, mResourceType);
        if (mAdapter.updateCurrentUsingPath(currentUsingPath)) {
            mAdapter.notifyDataSetChanged();
        }
    }
	
	private ArrayList<ResolveInfo> resolveIntent(Intent pickIntent, ArrayList<String> excludes) {
        final PackageManager packageManager = mActivity.getPackageManager();
        
        List<ResolveInfo> ris = packageManager.queryIntentActivities(
        					pickIntent, 
        					PackageManager.MATCH_DEFAULT_ONLY);
        if (excludes != null && !excludes.isEmpty()) {
        	
        	for (int i = ris.size() - 1; i>= 0; --i) {
        		ResolveInfo ri = ris.get(i);
        		for (int j = excludes.size() - 1; j >= 0; --j) {
        			if (ri.activityInfo.name.indexOf(excludes.get(j)) >= 0) {
        				ris.remove(i);
        				break;
        			}
        		}
        	}
        }
        if (ris.size() > 0) {
            Collections.sort(ris, new NameComparator(packageManager));
        }
        return new ArrayList<ResolveInfo>(ris);
    }
	
	private void resolveIntent() {
	    if (mThirdAppInfoList == null) {
	        ArrayList<String> excludes = new ArrayList<String>(2);
	        // build exclude list
//	        excludes.add(ThemeResourceTabActivity.class.getName());
	        
	        switch (mResourceType) {
	        	default: 
		        case ResourceConstants.R_DESKTOP_WALLPAER:
		        case ResourceConstants.R_LOCKSCREEN_WALLPAER:
		        	mPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
		            mPickerIntent.addCategory(Intent.CATEGORY_OPENABLE);
		            mPickerIntent.setType("image/*");
		        	break;
	        }
	        
	        // get third apps list
	        mThirdAppInfoList = resolveIntent(mPickerIntent, excludes);
	    }
	}
	
	protected View getHeaderView() {
        View view = LayoutInflater.from(mActivity).inflate(
                R.layout.wallpaper_headerview, mListView, false);
        ImageView systemImage = (ImageView) view.findViewById(R.id.system_preview_thumb);
        systemImage.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (USE_KOOBEE_STYLE) {
                    resolveIntent();
                    final ArrayList<ResolveInfo> list = mThirdAppInfoList;
                    for (int i = list.size() - 1; i >= 0; --i) {
                        ResolveInfo ri = list.get(i);
                        if (ri.activityInfo.packageName.toLowerCase().contains("gallery")) {
                            mPickerIntent.setComponent(new ComponentName(
                                    ri.activityInfo.packageName, ri.activityInfo.name));
                            break;
                        }
                    }
                }
                
                Intent pickerChsr = mPickerIntent;
                if (mResourceType == ResourceConstants.R_DESKTOP_WALLPAER
                         || mResourceType == ResourceConstants.R_LOCKSCREEN_WALLPAER) {
                    startActivityForResult(pickerChsr, WallpaperUtils.REQUEST_CODE_PICK_IMAGE);
                } else {
                    startActivity(pickerChsr);
                }

            }
        });
        ImageView liveImage = (ImageView) view.findViewById(R.id.live_preview_thumb_item);
        liveImage.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent target = new Intent();
                target.setClassName(
                        "com.android.wallpaper.livepicker", 
                        "com.android.wallpaper.livepicker.LiveWallpaperActivity");
                startActivity(target);
            }
        });
        if(checkLiveWallpaperPicker()){
            systemImage.setImageResource(R.drawable.choose_from_album);
        }else{
            liveImage.setVisibility(View.GONE);
            systemImage.setImageResource(R.drawable.choose_from_album);
        }

        return view;
    }
    
    private boolean checkLiveWallpaperPicker() {
        Intent checkIntent = new Intent();
        checkIntent.setClassName(
                "com.android.wallpaper.livepicker", 
                "com.android.wallpaper.livepicker.LiveWallpaperActivity");
        List<ResolveInfo> list = getActivity().getPackageManager().queryIntentActivities(checkIntent, 0);
        if (list == null || list.isEmpty()) {
            return false;
        }else{
            return true;
        }
    }
	
	
	private static class NameComparator implements Comparator<ResolveInfo> {
		private PackageManager mPM = null;
		
		public NameComparator(PackageManager packageManager) {
			mPM = packageManager;
		}
		
		public int compare(ResolveInfo lhs, ResolveInfo rhs) {
			String lpkgName = lhs.activityInfo.packageName;
            String rpkgName = rhs.activityInfo.packageName;
            int compareResult = lpkgName.compareTo(rpkgName);
            if (compareResult == 0) {
            	Object lobj = lhs.loadLabel(mPM);
            	Object robj = rhs.loadLabel(mPM);
            	if (lobj == null) lobj = lhs.activityInfo.name;
            	if (robj == null) robj = rhs.activityInfo.name;
            	
            	compareResult = lobj.toString().compareTo(robj.toString());
            }
            return compareResult;
		}
	}
}