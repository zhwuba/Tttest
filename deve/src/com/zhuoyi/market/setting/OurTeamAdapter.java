package com.zhuoyi.market.setting;

import com.zhuoyi.market.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class OurTeamAdapter extends BaseAdapter {
    
    private LayoutInflater mInflater = null;
    private String[] mName = null;
    
    public OurTeamAdapter (Context context, String[] name) {
        mInflater = LayoutInflater.from(context);
        mName = name;
    }
    

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return Integer.MAX_VALUE;
    }


    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return 0;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        ViewCache holder = null;
        if(convertView==null) {  
            convertView = mInflater.inflate(R.layout.our_team_item, parent,false);
            holder = new ViewCache(); 
            holder.member_name = (TextView) convertView.findViewById(R.id.member_name);
            convertView.setTag(holder);
        } else {
            holder = (ViewCache)convertView.getTag();
        }
        String name = "";
        if (mName != null) {
            try {
                name = mName[position%mName.length];
            } catch (Exception e) {
                name = "";
            }
        }
        holder.member_name.setText(name);
        return convertView;
    }

    
    static class ViewCache {
        TextView member_name;
    }

}
