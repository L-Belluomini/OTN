package com.atakmap.android.OTN.plugin;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Empty service to make the plugin visible to ATAK for package scanning.
 * This ensures ATAK can discover the plugin.xml asset in the APK.
 */
public class PluginService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
