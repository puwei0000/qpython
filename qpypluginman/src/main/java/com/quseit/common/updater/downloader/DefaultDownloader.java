package com.quseit.common.updater.downloader;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.FileDownloader;
import com.quseit.common.updater.R;

import java.io.File;

import static android.os.Environment.getExternalStoragePublicDirectory;

public class DefaultDownloader implements Downloader {
    public static final String TAG = "DefaultDownloader";
    public final String DEFAULT_PATH;
    private final Context context;
    private final NotificationManager notificationManager;

    public DefaultDownloader(Context context) {
        this.context = context;
        FileDownloader.init(context);
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        DEFAULT_PATH = getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
    }

    @Override
    public void download(final String name, String url, final Callback callback) {
        String path = DEFAULT_PATH + "/" + name;
        download(name, url, path, callback);
    }

    @Override
    public void download(final String name, String url, final String path, final Callback callback) {
        Log.d(TAG, "download:"+name+":"+url+"["+path+"]");
        FileDownloader.getImpl()
        .create(url)
        .setPath(path, false)
        .setCallbackProgressTimes(2000)
        .setListener(new FileDownloadListener() {
            @Override
            protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                Log.d(TAG, "download:pending:");
                callback.pending(name);
            }

            @Override
            protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                Log.d(TAG, "download:progress:");

                Notification notification = new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_cloud_download_black_24dp)
                        .setContentTitle(context.getText(R.string.downloading))
                        .setContentText(name)
                        .setProgress(totalBytes, soFarBytes, false)
                        .build();
                notificationManager.notify(name.hashCode(), notification);
            }

            @Override
            protected void completed(BaseDownloadTask task) {
                Log.d(TAG, "download:completed:");

                //notificationManager.cancel(name.hashCode());
                Notification notification = new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_cloud_download_black_24dp)
                        .setContentTitle(context.getText(R.string.downloaded))
                        .setContentText(name)
                        .setProgress(100, 100, false)
                        .build();

                notificationManager.notify(name.hashCode(),notification);
                File file = new File(task.getTargetFilePath());
                callback.complete(name, file);
            }

            @Override
            protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                Log.d(TAG, "download:paused:");

            }

            @Override
            protected void error(BaseDownloadTask task, Throwable e) {
                Log.d(TAG, "download:error:"+e.getLocalizedMessage());
                callback.error(e.getLocalizedMessage());

            }

            @Override
            protected void warn(BaseDownloadTask task) {
                Log.d(TAG, "download:warn:");

            }
        })
        .start();

    }
}