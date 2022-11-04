package com.atakmap.android.OTN.router;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.atakmap.android.OTN.OTNGraph;
import com.atakmap.android.OTN.OTNrequest;
import com.atakmap.android.OTN.OTNwaypoitRouterOptionAdapter;
import com.atakmap.android.OTN.plugin.R;
import com.atakmap.android.dropdown.DropDownManager;
import com.atakmap.android.gui.PluginSpinner;
import com.atakmap.android.ipc.AtakBroadcast;
import com.atakmap.android.routes.RoutePlannerInterface;
import com.atakmap.android.routes.RouteGenerationTask;
import com.atakmap.android.routes.RoutePlannerOptionsView;


import com.atakmap.android.toolbar.ToolManagerBroadcastReceiver;
import com.atakmap.android.user.MapClickTool;
import com.atakmap.coremap.log.Log;
import com.graphhopper.GraphHopperConfig;
import com.graphhopper.config.Profile;

import java.util.LinkedList;
import java.util.List;

import com.atakmap.coremap.maps.coords.GeoPoint;

public class OTNOfflineRouter implements RoutePlannerInterface, AdapterView.OnItemSelectedListener  {
    private final String TAG = "OTNOfflineRouter";
    private int selectedProfile = 0;
    private OTNrequest.ProfileType selectedType ;
    private final Context pluginContext;
    private  final OTNGraph graph;
    private final List<GeoPoint> waypoints = new LinkedList<>();

    private static final String MAP_CLICK = "com.atakmap.android.OTN.MAP_CLICK";



    public OTNOfflineRouter( Context pluginContext , OTNGraph graph , OTNrequest.ProfileType type ){
        this.pluginContext = pluginContext;
        this.graph = graph;
        this.selectedType= type;



    }


    /**
     * Gets the descriptive name of the planner.
     *
     * @return the descriptive name of the planner
     */
    public String getDescriptiveName(){
        return "OTN OFFline router";
    }

    /**
     * Planner requires a network to be used.
     *
     * @return true if an active network is required.
     */
    public boolean isNetworkRequired(){
        return false;
    }

    /**
     * Gets the RouteGenerationTask for this planner that is run when initially generating a route.
     * @param routeGenerationEventListener The listener that should be associated with this task.
     * @return A RouteGenerationTask for this planner.
     */
    public RouteGenerationTask getRouteGenerationTask(
            RouteGenerationTask.RouteGenerationEventListener routeGenerationEventListener){

     return new OTNOfflineroutingTask( routeGenerationEventListener, graph  ,  new OTNrequest(graph.getConfigGH(), selectedProfile , selectedType) );
    }

    /**
     * Gets the additional options specific for the planner that may effect the
     * results.
     */
    public RoutePlannerOptionsView getOptionsView(AlertDialog parent){
        RoutePlannerOptionsView view= (RoutePlannerOptionsView) LayoutInflater.from(pluginContext).inflate(R.layout.otnplanneroption, null); //@gabri for waypoint

        // profile
        PluginSpinner profileSpinner = ( PluginSpinner) view.findViewById(R.id.route_plan_method);
        ArrayAdapter<String> profileAdapter = new ArrayAdapter<>( pluginContext , android.R.layout.simple_spinner_dropdown_item );
       if (graph == null) {
           Log.w(TAG , "jConfig is null!!");
           return view; // todo tell user does not work but dont kill app
       }
        for (  Profile item : graph.getConfigGH().getProfiles() ){
            profileAdapter.add( item.getName() );
        }
        profileSpinner.setAdapter(profileAdapter);
        profileSpinner.setOnItemSelectedListener( this );

        // way point UI
        waypoints.add( new GeoPoint( 0,0));
        ListView waypointLayout = ( ListView ) view.findViewById(R.id.waypoint_list);
        ArrayAdapter<GeoPoint> waypointAdapter = new OTNwaypoitRouterOptionAdapter(pluginContext , R.layout.waypoint_listitem , waypoints);
        waypointLayout.setAdapter(waypointAdapter);


        ImageButton pointDropperButton = view.findViewById(R.id.point_dropper_btn);
        pointDropperButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle toolBundle = new Bundle();
                toolBundle.putParcelable("callback" , new Intent( MAP_CLICK ).putExtra("wayPointNumber" , waypoints.size() ) );
                ToolManagerBroadcastReceiver.getInstance().startTool(MapClickTool.TOOL_NAME , toolBundle);
                if ( ToolManagerBroadcastReceiver.getInstance().getActiveTool() instanceof MapClickTool) {
                    DropDownManager.getInstance().hidePane();
                    parent.hide();
                }
            }
        });

        // if more brodcast are need move to dedicated reciver ? maybe...
        AtakBroadcast.getInstance().registerReceiver ( new BroadcastReceiver( ) {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG , "brodcast recived");
                if ( intent.getAction().equals(MAP_CLICK) ){
                    parent.show();
                    DropDownManager.getInstance().unHidePane();
                    GeoPoint waypointNew = GeoPoint.parseGeoPoint( intent.getStringExtra("point") );
                    if ( waypointNew == null || !waypointNew.isValid() ){
                        return;
                    }

                    if ( intent.getIntExtra("wayPointNumber", -1 ) != waypoints.size() ){
                        return;
                    }
                    waypoints.add( waypointNew);
                    waypointAdapter.notifyDataSetChanged();

                    Log.d(TAG , waypoints.toString() );
                }
            }
        } , new AtakBroadcast.DocumentedIntentFilter( MAP_CLICK ) );

        /*Button wayPointDialogButton = view.findViewById(R.id.waypoint_dialog);
        wayPointDialogButton.setOnClickListener( new View.OnClickListener() {
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(pluginContext);
                builder.setView( (LinearLayout) LayoutInflater.from(pluginContext).inflate (R.layout.route_planner_waypoints, null));
                builder.setNegativeButton("", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //AlertDialog.this.getDialog().cancel();
                    }
                });

             builder.create();
             builder.show();
        }


        });*/

       /* Button profileButton = view.findViewById(R.id.button);
        profileButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "button pressed");

            }


        });

        */

        return view;


    }



    /**
     * Gets any additional options for the planner that are needed at the time of navigating a route.
     *
     * @return Null if the planner does not support additional options, the options otherwise
     */
    public RoutePlannerOptionsView getNavigationOptions(AlertDialog parent){
        return null;
    }

    /**
     * Gets whether or not the planner is capable of supporting re-routing.
     */
    public boolean isRerouteCapable(){
        return true;
    }

    /**
     * Gets whether or not the planner is capable of supporting routing around regions.
     */
    public boolean canRouteAroundRegions(){
        /*if (selectedType == OTNrequest.ProfileType.BEST) {
            return false;
        } else return selectedType == OTNrequest.ProfileType.BESTFLEXIBLE;
        // if fails the simpler is the best

         */
        return true;
    }




    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long iD) {

        Log.d(TAG , "view listener "+Integer.toString(position) + " Id:"+Long.toString(iD) + "adapter ID " + Integer.toString (adapterView.getId() ) );
        if (adapterView.getId() == R.id.route_plan_method) {
            Log.d(TAG, "view listener profile spinner");
            if (position == Math.round(iD)) {
                selectedProfile = position;
            }
        } else {
            Log.w(TAG, " itemselceted defaulted");
            return;
        }



    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }


}

