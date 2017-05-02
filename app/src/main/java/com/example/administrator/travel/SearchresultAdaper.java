package com.example.administrator.travel;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.amap.api.maps.model.LatLng;

import java.util.List;

/**
 * Created by han yuanfeng on 2017/2/14.
 */

public class SearchresultAdaper extends BaseAdapter
{
    private Context mContext;
    private List<String> mpointList;
    private List<LatLng> mlatLnglist;
    private AutoCompleteTextView searchText;

    public SearchresultAdaper(Context context, List<String> listString, List<LatLng> mlatLnglist, AutoCompleteTextView view) {
        mContext = context;
        mpointList = listString;
        searchText = view;
        this.mlatLnglist = mlatLnglist;
    }

    @Override
    public int getCount() {
        return mpointList.size();
    }

    @Override
    public Object getItem(int position) {
        return mpointList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        SearchresultAdaper.ViewHolder holder = null;
        if (convertView == null) {
            holder = new SearchresultAdaper.ViewHolder();
            convertView = View.inflate(mContext, R.layout.item_searchresult_item, null);
            holder.title = (TextView) convertView.findViewById(R.id.bus_path_title);
            convertView.setTag(holder);
        } else {
            holder = (SearchresultAdaper.ViewHolder) convertView.getTag();
        }
        final String item = mpointList.get(position);
        holder.title.setText(item);
        convertView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("addressName", item);
                intent.putExtra("Latitude", mlatLnglist.get(position).latitude);
                intent.putExtra("Longitude", mlatLnglist.get(position).longitude);
                ListenerManager.getInstance().sendBroadCast(intent);
            }
        });

        return convertView;
    }

    private class ViewHolder {
        TextView title;
    }
}
