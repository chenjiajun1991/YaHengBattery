package baidumapsdk.demo.demoapplication;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

public class DistributorInfoAdapter extends BaseAdapter {
    private Context mContext;
    private List<?> mItemList;
    private int mType;

    public DistributorInfoAdapter(Context context,
                          List<?> items, int type) {
        mContext = context;
        mItemList = items;
        mType = type;
    }

    @Override
    public int getCount() {
        return mItemList.size();
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
            convertView = View.inflate(mContext, R.layout.list_row_item,
                    null);
        }

/*
        final TextView itemSummary = (TextView)convertView.findViewById(R.id.item_summary);
        itemSummary.setVisibility(View.GONE);
*/

        final LinearLayout summaryItems =
                (LinearLayout) convertView.findViewById(R.id.summary_items);
        summaryItems.setVisibility(View.GONE);

        TextView item = (TextView) convertView.findViewById(R.id.item_header);
        CharSequence name = null;
        if (mType == 0) {
            name = (String) ((OverlayDemo.SimpleDisInfo)mItemList.get(position)).name;
        } else {
            name = "IMEI号：" + ((UserView.UserBatInfo)mItemList.get(position)).imei;
        }
        item.setText(name);

        return convertView;
    }

    public void addItemsToAdapterList(List<?> list) {
        if (mType == 0) {
            if (list != null && list.size() > 0) {
                List<OverlayDemo.SimpleDisInfo> tempList = (List<OverlayDemo.SimpleDisInfo>)list;
                for (OverlayDemo.SimpleDisInfo item : tempList) {
                    ((List<OverlayDemo.SimpleDisInfo>)mItemList).add(item);
                }
            }
        }
    }
}
