package baidumapsdk.demo.demoapplication;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.List;

public abstract class UtilityDialog {
    private List<?> mItems;
    private Context mContext;
    private int mFriendPos;

    public UtilityDialog(Context context, List<?> items, int friendPos) {
        mItems = items;
        mContext = context;
        mFriendPos = friendPos;
    }

    public void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(getTitle());

        ViewGroup contentView = (ViewGroup) View.inflate(mContext, R.layout.picker_list, null);
        final ListView itemList = (ListView)contentView.findViewById(android.R.id.list);

        builder.setView(contentView);
        builder.setPositiveButton(getPositiveButtonText(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                handleButtonClick();
                dialogInterface.dismiss();
            }
        });

        final Dialog dialog = builder.create();
        itemList.setAdapter(new BatteryAdapter(mContext, mItems, false, mFriendPos, true));
        itemList.setChoiceMode(ListView.CHOICE_MODE_NONE);
        dialog.show();
    }

    public abstract String getTitle();
    public abstract String getPositiveButtonText();
    public abstract void handleButtonClick();
}
