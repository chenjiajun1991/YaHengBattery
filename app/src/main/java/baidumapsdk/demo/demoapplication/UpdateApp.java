package baidumapsdk.demo.demoapplication;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import java.io.File;

public class UpdateApp extends ActionBarActivity {
    private BroadcastReceiver mGetAppUpdateReceiver = null;
    private MyProgressDialog dialog = null;
    private boolean mFirstBackPressed = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (mGetAppUpdateReceiver == null) {
            mGetAppUpdateReceiver = new GetAppUpdateVersion();
            IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
            registerReceiver(mGetAppUpdateReceiver, filter);
        }
    }

    @Override
    protected void onDestroy() {
        if (mGetAppUpdateReceiver != null) {
            unregisterReceiver(mGetAppUpdateReceiver);
        }
        super.onDestroy();
    }

    @Override
    protected void onResume()
    {
        String apkUrl = getIntent().getStringExtra("apkUrl");
        if (apkUrl != null) {
            new UpdateAppTask(UpdateApp.this).execute(apkUrl);
        }
        ensureDialog();
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void ensureDialog() {
        if (dialog == null) {
            String title = getString(R.string.process_wait_title);
            String msg = getString(R.string.update_app_download);

            //dialog = ProgressDialog.show(UpdateApp.this, title, msg, true, true);
            dialog = new MyProgressDialog(UpdateApp.this);
            dialog.setIcon(android.R.drawable.ic_dialog_info);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setTitle(title);
            dialog.setMessage(msg);
            dialog.show();
        }
    }

    private void dismissProgressDialog() {
        if (dialog != null) {
            dialog.hide();
            dialog.dismiss();
            dialog = null;
        }
    }

    public void AlertDialogShow(String message) {
        AlertDialog.Builder builder =  new AlertDialog.Builder(UpdateApp.this);
        builder.setTitle("提示")
                .setMessage(message)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        onBackPressed();
                    }
                })
                .setCancelable(true);
        Dialog dialog = builder.create();
        dialog.show();
    }

    class GetAppUpdateVersion extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(intent.getAction())) {
                dismissProgressDialog();
                DownloadManager.Query q = new DownloadManager.Query();
                q.setFilterById(UpdateAppTask.mDownloadId);
                DownloadManager dm = (DownloadManager) UpdateApp.this
                        .getSystemService(Context.DOWNLOAD_SERVICE);
                Cursor c = dm.query(q);
                if (c.moveToFirst()) {
                    int columnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
                    if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(columnIndex)) {
                        String location = c.getString(
                                c.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME));
                        File apkInstallFile = new File(location);
                        Uri apkUri = Uri.fromFile(apkInstallFile);
                        Intent launch = new Intent(Intent.ACTION_VIEW);
                        launch.setDataAndType(apkUri,
                                "application/vnd.android.package-archive");
                        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        UpdateApp.this.startActivity(launch);
                        finish();

                    } else if (DownloadManager.STATUS_FAILED == c.getInt(columnIndex)){
                        Toast.makeText(getApplicationContext(),
                                "下载更新失败！",
                                Toast.LENGTH_SHORT).show();
                    }
                    c.close();
                }
            }
        }
    }

    class MyProgressDialog extends ProgressDialog {
        MyProgressDialog(Context context){ super(context);}

        @Override
        public void onBackPressed() {
            dismissProgressDialog();
            AlertDialogShow(getString(R.string.update_exit_confirm));
        }
    }
}
