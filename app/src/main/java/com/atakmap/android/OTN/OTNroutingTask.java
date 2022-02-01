package com.atakmap.android.OTN;

import android.content.SharedPreferences;



import com.atakmap.android.maps.PointMapItem;
import com.atakmap.android.routes.RouteGenerationTask;
import com.atakmap.android.routes.RoutePointPackage;
import com.atakmap.android.routes.nav.NavigationCue;
import com.atakmap.coremap.maps.conversion.EGM96;
import com.atakmap.coremap.maps.coords.GeoPoint;
import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.ResponsePath;
import com.graphhopper.config.CHProfile;
import com.graphhopper.config.Profile;
import com.graphhopper.util.InstructionList;
import com.graphhopper.util.Translation;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class OTNroutingTask extends RouteGenerationTask{
    private GraphHopper _hopper;
    private final String osmFile = "/sdcard/atak/tools/OTN/centro-latest.osm.pbf";
    private final String cacheLoc = "/sdcard/atak/tools/OTN/";
    private final String Vehicle = "car";
    private final String Profile = "car";

    public OTNroutingTask(RouteGenerationEventListener listener) {
        super(listener);
    }
    public OTNroutingTask(RouteGenerationEventListener listener, String test ) {

        super(listener);
    }

    @Override
    public RoutePointPackage generateRoute(SharedPreferences prefs, GeoPoint origin, GeoPoint dest, List<GeoPoint> byWayOff) {
        // cumulative cycle results
        List<PointMapItem> waypoint = new LinkedList<PointMapItem>()  ;
        Map<String , NavigationCue> waycue = new HashMap<>();
        // aux variable for cycle
        GeoPoint point = null;
        GeoPoint tmpPoint = null;
        int cueIndex = 0;
        String cue = "";
        NavigationCue navCue = null;
        String tmpUid = "";
        OTNresponse tmpMapPoint = null;


        GraphHopper _hopper = new GraphHopper();
        _hopper.setOSMFile ( osmFile );
        _hopper.setGraphHopperLocation ( cacheLoc );
        _hopper.setProfiles ( new Profile ( Profile ) .setVehicle ( Vehicle ) .setWeighting ( "fastest" ).setTurnCosts ( false ) );
        _hopper.getCHPreparationHandler( ).setCHProfiles( new CHProfile ( Profile ) );
        _hopper.importOrLoad();


        GHRequest request = new GHRequest(origin.getLatitude() , origin.getLongitude() , dest.getLatitude() ,dest.getLongitude())
                .setProfile ( Profile )
                .setLocale ( Locale.ENGLISH );

        ResponsePath  hopResponse = _hopper.route ( request ).getBest()  ;
        _hopper.close();
        if ( hopResponse.hasErrors() ) {
            throw new RuntimeException( hopResponse.getErrors().toString());
        }

        InstructionList instructionList = hopResponse.getInstructions();
        final Translation translation = instructionList.getTr();

        for ( int pointIndex = 0 ; pointIndex < hopResponse.getPoints().size() ; pointIndex ++) {
            tmpPoint = new GeoPoint( hopResponse.getPoints().getLat ( pointIndex ) , hopResponse.getPoints().getLon ( pointIndex ) );
            point = new GeoPoint( tmpPoint.getLatitude() , tmpPoint.getLongitude() , 100 ); // todo: fix altitude
            tmpUid = UUID.randomUUID().toString();
            tmpMapPoint = new OTNresponse( point , tmpUid  ) ;// TODO: find already used pointmapitem ?
            tmpMapPoint.setMetaString("type" , "b-m-p-c" );
            waypoint.add ( tmpMapPoint );

            if ( hopResponse.getPoints().getLat ( pointIndex ) == instructionList.get( cueIndex ).getPoints().getLat(0) &&
                    hopResponse.getPoints().getLon ( pointIndex ) == instructionList.get( cueIndex ).getPoints().getLon(0) ) {
                // set point as waypoint
                waypoint.get(pointIndex).setMetaString("type" , "b-m-p-w" );
                // set relative nav cue
                cue = instructionList.get(cueIndex).getTurnDescription(translation);
                navCue = new NavigationCue(UUID.randomUUID().toString() , cue , cue ) ;
                navCue.addCue(NavigationCue.TriggerMode.DISTANCE , 50 );
                waycue.put(tmpUid , navCue );
                cueIndex ++;
            }

        }
        return new RoutePointPackage( waypoint , waycue );
    }

}