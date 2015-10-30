package com.freeme.themeclub.wallpaper.resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.freeme.themeclub.wallpaper.widget.DataGroup;

public class ResourceSet extends ArrayList<DataGroup<Resource>> {

    private static final long serialVersionUID = 1L;
    private static Map<String, ResourceSet> instances = new HashMap<String, ResourceSet>();
    
    public static ResourceSet getInstance(String id) {
        ResourceSet resourceSet = instances.get(id);
        if (resourceSet == null) {
        	synchronized (instances) {
        		resourceSet = new ResourceSet();
	            instances.put(id, resourceSet);
        	}
        }
        return resourceSet;
    }
    
    private Map<String, Object> mMetaData = null;

    private ResourceSet() {
        mMetaData = new HashMap<String, Object>();
    }

    public void set(List<Resource> resourceSet, int group) {
        DataGroup<Resource> groups = get(group);
        groups.clear();
        groups.addAll(resourceSet);
    }

    public Object getMetaData(String key) {
        return mMetaData.get(key);
    }
    
    public void setMetaData(String key, Object value) {
        mMetaData.put(key, value);
    }
}