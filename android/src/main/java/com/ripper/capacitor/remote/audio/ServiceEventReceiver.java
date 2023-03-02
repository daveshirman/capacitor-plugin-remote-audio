package com.ripper.capacitor.remote.audio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.getcapacitor.Bridge;
import com.getcapacitor.JSObject;
import com.getcapacitor.PluginCall;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class ServiceEventReceiver extends BroadcastReceiver {

    private PluginCall call;
    private Bridge bridge;
    private boolean isPersistent;

    public ServiceEventReceiver(PluginCall call) {
        this.isPersistent = false;
        this.call = call;
    }

    public ServiceEventReceiver(Bridge bridge) {
        this.isPersistent = true;
        this.bridge = bridge;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // stop this firing again.
        if (!isPersistent) {
            LocalBroadcastManager.getInstance(context).unregisterReceiver(this);
        }

        String action = intent.getAction();

        assert action != null;
        if (action.equals(SharedConstants.UpdateAppUI_WasPaused)) {
            bridge.triggerWindowJSEvent(SharedConstants.UpdateAppUI_WasPaused);
        }

        if (action.equals(SharedConstants.UpdateAppUI_WasPlayed)) {
            bridge.triggerWindowJSEvent(SharedConstants.UpdateAppUI_WasPlayed);
        }

        if (action.equals(SharedConstants.DownloadMediaProgress)) {
            Object id = intent.getExtras().get("id");
            Object progress = intent.getExtras().get("progress");
            String data = String.format("{ 'id': '%s', 'progress': '%s' }", id.toString(), progress.toString());
            bridge.triggerWindowJSEvent(SharedConstants.DownloadMediaProgress, data);
        }




        if (call == null) { return; }

        Object result = intent.getExtras().get("result");

        if (action.equals(SharedConstants.GetMediaInfo)) {
            JSObject obj = new JSObject();
            obj.put("duration", result);

            JSObject ret = new JSObject();
            ret.put("info", obj);
            call.resolve(ret);
        }

        if (action.equals(SharedConstants.Play)) {
            JSObject ret = new JSObject();
            ret.put("isPlaying", result);
            call.resolve(ret);
        }

        if (action.equals(SharedConstants.GetCurrentPosition)) {
            JSObject ret = new JSObject();
            ret.put("position", result);
            call.resolve(ret);
        }

    }
}