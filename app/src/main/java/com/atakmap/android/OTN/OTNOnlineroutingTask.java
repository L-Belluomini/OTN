package com.atakmap.android.OTN;

import android.content.SharedPreferences;
import android.util.Log;

import com.atakmap.android.maps.PointMapItem;
import com.atakmap.android.routes.RouteGenerationTask;
import com.atakmap.android.routes.RoutePointPackage;
import com.atakmap.android.routes.nav.NavigationCue;
import com.atakmap.coremap.maps.coords.GeoPoint;
import com.atakmap.map.elevation.ElevationManager;
import com.graphhopper.GHRequest;

import com.graphhopper.ResponsePath;
import com.graphhopper.api.GraphHopperWeb;
import com.graphhopper.util.InstructionList;

import com.graphhopper.util.Translation;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import okhttp3.OkHttpClient;

public class OTNOnlineroutingTask extends RouteGenerationTask {

        private static final String TAG = "OTNOnlineroutingTask";

        public OTNOnlineroutingTask(RouteGenerationEventListener listener) {
            super(listener);
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


            GraphHopperWeb webHopper = new GraphHopperWeb("http:Leo-Deck:8989/route/");


            /*
            boolean chRun = takRequest.isChCapable();
            boolean lmRun = takRequest.isLmCapable();
            */

            ghRequest = new GHRequest(origin.getLatitude() , origin.getLongitude() , dest.getLatitude() ,dest.getLongitude())
                    .setProfile ("car" )
                    .setLocale ( Locale.ENGLISH );
            /*
            if (!chRun) {
                ghRequest.putHint(Parameters.CH.DISABLE , true);
                Log.d(TAG , "ch disabled");
            }
            if(!lmRun){
                ghRequest.putHint(Parameters.Landmark.DISABLE , true);
                Log.d(TAG , "lm disabled");
            }

             */

        /*switch ( takRequest.getProfileType()) {
            case(BEST):
                break;
            case CH:
        }
        */

            ResponsePath hopResponse = webHopper.route ( ghRequest ) .getBest( )  ;
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
