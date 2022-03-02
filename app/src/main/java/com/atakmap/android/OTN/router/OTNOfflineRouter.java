package com.atakmap.android.OTN.router;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.atakmap.android.OTN.OTNGraph;
import com.atakmap.android.OTN.OTNrequest;
import com.atakmap.android.OTN.plugin.R;
import com.atakmap.android.gui.PluginSpinner;
import com.atakmap.android.routes.RoutePlannerInterface;
import com.atakmap.android.routes.RouteGenerationTask;
import com.atakmap.android.routes.RoutePlannerOptionsView;


import com.atakmap.coremap.log.Log;
import com.graphhopper.GraphHopperConfig;
import com.graphhopper.config.Profile;

public class OTNOfflineRouter implements RoutePlannerInterface, AdapterView.OnItemSelectedListener  {
    private final String TAG = "OTNOfflineRouter";
    private int selectedProfile = 0;
    private OTNrequest.ProfileType selectedType ;
    private final Context pluginContext;
    private  final OTNGraph graph;




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
        RoutePlannerOptionsView view= (RoutePlannerOptionsView) LayoutInflater.from(pluginContext).inflate(R.layout.otnplanneroption, null);


        // profile
        PluginSpinner profileSpinner = ( PluginSpinner) view.findViewById(R.id.profilesSpinner);
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
        if (selectedType == OTNrequest.ProfileType.BEST) {
            return false;
        } else return selectedType == OTNrequest.ProfileType.BESTFLEXIBLE;
        // if fails the simpler is the best
    }




    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long iD) {

        Log.d(TAG , "view listener "+Integer.toString(position) + " Id:"+Long.toString(iD) + "adapter ID " + Integer.toString (adapterView.getId() ) );
        if (adapterView.getId() == R.id.profilesSpinner) {
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

