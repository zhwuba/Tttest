package com.freeme.themeclub.wallpaper.local;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.freeme.themeclub.wallpaper.ResourceHelper;
import com.freeme.themeclub.wallpaper.base.IntentConstants;
import com.freeme.themeclub.wallpaper.cache.FolderCache;
import com.freeme.themeclub.wallpaper.os.ExtraFileUtils;
import com.freeme.themeclub.wallpaper.resource.Resource;
import com.freeme.themeclub.wallpaper.widget.AsyncAdapter;

public abstract class ResourceFolder implements AsyncAdapter.AsyncLoadDataVisitor<Resource> {

	private static final String CACHE_PREFIX = "cache";

	protected String mCacheFileName = null;
	protected Context mContext = null;
	protected boolean mDirtyResources = false;
	protected Map<String, Integer> mFileFlags = null;
	private boolean mFirstLoadData = false;
	protected String mFolderDescription = null;
	protected String mFolderPath = null;
	protected String mKeyword = null;
	protected Bundle mMetaData = null;
	private int mResourceSeq = 0;

	public ResourceFolder(Context context, Bundle metaData, String folderPath) {
		this(context, metaData, folderPath, null);
	}

	public ResourceFolder(Context context, Bundle metaData, String folderPath, String keyword) {
		mFirstLoadData = true;
		mFileFlags = new HashMap<String, Integer>();
		mResourceSeq = 0;
		mContext = context;
		mMetaData = metaData;
		mFolderPath = folderPath;
		mKeyword = keyword;

		if (!TextUtils.isEmpty(folderPath)) {
			String cacheFolder = ExtraFileUtils.standardizeFolderPath(
					mMetaData.getString(IntentConstants.EXTRA_CACHE_LIST_FOLDER));
			File file = new File(new StringBuilder(cacheFolder).append(folderPath.replace('/', '_')).toString());
			if (file.exists()) {
				file.delete();
			}

			mCacheFileName = new StringBuilder(cacheFolder).append(CACHE_PREFIX).append(folderPath.replace('/', '_')).toString();
		}
	}

	private void addResourceIntoUI(AsyncAdapter<Resource>.AsyncLoadDataTask task, Resource r) {
		if (r != null) {
			if (mResourceSeq == 0) {
				r.setDividerTitle(mFolderDescription);
			}

			task.onLoadData(new Resource[] { r });

			mResourceSeq += 1;
		}
	}

	protected Resource buildResource(String filePath) {
		Bundle information = new Bundle();

		File file = new File(filePath);
		information.putString(Resource.NAME, file.getName());
		information.putString(Resource.SIZE, String.valueOf(file.length()));
		information.putLong(Resource.MODIFIED_TIME, file.lastModified());
		information.putString(Resource.LOCAL_PATH, filePath);

		Resource resource = new Resource();
		resource.setInformation(information);
		// XXX 
		//resource.setFileHash(ResourceHelper.getFileHash(filePath));
		return resource;
	}

	public boolean dataChanged() {
		FolderCache.FolderInfo folderInfo = ResourceHelper.getFolderInfoCache(mFolderPath);
		if (folderInfo == null) {
			return false;
		}

		Iterator<String> i = mFileFlags.keySet().iterator();
		while (i.hasNext()) {
			String path = (String)i.next();
			if (!folderInfo.files.containsKey(path)) {
				return true;
			}
		}

		i = folderInfo.files.keySet().iterator();
		while (i.hasNext()) {
			String path = i.next();
			if (!mFileFlags.containsKey(path) && isInterestedResource(path)) {
				return true;
			}
		}

		return false;
	}

	public String getFolderPath() {
		return mFolderPath;
	}

	public List<Resource> getHeadExtraResource() {
		return null;
	}

	protected boolean isInterestedResource(String path) {
		return true;
	}

	public final void loadData(AsyncAdapter<Resource>.AsyncLoadDataTask task) {
		onPreLoadData();
		onLoadData(task);
		onPostLoadData();
	}

	protected final void onLoadData(AsyncAdapter<Resource>.AsyncLoadDataTask task) {
		mResourceSeq = 0;
		List<Resource> headExtraResource = getHeadExtraResource();
		if (headExtraResource != null) {
			for (Iterator<Resource> i = headExtraResource.iterator(); 
					i.hasNext(); 
					addResourceIntoUI(task, i.next()))
				;
		}

		if (!TextUtils.isEmpty(mFolderPath)) {
			if (mFirstLoadData) {
				try {
					readCache();
				} catch (FileNotFoundException e) {
					reset();
				} catch (Exception e) {
					reset();
				}
				mFirstLoadData = false;
			}

			mDirtyResources = false;
			FolderCache.FolderInfo folderInfo = ResourceHelper.getFolderInfoCache(mFolderPath);
			if (folderInfo != null) {
				// file-path list
				ArrayList<String> list = new ArrayList<String>(folderInfo.files.keySet());

				// sort
				sortResource(list, folderInfo);

				final int size = list.size();
				for (int i = 0; i < size; ++i) {
					String path = list.get(i);
					if (!path.endsWith(".temp")) {
						if (!mFileFlags.containsKey(path)) {
							addItem(path);
							mDirtyResources = true;
						}
						Resource resource = buildResource(path);
						if (resource != null) {
							String title = resource.getTitle();
							if (mKeyword == null 
									|| title != null && title.toLowerCase().contains(mKeyword) 
									|| folderInfo.files.get(path).name.toLowerCase().contains(mKeyword)) {
								addResourceIntoUI(task, resource);
							}
						}
					}
				}

				// check file-flag list
				list = new ArrayList<String>(mFileFlags.keySet());
				for (int i = 0; i < list.size(); i++) {
					String path = list.get(i);
					if (!folderInfo.files.containsKey(path)) {
						removeItem(path);
						mDirtyResources = true;
					}
				}
			}

			if (mDirtyResources) {
				try {
					saveCache();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	protected void onPostLoadData() {
	}

	protected void onPreLoadData() {
	}

	@SuppressWarnings("unchecked")
	protected void readDataFromStream(ObjectInputStream objStream) throws Exception {
		mFileFlags = (HashMap<String, Integer>) objStream.readObject();
	}

	protected void writeDataToStream(ObjectOutputStream out) throws IOException {
		out.writeObject(mFileFlags);
	}

	protected void addItem(String filePath) {
	}

	protected void removeItem(String filePath) {
		mFileFlags.remove(filePath);
	}

	protected void reset() {
		mFileFlags.clear();
	}

	protected void readCache() throws Exception {
		File file = new File(mCacheFileName);

		ObjectInputStream objStream = null;
		try {
			objStream = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file), 16384));
			readDataFromStream(objStream);
		} finally {
			if (objStream != null) {
				try {
					objStream.close();
				} catch (IOException e) {
				}
			}
		}
	}

	protected void saveCache() throws Exception {
		File file = new File(mCacheFileName);
		file.delete();

		ObjectOutputStream objStream = null; 
		try {
			objStream = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
			writeDataToStream(objStream);
		} finally {
			if (objStream != null) {
				try {
					objStream.close();
				} catch (IOException e) {
				}
			}
		}
	}

	public void setFolderDescription(String folderTitle) {
		mFolderDescription = folderTitle;
	}

	public void setKeyword(String keyword) {
		mKeyword = keyword.toLowerCase();
	}

	protected void sortResource(List<String> list, final FolderCache.FolderInfo folderInfo) {
		if (ResourceHelper.isSystemResource(mFolderPath) || ResourceHelper.isDataResource(mFolderPath)) {
			Collections.sort(list);
		} else {
			Collections.sort(list, new Comparator<String>() {
				public int compare(String x0, String x1) {
					try {
						return Long.valueOf(folderInfo.files.get(x1).modifiedTime)
								.compareTo(Long.valueOf(folderInfo.files.get(x0).modifiedTime));
					} catch (Exception e) {
						return 0;
					}
				}
			});
		}
	}
}