
package com.atakmap.android.OTN.plugin;

import com.atakmap.android.OTN.OTNMapComponent;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.atak.plugins.impl.AbstractPluginTool;
import com.atakmap.android.ipc.AtakBroadcast;
import com.atakmap.android.navigation.NavButtonManager;
import com.atakmap.android.navigation.models.NavButtonModel;
import gov.tak.api.util.Disposable;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.ViewGroup;
import com.atakmap.android.maps.MapView;
// import com.atakmap.coremap.log.Log;
// import transapps.mapi.MapView; // deprecated
// import transapps.maps.plugin.tool.Group;
// import transapps.maps.plugin.tool.Tool;
// import transapps.maps.plugin.tool.ToolDescriptor;

public class OTNTool extends AbstractPluginTool implements Disposable{

    private final  String TAG = "OTNtool";

    //private final Context context;

    public OTNTool(final Context context) {
        super(context,
                context.getString(R.string.app_name),
                context.getString(R.string.app_desc),
                context.getResources().getDrawable(R.drawable.otn_logo),
                OTNMapComponent.SHOW_PLUGIN);

        /*AtakBroadcast.getInstance().registerReceiver(br,
                new AtakBroadcast.DocumentedIntentFilter(
                        "\"com.atakmap.android.OTNMapComponent.SHOW_PLUGIN"));

         */
    }

   /* private final BroadcastReceiver br = new BroadcastReceiver() {
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
                Log.d(TAG, "increment visual count to: " + count);
            }
        }
    };

    */

    @Override
    public void dispose() {
        //AtakBroadcast.getInstance().unregisterReceiver(br);
    }

    // public OTNTool(Context context) {this.context = context;}

    /*
    @Override
    public String getDescription() {
        return context.getString(R.string.app_name);
    }

    @Override
    public Drawable getIcon() {
        return (context == null) ? null
                : context.getResources().getDrawable(R.drawable.otn_logo);
    }

    @Override
    public Group[] getGroups() {
        return new Group[] {
                Group.GENERAL
        };
    }

    @Override
    public String getShortDescription() {
        return context.getString(R.string.app_name);
    }

    @Override
    public Tool getTool() {
        return this;
    }



    @Override
    public void onActivate(Activity arg0, transapps.mapi.MapView oldMapView, ViewGroup arg2,
            Bundle arg3,
            ToolCallback arg4) {
        MapView mapView;

        // hack to updated API
        if (oldMapView == null || !(oldMapView.getView() instanceof MapView)) {
            Log.w(TAG, "This plugin is only compatible with ATAK MapView");
            return;
        }
        mapView = (MapView) oldMapView.getView();

        // Hack to close the dropdown that automatically opens when a tool
        // plugin is activated.
        if (arg4 != null) {
            arg4.onToolDeactivated(this);
        }
        // Intent to launch the dropdown or tool // IMPORTANT BIT //

        //arg2.setVisibility(ViewGroup.INVISIBLE);
        Intent i = new Intent(
                OTNMapComponent.SHOW_PLUGIN);
        AtakBroadcast.getInstance().sendBroadcast(i);
    }

    @Override
    public void onDeactivate(ToolCallback arg0) {}
    */
}
