package baidumapsdk.demo.demoapplication;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.os.Handler;
import android.util.Log;

public class ProgressReceiver extends ResultReceiver {

    public static final int STATUS_COMPLETE = 0;
    public static final int STATUS_FAILED = 1;
    private ProgressDialog dialog;
    private Context mContext;

    public ProgressReceiver(Handler handler) {
        super(handler);
    }

    private void ensureDialog() {
        if (dialog == null) {
            String title = mContext.getString(R.string.process_wait_title);
            String msg = mContext.getString(R.string.process_wait_msg);

            dialog = ProgressDialog.show(mContext, title, msg, true, true);
            dialog.setIcon(android.R.drawable.ic_dialog_info);
            dialog.setCanceledOnTouchOutside(false);
        }
    }

    public ProgressReceiver showDialog(Context context) {
        mContext = context;
        ensureDialog();
        dialog.show();
        return this;
    }


    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        boolean finished = false;
        switch (resultCode) {
            case STATUS_COMPLETE:
                finished = true;
                break;
            case STATUS_FAILED:
                finished = true;
                break;
        }

        if (finished && (dialog != null)) {
            dialog.hide();
            dialog.dismiss();
            dialog = null;
        }
    }
}
