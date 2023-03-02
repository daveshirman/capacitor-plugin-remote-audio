package com.ripper.capacitor.remote.audio;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import android.Manifest;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.google.android.play.core.tasks.OnCompleteListener;
import com.google.android.play.core.tasks.OnFailureListener;
import com.google.android.play.core.tasks.OnSuccessListener;
import com.google.android.play.core.tasks.Task;

@CapacitorPlugin(name = "RemoteAudio")
public class RemoteAudioPlugin extends Plugin {

    final String PluginTag = "RemoteAudio:";
    static ServiceEventReceiver remoteAudioReceiver1 = null;
    static ServiceEventReceiver remoteAudioReceiver2 = null;
    static ServiceEventReceiver remoteAudioReceiver3 = null;
    List<DownloadFileAsync> downloaderTasks = null;

    @Override
    public void load() {
        if (remoteAudioReceiver1 == null) {
            remoteAudioReceiver1 = new ServiceEventReceiver(bridge);
            LocalBroadcastManager.getInstance(getContext()).registerReceiver(
                    remoteAudioReceiver1,
                    new IntentFilter(SharedConstants.UpdateAppUI_WasPlayed)
            );
        }

        if (remoteAudioReceiver2 == null) {
            remoteAudioReceiver2 = new ServiceEventReceiver(bridge);
            LocalBroadcastManager.getInstance(getContext()).registerReceiver(
                    remoteAudioReceiver2,
                    new IntentFilter(SharedConstants.UpdateAppUI_WasPaused)
            );
        }

        if (remoteAudioReceiver3 == null) {
            remoteAudioReceiver3 = new ServiceEventReceiver(bridge);
            LocalBroadcastManager.getInstance(getContext()).registerReceiver(
                    remoteAudioReceiver3,
                    new IntentFilter(SharedConstants.DownloadMediaProgress)
            );
        }
    }

    @PluginMethod
    public void setOrientation(PluginCall call) {
        Log.i(PluginTag, SharedConstants.SetOrientation);
        String orientation = call.getString("orientation");
        int orientationInt;
        switch (orientation) {
            case "unlocked":
                orientationInt = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
                break;
            case "landscape":
                orientationInt = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                break;
            default:
                orientationInt = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        }

        getActivity().setRequestedOrientation(orientationInt);

        call.resolve(null);
    }

    @PluginMethod
    public void getMediaInfo(PluginCall call) {
        try {
            Log.i(PluginTag, SharedConstants.GetMediaInfo);
            String title = call.getString("title");
            String url = call.getString("url");

            // Callback from service so we can return to JS...
            ServiceEventReceiver receiver = new ServiceEventReceiver(call);
            LocalBroadcastManager.getInstance(getContext()).registerReceiver(
                    receiver,
                    new IntentFilter(SharedConstants.GetMediaInfo)
            );

            // Start the foreground service + show required notification.
            Intent intent = new Intent(getContext(), MyForegroundService.class);
            intent.putExtra(MyForegroundService.EXTRA_Top_Level_Package_Name, getActivity().getPackageName());
            intent.putExtra("title", title);
            intent.putExtra("url", url);
            intent.setAction(MyForegroundService.ACTION_START);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                getContext().startForegroundService(intent);
            } else {
                getContext().startService(intent);
            }
        }
        catch (Exception e) {
            Log.i(PluginTag, "getMediaInfo failed");
            call.resolve(null);
        }
    }

    @PluginMethod
    public void play(PluginCall call) {
        try {
            Log.i(PluginTag, SharedConstants.Play);

            // Callback from service so we can return to JS...
            ServiceEventReceiver receiver = new ServiceEventReceiver(call);
            LocalBroadcastManager.getInstance(getContext()).registerReceiver(
                    receiver,
                    new IntentFilter(SharedConstants.Play)
            );

            Intent intent = new Intent(getContext(), MyForegroundService.class);
            intent.setAction(MyForegroundService.ACTION_PLAY);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                getContext().startForegroundService(intent);
            } else {
                getContext().startService(intent);
            }
        }
        catch (Exception e) {
            Log.i(PluginTag, "play failed");
            JSObject ret = new JSObject();
            ret.put("isPlaying", false);
            call.resolve(ret);
        }
    }

    @PluginMethod
    public void stop(PluginCall call) {
        // Stop the foreground service + remove the notification.
        Intent intent = new Intent(getContext(), MyForegroundService.class);
        intent.setAction(MyForegroundService.ACTION_STOP);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getContext().startForegroundService(intent);
        } else {
            getContext().startService(intent);
        }

        call.resolve();
    }

    @PluginMethod
    public void pause(PluginCall call) {
        try {
            String action = "pause";
            Log.i(PluginTag, action);

            Intent intent = new Intent(getContext(), MyForegroundService.class);
            intent.setAction(MyForegroundService.ACTION_PAUSE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                getContext().startForegroundService(intent);
            } else {
                getContext().startService(intent);
            }

            call.resolve();
        }
        catch (Exception e) {
            call.resolve();
        }
    }

    @PluginMethod
    public void seek(PluginCall call) {
        String action = "seek";
        Log.i(PluginTag, action);

        int milliseconds = call.getInt("milliseconds");

        Intent intent = new Intent(getContext(), MyForegroundService.class);
        intent.putExtra("milliseconds", milliseconds);
        intent.setAction(MyForegroundService.ACTION_SEEK);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getContext().startForegroundService(intent);
        } else {
            getContext().startService(intent);
        }

        call.resolve();
    }


    @PluginMethod
    public void getCurrentPosition(PluginCall call) {
        Log.i(PluginTag, SharedConstants.GetCurrentPosition);

        // Callback from service so we can return to JS...
        ServiceEventReceiver receiver = new ServiceEventReceiver(call);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(
                receiver,
                new IntentFilter(SharedConstants.GetCurrentPosition)
        );

        Intent intent = new Intent(getContext(), MyForegroundService.class);
        intent.setAction(MyForegroundService.ACTION_GET_CURRENT_POSITION);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getContext().startForegroundService(intent);
        } else {
            getContext().startService(intent);
        }
    }

    @PluginMethod
    public void checkOrRequestStoragePermissions(PluginCall call) {
        String TAG = "Storage Permission";
        Log.i(PluginTag, SharedConstants.CheckOrRequestStoragePermissions);

        boolean hasPermission = false;

        if (Build.VERSION.SDK_INT >= 23) {
            if (getContext().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                hasPermission = true;
            } else {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                hasPermission = false;
            }
        }
        else {
            // Permission is automatically granted on sdk<23 upon installation
            hasPermission = true;
        }

        JSObject ret = new JSObject();
        ret.put("hasStoragePermissions", hasPermission);
        call.resolve(ret);
    }

    @PluginMethod
    public void downloadMedia(PluginCall call) {
        try {
            Log.i(PluginTag, SharedConstants.DownloadMedia);
            String id = call.getString("id");
            String url = call.getString("url");
            String folderName = call.getString("folderName");

            if (downloaderTasks == null) {
                downloaderTasks = new ArrayList<>();
            }

            ListIterator<DownloadFileAsync> iter = downloaderTasks.listIterator();
            while (iter.hasNext()) {
                if (!iter.next().getIsRunning()) {
                    iter.remove();
                }
            }

            Integer newTaskId = (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE);
            DownloadFileAsync newTask = new DownloadFileAsync(getContext(), newTaskId, id, url, folderName);
            downloaderTasks.add(newTask);
            newTask.execute();

            call.resolve(null);
        }
        catch (Exception e) {
            Log.i(PluginTag, "downloadMedia failed");
            call.resolve(null);
        }
    }

    @PluginMethod
    public void deleteDownload(PluginCall call) {
        try {
            Log.i(PluginTag, SharedConstants.DeleteDownload);
            String id = call.getString("id");
            String folderName = call.getString("folderName");

            File cacheDir = new File(android.os.Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "/" + folderName + "/");
            File filesList[] = cacheDir.listFiles();
            for (File file : filesList) {
                if (file.isFile() && file.getName().indexOf(id) > -1) {
                    file.delete();
                    call.resolve(null);
                }
            }

            call.resolve(null);
        }
        catch (Exception e) {
            Log.i(PluginTag, "deleteDownload failed");
            call.resolve(null);
        }
    }

    @PluginMethod
    public void deleteAllDownloads(PluginCall call) {
        try {
            Log.i(PluginTag, SharedConstants.DeleteAllDownloads);
            String folderName = call.getString("folderName");

            File cacheDir = new File(android.os.Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "/" + folderName + "/");
            File filesList[] = cacheDir.listFiles();
            for (File file : filesList) {
                if(file.isFile()) {
                    file.delete();
                }
            }
            cacheDir.delete();
            call.resolve(null);
        }
        catch (Exception e) {
            Log.i(PluginTag, "deleteAllDownloads failed");
            call.resolve(null);
        }
    }



    @PluginMethod
    public void cancelDownload(PluginCall call) {
        try {
            Log.i(PluginTag, SharedConstants.CancelDownload);
            String id = call.getString("id");

            DownloadFileAsync foundTask = null;
            for (DownloadFileAsync task : downloaderTasks) {
                if (task.getId().equals(id)) {
                    foundTask = task;
                    break;
                }
            }
            if (foundTask != null) {
                foundTask.cancel(true);
                downloaderTasks.remove(foundTask);
                call.resolve(null);
            }
            else {
                call.resolve(null);
            }
        }
        catch (Exception e) {
            Log.i(PluginTag, "cancelDownload failed");
            call.resolve(null);
        }
    }

    @PluginMethod()
    public void showRateApp(final PluginCall call)
    {
        final ReviewManager manager = ReviewManagerFactory.create(getContext());
        final AppCompatActivity activity = getActivity();
        Task<ReviewInfo> request = manager.requestReviewFlow();
        request.addOnFailureListener(e -> {
            e.printStackTrace();
            call.reject("Request Failed", e);
        });
        request.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // We can get the ReviewInfo object
                ReviewInfo reviewInfo = task.getResult();
                Task<Void> flow = manager.launchReviewFlow(activity, reviewInfo);
                flow.addOnCompleteListener(task1 -> {
                    // The flow has finished. The API does not indicate whether the user
                    // reviewed or not, or even whether the review dialog was shown. Thus, no
                    // matter the result, we continue our app flow.
                    call.resolve();
                });
                flow.addOnSuccessListener(result -> call.resolve());
                flow.addOnFailureListener(e -> {
                    e.printStackTrace();
                    call.reject("Flow Failed", e);
                });
            }
            else {
                // There was some problem, continue regardless of the result.
                call.reject("Task Failed");
            }
        });
    }
}