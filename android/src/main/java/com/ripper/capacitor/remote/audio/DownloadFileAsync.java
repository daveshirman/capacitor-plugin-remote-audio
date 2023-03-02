package com.ripper.capacitor.remote.audio;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;


class DownloadFileAsync extends AsyncTask<String, String, String> {

    private Context context;

    private Integer taskId; // this task
    private String id;  // file id at Firebase
    private String urlToDownload;   // url of resource
    private String downloadFolderName;  // where to save
    private Integer lastDownloadProgress;
    private boolean running = true;
    private File localFile;

    private static final String NOTIFICATION_CHANNEL_ID = "com.ripper.capacitor.remote.audio.downloads";
    private static final String NOTIFICATION_CHANNEL_NAME = "AOM Downloads";
    private static final String NOTIFICATION_CHANNEL_DESCRIPTION = "For showing downloads in progress";

    public DownloadFileAsync(Context context, Integer newTaskId, String id, String urlToDownload, String downloadFolderName) {
        this.context = context;
        this.taskId = newTaskId;
        this.id = id;
        this.urlToDownload = urlToDownload;
        this.downloadFolderName = downloadFolderName;
    }

    public String getId() {
        return id;
    }

    public boolean getIsRunning() {
        return running;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }

    @Override
    protected void onCancelled() {
        running = false;
        deleteLocalFile();
        NotificationManagerCompat.from(context).cancel(taskId);
    }

    private void deleteLocalFile() {
        if (localFile == null) {
            setLocalFileReference();
        }

        if (localFile != null) {
            try {
                localFile.delete();
                localFile = null;
            }
            catch (Exception e) {
                Log.e("onCancelled err", e.getMessage());
            }
        }
    }

    private String getExtension(String url) {
        if (url != null && url.length() > 0) {
            return url.substring(url.lastIndexOf(".") + 1);
        }
        return null;
    }

    @Override
    protected String doInBackground(String... urlParam) {
        int count;
        try {
            if (checkOrRequestStoragePermissions()) {
                setLocalFileReference();

                if (localFile != null) {
                    localFile.createNewFile();
                }
                else {
                    return null;
                }

                URL url = new URL(urlToDownload);
                URLConnection connection = url.openConnection();
                connection.connect();
                int lengthOfFile = connection.getContentLength();

                this.lastDownloadProgress = 0;
                InputStream input = new BufferedInputStream(url.openStream());
                OutputStream output = new FileOutputStream(localFile);
                byte data[] = new byte[1024];
                long total = 0;

                // Show downloading notification.
                createNotificationChannel();
                NotificationCompat.Builder builder = buildDownloadingNotification(0);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
                    notificationManager.notify(taskId, builder.build());
                }

                // Trap if the task is cancelled.
                while (running && (count = input.read(data)) != -1) {
                    total += count;
                    publishProgress("" + (int) ((total * 100) / lengthOfFile));
                    output.write(data, 0, count);
                }

                output.flush();
                output.close();
                input.close();
            }
        }
        catch (Exception e) {
            Log.e("DownloadFileAsync err", e.getMessage());
        }


        return null;
    }

    private void setLocalFileReference() {
        File cacheDir = new File(android.os.Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "/" + this.downloadFolderName + "/");

        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
        String fileExtension = getExtension(urlToDownload);
        if (fileExtension == null) { return; }
        localFile = new File(cacheDir, id + "." + fileExtension);
    }

    protected void onProgressUpdate(String... progress) {
        Integer progressParsed = Integer.parseInt(progress[0]);
        Log.d("DownloadFileAsync onProgressUpdate", "Progress: " + progressParsed);

        if (progressParsed > this.lastDownloadProgress) {
            this.lastDownloadProgress = progressParsed;

            if (progressParsed == 100) {
                running = false;
                NotificationManagerCompat.from(context).cancel(taskId);

                Intent intent = new Intent(SharedConstants.DownloadMediaProgress);
                intent.putExtra("id", id);
                intent.putExtra("progress", progressParsed);
                LocalBroadcastManager.getInstance(this.context).sendBroadcast(intent);
            }
            else {
                NotificationCompat.Builder builder = buildDownloadingNotification(lastDownloadProgress);
                NotificationManagerCompat.from(context).notify(taskId, builder.build());
            }
        }
    }

    @Override
    protected void onPostExecute(String unused) {

    }


    private boolean checkOrRequestStoragePermissions() {
        String TAG = "Storage Permission";
        if (Build.VERSION.SDK_INT >= 23) {
            if (this.context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                ActivityCompat.requestPermissions((Activity)this.context, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else {
            return true;
        }
    }


    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription(NOTIFICATION_CHANNEL_DESCRIPTION);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private NotificationCompat.Builder buildDownloadingNotification(Integer progress) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setOngoing(false)
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setShowWhen(false)
                .setSmallIcon(getNotificationIcon())
                .setContentTitle("Downloading: " + progress.toString() + "%")
                .setProgress(100, progress, false);

        return builder;
    }


    private int getNotificationIcon() {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("notificationlogo", "drawable",
                context.getPackageName());
        return resourceId;
    }


}
