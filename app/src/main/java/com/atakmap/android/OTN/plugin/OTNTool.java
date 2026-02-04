
package com.atakmap.android.OTN.plugin;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.atak.plugins.impl.AbstractPluginTool;
import com.atakmap.android.OTN.OTNMapComponent;
import com.atakmap.android.ipc.AtakBroadcast;
import com.atakmap.android.navigation.NavButtonManager;
import com.atakmap.android.navigation.models.NavButtonModel;
import com.atakmap.coremap.log.Log;

import gov.tak.api.util.Disposable;

/**
 * OTN Plugin Tool for ATAK 5.6.0
 * Updated from Tool/ToolDescriptor to AbstractPluginTool for new plugin API
 */
public class OTNTool extends AbstractPluginTool implements Disposable {

    private final String TAG = "OTNTool";

    /**
     * Constructor for ATAK 5.x plugin tool
     * @param context The plugin context
     */
    public OTNTool(final Context context) {
        super(context,
                context.getString(R.string.app_name),
                context.getString(R.string.app_desc),
                context.getResources().getDrawable(R.drawable.otn_logo),
                OTNMapComponent.SHOW_PLUGIN);

        Log.d(TAG, "OTN Tool initialized for ATAK 5.6.0");

        // Register broadcast receiver for icon badge count updates
        AtakBroadcast.getInstance().registerReceiver(badgeCountReceiver,
                new AtakBroadcast.DocumentedIntentFilter(
                        "com.atakmap.android.OTN.plugin.iconcount"));
    }

    /**
     * Broadcast receiver for updating plugin icon badge count
     */
    private final BroadcastReceiver badgeCountReceiver = new BroadcastReceiver() {
        private int count = 0;

        @Override
        public void onReceive(Context c, Intent intent) {
            // Get the button model used by this plugin
            NavButtonModel mdl = NavButtonManager.getInstance()
                    .getModelByPlugin(OTNTool.this);
            if (mdl != null) {
                // Increment the badge count and refresh
                mdl.setBadgeCount(++count);
                NavButtonManager.getInstance().notifyModelChanged(mdl);
                Log.d(TAG, "Updated badge count to: " + count);
            }
        }
    };

    /**
     * Clean up resources when plugin is disposed
     */
    @Override
    public void dispose() {
        AtakBroadcast.getInstance().unregisterReceiver(badgeCountReceiver);
        Log.d(TAG, "OTN Tool disposed");
    }
}
