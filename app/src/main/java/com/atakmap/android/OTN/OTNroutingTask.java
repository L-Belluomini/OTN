package com.atakmap.android.OTN;

import android.content.SharedPreferences;

import com.atakmap.android.maps.MapItem;
import com.atakmap.android.maps.PointMapItem;
import com.atakmap.android.routes.RouteGenerationTask;
import com.atakmap.android.routes.RoutePointPackage;
import com.atakmap.android.routes.nav.NavigationCue;
import com.atakmap.coremap.maps.coords.GeoPoint;
import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.ResponsePath;
import com.graphhopper.config.CHProfile;
import com.graphhopper.config.Profile;
import com.graphhopper.util.InstructionList;
import com.graphhopper.util.PointList;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class OTNroutingTask extends RouteGenerationTask{
    private GraphHopper _hopper;

    public OTNroutingTask(RouteGenerationEventListener listener) {
        super(listener);
    }

    @Override
    public RoutePointPackage generateRoute(SharedPreferences prefs, GeoPoint origin, GeoPoint dest, List<GeoPoint> byWayOff) {
        GraphHopper _hopper = new GraphHopper();
        _hopper.setOSMFile("/sdcard/atak/tools/OTN/centro-latest.osm.pbf");
        _hopper.setGraphHopperLocation("/sdcard/atak/tools/OTN/");
        _hopper.setProfiles ( new Profile( "car" ) .setVehicle ( "car" ) .setWeighting ( "fastest" ).setTurnCosts ( false ) );
        _hopper.getCHPreparationHandler( ).setCHProfiles(new CHProfile( "car" ) );
        _hopper.importOrLoad();
        // cumulative cycle results
        List<PointMapItem> waypoint = new LinkedList<PointMapItem>()  ;
        Map<String , NavigationCue> waycue = new HashMap<>();

        GHRequest request = new GHRequest(origin.getLatitude() , origin.getLongitude() , dest.getLatitude() ,dest.getLongitude())
                .setProfile("car")
                .setLocale(Locale.ENGLISH);

        ResponsePath  hopResponse = _hopper.route ( request ).getBest()  ;

        if ( hopResponse.hasErrors() ) {
            throw new RuntimeException( hopResponse.getErrors().toString());
        }

        InstructionList instructionList = hopResponse.getInstructions();

        // aux variable for cicle
        GeoPoint point = null;
        OTNresponse mapitem = null;

        for ( int index = 0 ; index < hopResponse.getPoints().size() ; index ++) {
            point = new GeoPoint( hopResponse.getPoints().getLat ( index ) , hopResponse.getPoints().getLon ( index ) );
            waypoint.add ( new OTNresponse( point , "OTN"+ index ) ); // TODO: find already used mapitem

            //if ( index + 1 == hopResponse.getPoints().size() ) {
                waycue.put("OTN"+index , new NavigationCue("OTN" + index , "in arrivo", "in arrivo" ) );
            //} else {
            //waycue.put("OTN"+index , new NavigationCue("OTN" + index , instructionList.get( index ).getTurnDescription ( instructionList.getTr( ) ) , instructionList.get( index ).getTurnDescription ( instructionList.getTr( ) ) ) );
            //}
            _hopper.close();
        }
        return new RoutePointPackage( waypoint , waycue );
    }

}