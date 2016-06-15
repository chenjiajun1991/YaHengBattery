package baidumapsdk.demo.demoapplication;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

public class BatteryAdapter extends BaseAdapter {
    private Context mContext;
    private List<?> mItemList;
    private boolean mSelectable = false;
    private boolean mHasListView = false;
    private int mFriendPos;

    public BatteryAdapter(Context context,
                          List<?> items,
                          boolean selectable,
                          int friendPos,
                          boolean hasListView) {
        mContext = context;
        mItemList = items;
        mSelectable = selectable;
        mFriendPos = friendPos;
        mHasListView = hasListView;
    }

    @Override
    public int getCount() {
        if (mSelectable) {
            return mItemList != null ? mItemList.size() + 1 : 1;
        } else {
            return mItemList != null ? mItemList.size() : 0;
        }
    }

    @Override
    public Object getItem(int position) {
        return mItemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.complex_listview_item,
                    null);
        }

        final ListView itemList = (ListView)convertView.findViewById(R.id.complex_list_view);
        itemList.setVisibility(View.GONE);

        TextView item = (TextView) convertView.findViewById(R.id.complex_list_item);
        if (mFriendPos != 10024) {
            item.setTextColor(Color.BLACK);
        }
        CharSequence name = null;
        if (mSelectable && position == 0) {
            name = mContext.getString(R.string.select_all);
        } else {
            if (mSelectable)
                name = (String) mItemList.get(position - 1);
            else {
                name = (String) mItemList.get(position);
            }
        }

        if (!mSelectable && (position >= mFriendPos)) {
            item.setTextColor(0xFF0000FF);
        }
        item.setText(name);

        return convertView;
    }
}
