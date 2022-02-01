
package com.atakmap.android.OTN;


import android.content.Context;
import android.content.Intent;
import com.atakmap.android.ipc.AtakBroadcast.DocumentedIntentFilter;

import com.atakmap.android.maps.MapActivity;
import com.atakmap.android.maps.MapComponent;
import com.atakmap.android.maps.MapView;
import com.atakmap.android.dropdown.DropDownMapComponent;


import com.atakmap.coremap.log.Log;
import com.atakmap.android.OTN.plugin.R;

import com.atakmap.android.routes.RoutePlannerManager;
import com.atakmap.android.routes.RouteMapComponent;

public class OTNMapComponent extends DropDownMapComponent {

    private static final String TAG = "OTNMapComponent";

    private Context pluginContext;

    private OTNDropDownReceiver ddr;

    private MapComponent mc;
    private RoutePlannerManager _routeManager ;
    private Context _context;

    public OTNRouter OTNrouter;
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


        _context = view.getContext();
        mc = ( (MapActivity) _context )
                .getMapComponent(RouteMapComponent.class);

        _routeManager = mc != null
                ? ((RouteMapComponent) mc)
                    .getRoutePlannerManager()
                :null;
        assert _routeManager != null;
        OTNrouter = new OTNRouter();
        _routeManager.registerPlanner("OTN" , OTNrouter);
    }

    @Override
    protected void onDestroyImpl(Context context, MapView view) {
        super.onDestroyImpl(context, view);
    }

}
