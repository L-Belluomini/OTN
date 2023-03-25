
package com.atakmap.android.OTN;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;

import com.atakmap.android.OTN.router.OTNOfflineRouter;
import com.atakmap.android.ipc.AtakBroadcast;
import com.atakmap.android.ipc.AtakBroadcast.DocumentedIntentFilter;

import com.atakmap.android.maps.DefaultMapGroup;
import com.atakmap.android.maps.MapActivity;
import com.atakmap.android.maps.MapComponent;
import com.atakmap.android.maps.MapView;
import com.atakmap.android.dropdown.DropDownMapComponent;


import com.atakmap.android.maps.Polyline;
import com.atakmap.android.maps.Shape;
import com.atakmap.android.overlay.AbstractMapOverlay2;
import com.atakmap.android.overlay.DefaultMapGroupOverlay;
import com.atakmap.android.preference.AtakPreferences;
import com.atakmap.android.routes.RoutePlannerInterface;
import com.atakmap.android.user.FocusBroadcastReceiver;
import com.atakmap.coremap.filesystem.FileSystemUtils;
import com.atakmap.coremap.io.IOProvider;
import com.atakmap.coremap.io.IOProviderFactory;
import com.atakmap.coremap.log.Log;
import com.atakmap.android.OTN.plugin.R;

import com.atakmap.app.preferences.ToolsPreferenceFragment;

import com.atakmap.android.routes.RoutePlannerManager;
import com.atakmap.android.routes.RouteMapComponent;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.graphhopper.GraphHopperConfig;

import java.io.File;
import java.io.FileReader;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class OTNMapComponent extends DropDownMapComponent {

    public static final String SHOW_GRAPH_DETAIL = "com.atakmap.android.OTN.SHOW_PLUGIN.DETAIL";
    public static final String SHOW_PLUGIN = "com.atakmap.android.OTN.SHOW_PLUGIN";
    public static final String SET_GRAPHS = "com.atakmap.android.OTN.SET_GRAPHS";
    public static final String FIND_GRAPHS = "com.atakmap.android.OTN.FIND_GRAPHS";
    public static final String SET_SELECTED_GRAPH = "com.atakmap.android.OTN.SET_SELECTED_GRAPH";
    public static final String FOCUS_BRODER = "com.atakmap.android.OTN.FOCUS_BRODER";
    public static final String SET_BORDER_COLOR = "com.atakmap.android.OTN.SET_BORDER_COLOR";
    public final static String ID_COLOR_STROKE = "otn_color_stroke";
    public final static String ID_COLOR_FILL = "otn_color_fill";
    public final static String INSTRUCTION_LANG = "otn_instruction_lang";
    private static final String TAG = "OTNMapComponent";
    private Context pluginContext;
    private Context _context;
    private RoutePlannerManager _routeManager ;
    private LinkedList<String> registeredRouters = new LinkedList<>();
    private LinkedList<OTNGraph> graphs = new LinkedList<>();
    private final Map<String , String> bordersMap = new HashMap<>();
    private OTNGraph selectdeGraph;
    private OTNGraph tmpGraph;
    private DefaultMapGroup mapGroup;
    private AtakPreferences _prefs;

    private AbstractMapOverlay2 overlay;
    private int fillColor;
    private int strokeColor;

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
        pluginContext = context;
        pluginContext.setTheme(R.style.ATAKPluginTheme);
        super.onCreate(pluginContext, intent, view);

        OTNDropDownReceiver ddr = new OTNDropDownReceiver(
                view, pluginContext);

        OTNDropDownGraphDetails ddgd = new OTNDropDownGraphDetails(
                view, pluginContext);

        // prepare to set the routers
        _context = view.getContext();
        MapComponent mc = ((MapActivity) _context)
                .getMapComponent(RouteMapComponent.class);

        _routeManager = mc != null
                ? ((RouteMapComponent) mc)
                .getRoutePlannerManager()
                :null;
        assert _routeManager != null;

        _prefs = new AtakPreferences(view);

        fillColor = _prefs.get ( ID_COLOR_FILL, Color.BLUE );
        strokeColor = _prefs.get ( ID_COLOR_STROKE, Color.BLUE );

        Log.d(TAG, "registering the plugin filter");
        DocumentedIntentFilter ddFilter = new DocumentedIntentFilter();
        ddFilter.addAction(SHOW_PLUGIN);
        ddFilter.addAction(SET_GRAPHS);
        registerDropDownReceiver(ddr, ddFilter);

        ddFilter = new DocumentedIntentFilter();
        ddFilter.addAction(SHOW_GRAPH_DETAIL);
        registerDropDownReceiver(ddgd, ddFilter);


        final BroadcastReceiver selectegraphReciver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context pluginContext, Intent intent) {
            Log.w(TAG,"failled setting bundle");
            final String action = intent.getAction();
            if (action == null)
                return;

                switch (action) {
                    case (SET_SELECTED_GRAPH):
                        Bundle graphBundle = intent.getBundleExtra("GRAPH");
                        if (graphBundle == null) {
                            Log.w(TAG,"failled setting bundle");
                            return;

                        }
                        tmpGraph =  (OTNGraph) graphBundle.getSerializable("GRAPH");
                        if ( tmpGraph == null ) {
                            Log.w(TAG,"failled importing selected graph");
                            return;
                        }
                        Log.d(TAG , tmpGraph.toString());
                        updateRouters();

                        break;

                    case (FIND_GRAPHS):
                        findnSetGraphs();
                        pushGraphs();
                        updateOverlays();
                        break;

                    case (OTNMapComponent.FOCUS_BRODER):
                        Log.d(TAG, "focus intne launched");
                        String borderHash = intent.getStringExtra("BorderHash");

                        if (borderHash == null){
                            Log.d(TAG, "focus NO hash" );
                            return;
                        }
                        Log.d(TAG, "focus hash" + borderHash);
                        Intent focusIntent = new Intent(FocusBroadcastReceiver.FOCUS );
                        focusIntent.putExtra("uid", bordersMap.get(borderHash) );
                        //focusIntent.putExtra("useTightZoom",true);
                        AtakBroadcast.getInstance().sendBroadcast(focusIntent);
                        break;

                }
            }
        };
        DocumentedIntentFilter mcFilter = new DocumentedIntentFilter();
        mcFilter.addAction(FIND_GRAPHS);
        mcFilter.addAction(SET_SELECTED_GRAPH);
        mcFilter.addAction(FOCUS_BRODER);
        AtakBroadcast.getInstance().registerReceiver(selectegraphReciver, mcFilter );

        // map overlay
        //overlay = new OTNGraphOverlay(view , pluginContext);

        mapGroup = new DefaultMapGroup("OTNoverlay");
        overlay = new DefaultMapGroupOverlay ( MapView.getMapView() , mapGroup   );
        mapGroup.setMetaString("iconUri" , "android.resource://"+  pluginContext.getPackageName() + "/" + R.drawable.otn_logo);

        view.getMapOverlayManager().addOverlay( overlay );
        Log.d(TAG , "overlay added");

        // check if OTN exist
        if ( checkOTNFolder()) {
            // otn exist
            findnSetGraphs();
        } else {
            Log.w(TAG , "no folder found");
            Toast toast = Toast.makeText(pluginContext, "NO OTN folder FOUND", Toast.LENGTH_LONG); // check right context
            toast.show();
            setNewUp();
            return;
        }

        findnSetGraphs();
        updateOverlays();

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
        Toast toast = Toast.makeText(pluginContext, "NO OTN graph FOUND", Toast.LENGTH_SHORT); // check right context
        toast.show();
        return;
        }



        //add offline geocorder
        /*
        GeocodeManager _geocoderManager = GeocodeManager.getInstance(_context);
        _geocoderManager.registerGeocoder( new OTNOfflineGeocoder( graphs) );
        */

        _routeManager.registerPlanner ( "OTNOFFlineFast", new OTNOfflineRouter( pluginContext , selectdeGraph , OTNrequest.ProfileType.BEST ) );
        _prefs.set( "OTNSelectedGraph" , selectdeGraph.getEdgeHash() );
        registeredRouters.add( "OTNOFFlineFast" );
        Log.d(TAG, "registered route planner: " + "OTNOFFlineFast" + selectdeGraph.toString() );
        // register other route planners
        // querry serivce db and create realtive routers

        //TEST///////////////////////////////////////////////////////////////////////////////////////////////////

        ////////////////////// PREFERENCES /////////////////////////////
        ToolsPreferenceFragment
                .register(
                        new ToolsPreferenceFragment.ToolPreference(
                                "OTN Preferences",
                                "Custom preferences for OTN plugin",
                                "OTNPreference",
                                context.getResources().getDrawable(
                                        R.drawable.otn_logo, null),
                                new OTNPreferenceFragment(context)));
    }

    @Override
    protected void onDestroyImpl(Context context, MapView view) {
        super.onDestroyImpl(context, view);
        for ( String router : registeredRouters) {
            _routeManager.unregisterPlanner(router);
        }
        view.getMapOverlayManager().removeOverlay( overlay );

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
        _prefs.set("OTNSelectedGraph",tmpGraph.getEdgeHash());
        tmpGraph = null;
        // add routr/routers
        _routeManager.registerPlanner ( "OTNOFFlineFast", new OTNOfflineRouter( pluginContext , selectdeGraph , OTNrequest.ProfileType.BEST ) );
        registeredRouters.add( "OTNOFFlineFast" );
        pushGraphs();
    }

    protected void pushGraphs(){
        Intent i = new Intent(SET_GRAPHS);
        Bundle bundleGraphs = new Bundle();
        bundleGraphs.putSerializable("GRAPHS" ,(Serializable) this.graphs );
        Log.d(TAG , bundleGraphs.toString());
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
        if (grapfsFolder == null ){
            return;
        }
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
                    Log.d(TAG , this.graphs.toString());
                }

            }
        }
    }

    private void updateOverlays() {
        Log.d(TAG, "overlay update");
        // remove old graphs
        mapGroup.clearItems();
        mapGroup.clearGroups();

        bordersMap.clear();

        for ( OTNGraph tmpGrap: graphs ) {
            Polyline border = tmpGrap.getBorder();

            border.setStrokeColor( strokeColor );
            border.setFillColor( fillColor );
            border.setStrokeWeight( 5 ); // from 1 to 6

            //border.setRadialMenu();
            border.setStrokeStyle(Shape.BASIC_LINE_STYLE_DOTTED );
            border.setFillAlpha( 250 );
            border.setVisible( true);
            //border.setStyle(Shape.STYLE_FILLED_MASK); // stroke
            border.setTitle( tmpGrap.getGraphPath().substring( tmpGrap.getGraphPath().lastIndexOf("/")+1 ) );
            //border.setMetaString("iconUri" , "android.resource://"+  pluginContext.getPackageName() + "/" + R.drawable.ic_otnlogo_dropdown);

            if ( border != null ) {
                bordersMap.put(tmpGrap.getEdgeHash(), border.getUID());
                mapGroup.addItem(border);
            }
        }
    }
    /* TODO add specific graph updater
    private void updateOverlay (){

    }

     */

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
            return null;
        }
        return new OTNGraph("/OTN/graphs/" + dir , tmpConfig );
    }

    protected boolean checkOTNFolder() {
        boolean flag = false;
        IOProvider provider = IOProviderFactory.getProvider();
        flag = provider.exists( FileSystemUtils.getItem (FileSystemUtils.TOOL_DATA_DIRECTORY  + "/OTN") );
        if ( provider.exists( FileSystemUtils.getItem (FileSystemUtils.TOOL_DATA_DIRECTORY  + "/OTN/cahe") ) ) {
            Toast toast = Toast.makeText( pluginContext , "cache dir is deprecated.....", Toast.LENGTH_LONG);
            toast.show();

        }

        return flag ;
    }

    protected void setNewUp( ) {
        IOProvider provider = IOProviderFactory.getProvider();

        if (! provider.exists( FileSystemUtils.getItem (FileSystemUtils.TOOL_DATA_DIRECTORY  + "/OTN") ) ) {
            if ( provider.mkdir( new File(FileSystemUtils.TOOL_DATA_DIRECTORY  + "/OTN" ) ) ) {
                provider.mkdir( new File(FileSystemUtils.TOOL_DATA_DIRECTORY  + "/OTN/graphs" ) );
                Toast toast = Toast.makeText( pluginContext , "NO OTN graph FOUND", Toast.LENGTH_SHORT);
                toast.show();
            }

        }
    }

}
