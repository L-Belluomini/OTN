package com.atakmap.android.OTN.router;

import android.content.SharedPreferences;
import android.util.Log;

import com.atakmap.android.OTN.OTNGraph;
import com.atakmap.android.OTN.OTNrequest;
import com.atakmap.android.OTN.OTNresponse;
import com.atakmap.android.maps.PointMapItem;
import com.atakmap.android.routes.RouteGenerationTask;
import com.atakmap.android.routes.RoutePointPackage;
import com.atakmap.android.routes.nav.NavigationCue;
import com.atakmap.coremap.filesystem.FileSystemUtils;
import com.atakmap.coremap.maps.coords.GeoPoint;
import com.atakmap.map.elevation.ElevationManager;
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

    private static final String TAG = "OTNOfflineroutingTask";
    private final OTNGraph graph;
    private final OTNrequest takRequest;



    public OTNOfflineroutingTask(RouteGenerationEventListener listener, OTNGraph graph, OTNrequest takRequest  ) {
        super(listener);
        this.graph = graph;
        this.takRequest = takRequest;

    }


    @Override
    public RoutePointPackage generateRoute(SharedPreferences prefs, GeoPoint origin, GeoPoint dest, List<GeoPoint> byWayOff) {
        GHRequest ghRequest;
        // cumulative cycle results
        List<PointMapItem> waypoint = new LinkedList<>()  ;
        Map<String , NavigationCue> waycue = new HashMap<>();
        // aux variable for cycle
        GeoPoint point;
        GeoPoint tmpPoint;
        int cueIndex = 0;
        String cue;
        NavigationCue navCue;
        String tmpUid;
        OTNresponse tmpMapPoint;

        if (graph == null ){
            Log.e(TAG , "jConfig could not be loaded" );
            return new RoutePointPackage("jConfig could not be loaded");
        }
        GraphHopper hopper = new GraphHopper();
        hopper.setGraphHopperLocation(FileSystemUtils.getItem(FileSystemUtils.TOOL_DATA_DIRECTORY  +  graph.getGraphPath() ).getPath() );
        Log.d("OTN" , hopper.getGraphHopperLocation() );
        hopper.init(graph.getConfigGH());
        //hopper.load( FileSystemUtils.getItem(FileSystemUtils.TOOL_DATA_DIRECTORY  +   graph.getGraphPath()).getPath() );
        hopper.load( );


        ghRequest = new GHRequest(origin.getLatitude() , origin.getLongitude() , dest.getLatitude() ,dest.getLongitude())
                .setProfile (takRequest.getProfile().getName() )
                .setLocale ( Locale.ENGLISH );

        if ( ! takRequest.isChCapable() | takRequest.getProfileType() == OTNrequest.ProfileType.BESTFLEXIBLE ) {
            ghRequest.putHint(Parameters.CH.DISABLE , true);
            Log.d(TAG , "ch disabled");
        }

        if(!takRequest.isLmCapable()){
            ghRequest.putHint(Parameters.Landmark.DISABLE , true);
            Log.d(TAG , "lm disabled");
        }



        ResponsePath  hopResponse = hopper.route ( ghRequest ) .getBest( )  ;
        hopper.close( );
        if ( hopResponse.hasErrors() ) {
            Log.e(TAG , hopResponse.getErrors ( ).toString ( ) );
            return new RoutePointPackage( hopResponse.getErrors ( ).toString( ) );
        }

        final InstructionList instructionList = hopResponse.getInstructions();
        final Translation translation = instructionList.getTr();

        for ( int pointIndex = 0 ; pointIndex < hopResponse.getPoints().size() ; pointIndex ++) {
            tmpPoint = new GeoPoint( hopResponse.getPoints().getLat ( pointIndex ) , hopResponse.getPoints().getLon ( pointIndex ) );
            point = new GeoPoint( tmpPoint.getLatitude() , tmpPoint.getLongitude() , ElevationManager.getElevation( tmpPoint , null ) );
            tmpUid = UUID.randomUUID().toString();
            tmpMapPoint = new OTNresponse( point , tmpUid  ) ;// TODO: decorate mapitem better ?
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