package baidumapsdk.demo.demoapplication;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;

public class UpdateAppTask extends AsyncTask<String, Void, String> {
    private Context mContext = null;
    public static long mDownloadId;

    UpdateAppTask(Context context) {
        mContext = context;
    }

    @Override
    protected String doInBackground(String... urls) {
        String srcUrl = urls[0];

        DownloadManager dm = (DownloadManager) mContext
                .getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(srcUrl));
        mDownloadId = dm.enqueue(request);

        return null;
    }
}
