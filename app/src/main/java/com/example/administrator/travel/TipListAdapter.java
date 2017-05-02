package com.example.administrator.travel;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by han yuanfeng on 2017/2/11.
 */

public class TipListAdapter extends BaseAdapter
{
    private Context mContext;
    private List<String> mTipList;
    private AutoCompleteTextView searchText;

    public TipListAdapter(Context context, List<String> listString, AutoCompleteTextView view) {
        mContext = context;
        mTipList = listString;
        searchText=view;
    }

    @Override
    public int getCount() {
        return mTipList.size();
    }

    @Override
    public Object getItem(int position) {
        return mTipList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TipListAdapter.ViewHolder holder = null;
        if (convertView == null) {
            holder = new TipListAdapter.ViewHolder();
            convertView = View.inflate(mContext, R.layout.item_tip_list, null);
            holder.title = (TextView) convertView.findViewById(R.id.bus_path_title);
            convertView.setTag(holder);
        } else {
            holder = (TipListAdapter.ViewHolder) convertView.getTag();
        }
        final String item = mTipList.get(position);
        holder.title.setText(item);
        convertView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                searchText.setText(item);

            }
        });

        return convertView;
    }
    private class ViewHolder {
        TextView title;
    }
}
