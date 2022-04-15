
package com.atakmap.android.OTN;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.atakmap.android.OTN.router.OTNOfflineRouter;
import com.atakmap.android.ipc.AtakBroadcast;
import com.atakmap.android.ipc.AtakBroadcast.DocumentedIntentFilter;

import com.atakmap.android.maps.MapActivity;
import com.atakmap.android.maps.MapComponent;
import com.atakmap.android.maps.MapView;
import com.atakmap.android.dropdown.DropDownMapComponent;


import com.atakmap.android.preference.AtakPreferences;
import com.atakmap.android.routes.RoutePlannerInterface;
import com.atakmap.android.user.geocode.GeocodeManager;
import com.atakmap.coremap.filesystem.FileSystemUtils;
import com.atakmap.coremap.io.IOProvider;
import com.atakmap.coremap.io.IOProviderFactory;
import com.atakmap.coremap.log.Log;
import com.atakmap.android.OTN.plugin.R;

import com.atakmap.android.routes.RoutePlannerManager;
import com.atakmap.android.routes.RouteMapComponent;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.graphhopper.GraphHopperConfig;

import java.io.FileReader;
import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class OTNMapComponent extends DropDownMapComponent {

    private static final String TAG = "OTNMapComponent";
    private Context pluginContext;
    private Context _context;
    private OTNDropDownReceiver ddr;
    private MapComponent mc;
    private RoutePlannerManager _routeManager ;
    private LinkedList<String> registeredRouters = new LinkedList<>();
    private LinkedList<OTNGraph> graphs = new LinkedList<>();
    private OTNGraph selectdeGraph;
    private OTNGraph tmpGraph;
    private AtakPreferences _prefs;


    public OTNMapComponent(){    }



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

        // prepare to set the routers
        _context = view.getContext();
        mc = ( (MapActivity) _context )
                .getMapComponent(RouteMapComponent.class);

        _routeManager = mc != null
                ? ((RouteMapComponent) mc)
                .getRoutePlannerManager()
                :null;
        assert _routeManager != null;

        _prefs = new AtakPreferences(view);

        Log.d(TAG, "registering the plugin filter");
        DocumentedIntentFilter ddFilter = new DocumentedIntentFilter();
        ddFilter.addAction(OTNDropDownReceiver.SHOW_PLUGIN);
        ddFilter.addAction(OTNDropDownReceiver.SET_GRAPHS);
        registerDropDownReceiver(ddr, ddFilter);

        final BroadcastReceiver selectegraphReciver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();
                if (action == null)
                    return;

                switch (action) {
                    case (OTNDropDownReceiver.SET_GRAPHS):
                        Bundle graphBundle = intent.getBundleExtra("GRAPH");
                        if (graphBundle == null) {
                            Log.w(TAG,"failled setting bundle");
                            return;

                        }
                        tmpGraph =  (OTNGraph) graphBundle.getSerializable("GRAPH");
                        Log.d(TAG , tmpGraph.toString());
                        if ( tmpGraph == null ) {
                            Log.w(TAG,"failled importing graph list");
                            return;
                        }
                        updateRouters();

                        break;
                    case (OTNDropDownReceiver.FIND_GRAPHS):
                        findnSetGraphs();
                        pushGraphs();

                        break;
                }
            }
        };
        DocumentedIntentFilter mcFilter = new DocumentedIntentFilter();
        mcFilter.addAction(OTNDropDownReceiver.FIND_GRAPHS);
        mcFilter.addAction(OTNDropDownReceiver.SET_GRAPHS);
        AtakBroadcast.getInstance().registerReceiver(selectegraphReciver, mcFilter );

        // check if OTN exist
        if ( checkOTNFolder()) {
            // otn exist
            findnSetGraphs();
        } else {
            Log.w(TAG , "no folder found");
            Toast toast = Toast.makeText(context, "NO OTN folder FOUND", Toast.LENGTH_LONG); // check right context
            toast.show();
            return;
        }

        // retrive last selceted graph if present and unchanged
        for (OTNGraph tmpGraph : graphs ) {
            if ( tmpGraph.getEdgeHash().equals( _prefs.get("OTNSelectedGraph","") ) ){
                selectdeGraph = tmpGraph;
            }
        }

        if (selectdeGraph == null){
            if (graphs.size() > 1 ) {
                return;
            }
            // if there only one graph set as selected
            selectdeGraph = graphs.get(0);
        }

        pushGraphs();

        if ( this.graphs.isEmpty( ) ) {
        Log.w(TAG , "no graph found");
        Toast toast = Toast.makeText(context, "NO OTN graph FOUND", Toast.LENGTH_LONG); // check right context
        toast.show();
        return;
        }

        //add offline geocorder
        GeocodeManager _geocoderManager = GeocodeManager.getInstance(_context);
        _geocoderManager.registerGeocoder( new OTNOfflineGeocoder( graphs) );

        _routeManager.registerPlanner ( "OTNOFFlineFast", new OTNOfflineRouter( pluginContext , selectdeGraph , OTNrequest.ProfileType.BEST ) );
        _prefs.set( "OTNSelectedGraph" , selectdeGraph.getEdgeHash() );
        registeredRouters.add( "OTNOFFlineFast" );
        Log.d(TAG, "registered route planner: " + "OTNOFFlineFast" );
        // register other route planners
        // querry serivce db and create realtive routers
    }

    @Override
    protected void onDestroyImpl(Context context, MapView view) {
        super.onDestroyImpl(context, view);
        for ( String router : registeredRouters) {
            _routeManager.unregisterPlanner(router);

        }

    }
    protected void updateRouters() {
        Log.d(TAG,"updating router");
        for (String routerUID : registeredRouters ){
            RoutePlannerInterface tmpPlanner = _routeManager.getPlanner( routerUID );
            if ( tmpPlanner instanceof OTNOfflineRouter  ){
                _routeManager.unregisterPlanner(routerUID);
            }
        }
        selectdeGraph = tmpGraph;
        tmpGraph = null;
        // add routr/routers
        _routeManager.registerPlanner ( "OTNOFFlineFast", new OTNOfflineRouter( pluginContext , selectdeGraph , OTNrequest.ProfileType.BEST ) );
        registeredRouters.add( "OTNOFFlineFast" );

    }

    protected void pushGraphs(){
        Intent i = new Intent(
                OTNDropDownReceiver.SET_GRAPHS);
        Bundle bundleGraphs = new Bundle();
        bundleGraphs.putSerializable("GRAPHS" ,(Serializable) this.graphs );
        Log.d(this.TAG , bundleGraphs.toString());
        i.putExtra("GRAPHS", bundleGraphs );

        Bundle bundleGraph = new Bundle();
        bundleGraph.putSerializable ("GRAPH" , (Serializable) this.selectdeGraph);
        i.putExtra("GRAPH" , bundleGraph);
        AtakBroadcast.getInstance().sendBroadcast(i);
        Log.d(TAG , "sent broadcast setgraphs");
    }
    protected void findnSetGraphs(){
        IOProvider provider = IOProviderFactory.getProvider();
        String [] grapfsFolder = provider.list ( FileSystemUtils.getItem (FileSystemUtils.TOOL_DATA_DIRECTORY  + "/OTN" + "/graphs" ) );
        Log.d ( TAG , "array dirs: " + Arrays.toString(grapfsFolder));
        if ( grapfsFolder.length == 0 ){
            return;
        }
        OTNGraph tmp ;
        boolean flag;
        for (String dir : grapfsFolder ) {
            tmp = getGraphFromDir(dir);
             flag = true;
            if (tmp != null) {
                for (OTNGraph element : graphs ) {
                    if (tmp.getEdgeHash().equals(element.getEdgeHash())) {
                        flag = false;
                        break;
                    }
                }
                if ( flag ) {
                    this.graphs.add(tmp);
                }

            }
        }
    }
    protected OTNGraph getGraphFromDir (String dir){
        GraphHopperConfig tmpConfig = null;
        try {
            Gson ason = new Gson();
            FileReader fReader =  new FileReader( FileSystemUtils.getItem(FileSystemUtils.TOOL_DATA_DIRECTORY  + "/OTN/graphs/" + dir + "/config.json") );
            JsonReader reader = new JsonReader(fReader);
            tmpConfig = ason.fromJson (reader , GraphHopperConfig.class );
            if ( tmpConfig == null ) {
                Log.w(TAG , "reading j config failed");
               return null;
            }
        } catch ( Exception e) {
            Log.e( TAG ,"An error occurred, reading " );
            Log.e ( TAG , e.toString());
        }
        return new OTNGraph("/OTN/graphs/" + dir , tmpConfig );
    }

    protected static boolean checkOTNFolder() {
        IOProvider provider = IOProviderFactory.getProvider();
        return provider.exists( FileSystemUtils.getItem (FileSystemUtils.TOOL_DATA_DIRECTORY  + "/OTN") );
    }

}
