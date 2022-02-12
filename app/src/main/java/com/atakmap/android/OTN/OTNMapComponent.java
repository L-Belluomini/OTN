
package com.atakmap.android.OTN;


import android.content.Context;
import android.content.Intent;
import com.atakmap.android.ipc.AtakBroadcast.DocumentedIntentFilter;

import com.atakmap.android.maps.MapActivity;
import com.atakmap.android.maps.MapComponent;
import com.atakmap.android.maps.MapView;
import com.atakmap.android.dropdown.DropDownMapComponent;


import com.atakmap.coremap.filesystem.FileSystemUtils;
import com.atakmap.coremap.io.IOProviderFactory;
import com.atakmap.coremap.log.Log;
import com.atakmap.android.OTN.plugin.R;

import com.atakmap.android.routes.RoutePlannerManager;
import com.atakmap.android.routes.RouteMapComponent;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.graphhopper.GraphHopperConfig;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileSystem;

public class OTNMapComponent extends DropDownMapComponent {
    private GraphHopperConfig jConfig;
    private String cacheLoc = "/sdcard/atak/tools/OTN/cache"; // todo get from shared preference and setter from gui

    public OTNMapComponent(){


    }

    private static final String TAG = "OTNMapComponent";

    private Context pluginContext;

    private OTNDropDownReceiver ddr;

    private MapComponent mc;
    private RoutePlannerManager _routeManager ;
    private Context _context;


    @Override
    public void onStart(final Context context, final MapView view) {
        Log.d(TAG, "onStart");
    }

    @Override
    public void onPause(final Context context, final MapView view) {
        Log.d(TAG, "onPause");
    }

    @Override
    public void onResume(final Context context,
                         final MapView view) {
        Log.d(TAG, "onResume");

        if (jConfig == null){
            Log.w("OTN" , "reading j config failed");
        }
    }

    @Override
    public void onStop(final Context context,
                       final MapView view) {
        Log.d(TAG, "onStop");
    }

    public void onCreate(final Context context, Intent intent,
            final MapView view) {

        context.setTheme(R.style.ATAKPluginTheme);
        super.onCreate(context, intent, view);
        pluginContext = context;

        ddr = new OTNDropDownReceiver(
                view, context);

        Log.d(TAG, "registering the plugin filter");
        DocumentedIntentFilter ddFilter = new DocumentedIntentFilter();
        ddFilter.addAction(OTNDropDownReceiver.SHOW_PLUGIN);
        registerDropDownReceiver(ddr, ddFilter);

        //up as from template
        try {
            Gson ason = new Gson();
            FileSystemUtils.init();
            FileReader fReader =  new FileReader( FileSystemUtils.getItem(FileSystemUtils.TOOL_DATA_DIRECTORY  + "/OTN" + "/cache" + "/config.json") );
            JsonReader reader = new JsonReader(fReader);
            jConfig = ason.fromJson (reader , GraphHopperConfig.class );
            Log.d(TAG , "jConfig: " + jConfig.toString());
            if (jConfig == null){
                Log.w(TAG , "reading j config failed");
            }
        } catch (IOException e) {
            Log.e( TAG ,"An error occurred, reading " );
            Log.e ( TAG , e.toString());
        }


        _context = view.getContext();
        mc = ( (MapActivity) _context )
                .getMapComponent(RouteMapComponent.class);

        _routeManager = mc != null
                ? ((RouteMapComponent) mc)
                    .getRoutePlannerManager()
                :null;
        assert _routeManager != null;
        _routeManager.registerPlanner ( "OTNOffline" , new OTNOfflineRouter( jConfig , pluginContext ) );
    }

    @Override
    protected void onDestroyImpl(Context context, MapView view) {
        _routeManager.unregisterPlanner("OTNOffline");
        super.onDestroyImpl(context, view);
    }

}
