
package com.atakmap.android.OTN;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.atakmap.android.ipc.AtakBroadcast;
import com.atakmap.android.ipc.AtakBroadcast.DocumentedIntentFilter;

import com.atakmap.android.maps.MapActivity;
import com.atakmap.android.maps.MapComponent;
import com.atakmap.android.maps.MapView;
import com.atakmap.android.dropdown.DropDownMapComponent;


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

public class OTNMapComponent extends DropDownMapComponent {

    private static final String TAG = "OTNMapComponent";
    private Context pluginContext;
    private Context _context;
    private OTNDropDownReceiver ddr;
    private MapComponent mc;
    private RoutePlannerManager _routeManager ;

    private static OTNMapComponent _instance;
    private LinkedList<String> registerdRouters = new LinkedList<>();
    private GraphHopperConfig selectedConfig;
    private String selectedGraphDir;
    // graphs dir & config list are "synced"
    private LinkedList<OTNGraph> graphs = new LinkedList<>();
    private LinkedList <GraphHopperConfig> configList = new LinkedList<>() ;
    private  LinkedList <String> graphsDir = new LinkedList<>() ;

    public OTNMapComponent(){


    }



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
        _instance = this;
        pluginContext = context;

        ddr = new OTNDropDownReceiver(
                view, context);

        Log.d(TAG, "registering the plugin filter");
        DocumentedIntentFilter ddFilter = new DocumentedIntentFilter();
        ddFilter.addAction(OTNDropDownReceiver.SHOW_PLUGIN);
        ddFilter.addAction(OTNDropDownReceiver.SET_GRAPHS); // added
        registerDropDownReceiver(ddr, ddFilter);



        // check if OTN exist
        if ( checkOTNFolder()) {
            // otn exist
            findnSetGraphs();
        } else {
            //
            Toast toast = Toast.makeText(context , "NO graph FOUND" , Toast.LENGTH_LONG ); // check right context
            toast.show();
            return;
        }
        ////////
        selectedConfig = graphs.get(0).getConfigGH();
        selectedGraphDir =graphs.get(0).getGraphPath();
        ///////

        Intent i = new Intent(
                OTNDropDownReceiver.SET_GRAPHS);
        Bundle bundle = new Bundle();
        bundle.putSerializable("GRAPHS" ,(Serializable) graphs );
        Log.d(TAG , bundle.toString());
        i.putExtra("GRAPHS", bundle );
        AtakBroadcast.getInstance().sendBroadcast(i);
        Log.d(TAG , "sent broadcast setgrapsh");

        _context = view.getContext();
        mc = ( (MapActivity) _context )
                .getMapComponent(RouteMapComponent.class);

        _routeManager = mc != null
                ? ((RouteMapComponent) mc)
                .getRoutePlannerManager()
                :null;
        assert _routeManager != null;


            _routeManager.registerPlanner ( "OTNOFFlineFast", new OTNOfflineRouter( pluginContext , selectedConfig , selectedGraphDir, OTNrequest.ProfileType.BEST ) );
            registerdRouters.add( "OTNOFFlineFast" );
            Log.d(TAG, "registered route planner: " + "OTNOFFlineFast" );
            // register other route planners
        // querry serivce db and create realtive routers


    }

    @Override
    protected void onDestroyImpl(Context context, MapView view) {
        super.onDestroyImpl(context, view);
        for ( String router : registerdRouters ) {
            _routeManager.unregisterPlanner(router);

        }

    }

    protected void findnSetGraphs(){
        IOProvider provider = IOProviderFactory.getProvider();
        String [] grapfsFolder = provider.list ( FileSystemUtils.getItem (FileSystemUtils.TOOL_DATA_DIRECTORY  + "/OTN" + "/graphs" ) );
        Log.d ( TAG , "array dirs: " + Arrays.toString(grapfsFolder));
        if ( grapfsFolder.length == 0 ){
            return;
        }
        OTNGraph tmp ;
        for (String dir : grapfsFolder ) {
            tmp =getGraphFromDir(dir);
            if (tmp != null) {
                this.graphs.add(tmp);
            }

        }
    }
    protected OTNGraph getGraphFromDir (String dir){
        GraphHopperConfig tmpConfig = null;
        OTNGraph tmp = null;
        try {
            Gson ason = new Gson();
            FileReader fReader =  new FileReader( FileSystemUtils.getItem(FileSystemUtils.TOOL_DATA_DIRECTORY  + "/OTN/graphs/" + dir + "/config.json") );
            JsonReader reader = new JsonReader(fReader);
            tmpConfig = ason.fromJson (reader , GraphHopperConfig.class );
            if (tmpConfig == null){
                Log.w(TAG , "reading j config failed");
               return null;
            }
        } catch (Exception e) {
            Log.e( TAG ,"An error occurred, reading " );
            Log.e ( TAG , e.toString());
        }
        tmp = new OTNGraph();
        //Log.d(TAG , "jConfig: " + tmpConfig.toString());
        tmp.setConfigGH (tmpConfig);
        tmp.setGraphPath( "/OTN/graphs/" + dir);
        return  tmp;

    }

    protected static boolean checkOTNFolder() {
        IOProvider provider = IOProviderFactory.getProvider();
        return provider.exists( FileSystemUtils.getItem (FileSystemUtils.TOOL_DATA_DIRECTORY  + "/OTN") );
    }
    public static OTNMapComponent getInstance () {
        Log.d(TAG , "getting instanche ");
        if (_instance == null){
            Log.w(TAG , "big problem INtsnce not good");
        }
        return _instance;
    //public get
    }
}
