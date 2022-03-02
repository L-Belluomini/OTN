package com.atakmap.android.OTN.router;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;

import com.atakmap.android.OTN.plugin.R;
import com.atakmap.android.routes.RouteGenerationTask;
import com.atakmap.android.routes.RoutePlannerInterface;
import com.atakmap.android.routes.RoutePlannerOptionsView;
import com.atakmap.coremap.log.Log;

public class OTNOnlineRouter implements RoutePlannerInterface, AdapterView.OnItemSelectedListener {
    private final Context pluginContext;



    public OTNOnlineRouter( Context pluginContext){
        this.pluginContext = pluginContext;
    }

    /**
     * Gets the descriptive name of the planner.
     *
     * @return the descriptive name of the planner
     */
    public String getDescriptiveName(){
        return "OTN ONline router";
    }

    /**
     * Planner requires a network to be used.
     *
     * @return true if an active network is required.
     */
    public boolean isNetworkRequired(){
        return true;
    }

    /**
     * Gets the RouteGenerationTask for this planner that is run when initially generating a route.
     * @param routeGenerationEventListener The listener that should be associated with this task.
     * @return A RouteGenerationTask for this planner.
     */
    public RouteGenerationTask getRouteGenerationTask(
            RouteGenerationTask.RouteGenerationEventListener routeGenerationEventListener) {

        return  new OTNOnlineroutingTask(routeGenerationEventListener);
    }

    /**
     * Gets the additional options specific for the planner that may effect the
     * results.
     */
    public RoutePlannerOptionsView getOptionsView(AlertDialog parent){
        RoutePlannerOptionsView view= (RoutePlannerOptionsView) LayoutInflater.from(pluginContext).inflate(R.layout.otnplanneroption, null);

        // profile
       /* PluginSpinner profileSpinner = ( PluginSpinner) view.findViewById(R.id.profilesSpinner);
        ArrayAdapter<String> profileAdapter = new ArrayAdapter<String>( pluginContext , android.R.layout.simple_spinner_dropdown_item );
        for (  Profile item : jConfig.getProfiles() ){
            profileAdapter.add( item.getName() );
        }
        profileSpinner.setAdapter(profileAdapter);
        profileSpinner.setOnItemSelectedListener( this );

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
        return false;
    }

    /**
     * Gets whether or not the planner is capable of supporting routing around regions.
     */
    public boolean canRouteAroundRegions(){
        return false;
    }




    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long iD) {

        Log.d("OTN" , "view listener "+Integer.toString(position) + " Id:"+Long.toString(iD) + "adapter ID " + Integer.toString (adapterView.getId() ) );




        if (position == Math.round(iD) ) {

            //selectedProfile = position;
        }


    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }



}


