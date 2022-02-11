package com.atakmap.android.OTN;

import android.content.SharedPreferences;

import com.atakmap.android.maps.PointMapItem;
import com.atakmap.android.routes.RouteGenerationTask;
import com.atakmap.android.routes.RoutePointPackage;
import com.atakmap.android.routes.nav.NavigationCue;
import com.atakmap.coremap.maps.coords.GeoPoint;
import com.graphhopper.GHRequest;
import com.graphhopper.GraphHopper;
import com.graphhopper.GraphHopperConfig;
import com.graphhopper.ResponsePath;
import com.graphhopper.util.InstructionList;
import com.graphhopper.util.Parameters;
import com.graphhopper.util.Translation;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class OTNOfflineroutingTask extends RouteGenerationTask{

    private final String graphPath = "/sdcard/atak/tools/OTN/cache"; // todo get from shared preference and setted from gui
    private OTNrequest takRequest;
    private GraphHopperConfig jConfig;

    public OTNOfflineroutingTask(RouteGenerationEventListener listener) {
        super(listener);
    }
    public OTNOfflineroutingTask(RouteGenerationEventListener listener, GraphHopperConfig jconfig , OTNrequest takRequest  ) {
        super(listener);

        this.jConfig = jconfig;
        this.takRequest = takRequest;
    }

    @Override
    public RoutePointPackage generateRoute(SharedPreferences prefs, GeoPoint origin, GeoPoint dest, List<GeoPoint> byWayOff) {
        GHRequest ghRequest;
        // cumulative cycle results
        List<PointMapItem> waypoint = new LinkedList<PointMapItem>()  ;
        Map<String , NavigationCue> waycue = new HashMap<>();
        // aux variable for cycle
        GeoPoint point;
        GeoPoint tmpPoint;
        int cueIndex = 0;
        String cue;
        NavigationCue navCue;
        String tmpUid;
        OTNresponse tmpMapPoint;

        GraphHopper hopper = new GraphHopper();
        hopper.setGraphHopperLocation(graphPath);
        hopper.init(jConfig);
        hopper.load(graphPath);

        //tmp for test
        boolean chRun = false;
        boolean lmRun = false;


        ghRequest = new GHRequest(origin.getLatitude() , origin.getLongitude() , dest.getLatitude() ,dest.getLongitude())
                .setProfile (takRequest.getProfile().getName() )
                .setLocale ( Locale.ENGLISH );

        if (!chRun) {
            ghRequest.putHint(Parameters.CH.DISABLE , true);
        }
        if(!lmRun){
            ghRequest.putHint(Parameters.Landmark.DISABLE , true);
        }


        ResponsePath  hopResponse = hopper.route ( ghRequest ) .getBest( )  ;
        hopper.close( );
        if ( hopResponse.hasErrors() ) {
            throw new RuntimeException( hopResponse.getErrors().toString()); // todo better manage errors
        }

        final InstructionList instructionList = hopResponse.getInstructions();
        final Translation translation = instructionList.getTr();

        for ( int pointIndex = 0 ; pointIndex < hopResponse.getPoints().size() ; pointIndex ++) {
            tmpPoint = new GeoPoint( hopResponse.getPoints().getLat ( pointIndex ) , hopResponse.getPoints().getLon ( pointIndex ) );
            point = new GeoPoint( tmpPoint.getLatitude() , tmpPoint.getLongitude() , 100 ); // todo: fix altitude
            tmpUid = UUID.randomUUID().toString();
            tmpMapPoint = new OTNresponse( point , tmpUid  ) ;// TODO: find already used pointmapitem ? (decorate mapitem better ?)
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