package com.example.ccsph2.mediaplayerplaylist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.ccsph2.mediaplayerdemo.ContentData;

import java.util.ArrayList;

/**
 * Created by ccsph2 on 18/01/06.
 */

public class ContentAdapter extends BaseAdapter {

    private static class ViewHolder {
        TextView title;
        TextView artist;
    }

    ///////////////////////////////////



    private Context mContext;
    private LayoutInflater mInflater;
    private ArrayList<ContentData> mDataSource;

    public ContentAdapter(Context context, ArrayList<ContentData> items) {
        mContext = context;
        mDataSource = items;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    // Adapter methods
    @Override
    public Object getItem(int position) {
        return mDataSource.get(position);
    }

    @Override
    public int getCount() {
        return mDataSource.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = mInflater.inflate(android.R.layout.simple_list_item_2, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.title = (TextView) convertView.findViewById(android.R.id.text1);
            viewHolder.artist = (TextView) convertView.findViewById(android.R.id.text2);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        ContentData contentData = (ContentData) getItem(position);
        viewHolder.title.setText(contentData.getTitle());
        viewHolder.artist.setText(contentData.getArtist());

        return convertView;
    }
}
