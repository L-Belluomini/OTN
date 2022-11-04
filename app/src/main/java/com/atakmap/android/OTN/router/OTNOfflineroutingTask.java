package com.atakmap.android.OTN.router;

import android.content.SharedPreferences;
import android.util.Log;

import com.atakmap.android.OTN.OTNGraph;
import com.atakmap.android.OTN.OTNrequest;
import com.atakmap.android.OTN.OTNresponse;
import com.atakmap.android.drawing.mapItems.DrawingCircle;
import com.atakmap.android.maps.PointMapItem;
import com.atakmap.android.maps.Shape;
import com.atakmap.android.routes.RouteGenerationTask;
import com.atakmap.android.routes.RoutePointPackage;
import com.atakmap.android.routes.nav.NavigationCue;
import com.atakmap.android.routes.routearound.RouteAroundRegionManager;
import com.atakmap.coremap.maps.coords.GeoPoint;
import com.atakmap.map.elevation.ElevationManager;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.GraphHopperConfig;
import com.graphhopper.ResponsePath;
import com.graphhopper.config.Profile;
import com.graphhopper.json.Statement;
import com.graphhopper.util.CustomModel;
import com.graphhopper.util.InstructionList;
import com.graphhopper.util.JsonFeature;
import com.graphhopper.util.Parameters;
import com.graphhopper.util.Translation;
import com.graphhopper.util.shapes.GHPoint;


import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;

import java.util.ArrayList;
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
    private List<GeoPoint> _waypoints;



    public OTNOfflineroutingTask(RouteGenerationEventListener listener, OTNGraph graph, OTNrequest takRequest  ) {
        super(listener);
        this.graph = graph;
        this.takRequest = takRequest;

    }
    public OTNOfflineroutingTask(RouteGenerationEventListener listener, OTNGraph graph, OTNrequest takRequest  , List<GeoPoint> waypoints) {
        super(listener);
        this.graph = graph;
        this.takRequest = takRequest;
        this._waypoints=waypoints;

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

        Log.d(TAG,"by way off " + Integer.toString( byWayOff.size()));
        Log.d(TAG,takRequest.getProfileType().toString() );
        Log.d(TAG,"is ch capable "  + Boolean.toString(takRequest.isChCapable() ));
        Log.d(TAG,"is lm capable "  + Boolean.toString(takRequest.isLmCapable() ));

        hopper.init(graph.getConfigGH());
        Log.d("OTN" , hopper.getGraphHopperLocation() );
        //hopper.setGraphHopperLocation(FileSystemUtils.getItem(FileSystemUtils.TOOL_DATA_DIRECTORY  +  graph.getGraphPath() ).getPath() );
        //hopper.load( FileSystemUtils.getItem(FileSystemUtils.TOOL_DATA_DIRECTORY  +   graph.getGraphPath()).getPath() );
        Log.d(TAG , Boolean.toString( hopper.load( ) ) );



        if ( byWayOff.size() == 0 && _waypoints.size() == 0){
            ghRequest = new GHRequest(origin.getLatitude() , origin.getLongitude() , dest.getLatitude() ,dest.getLongitude());
        } else {
            ghRequest = new GHRequest();
        }
        // IF ENEBALED USE BYWAYOFF
        if ( byWayOff.size() > 0) { // if not overriden
            byWayOff.add( 0 , origin);
            byWayOff.add(dest); // append
            for (GeoPoint takwaypoint : byWayOff ) {
                Log.d(TAG , takwaypoint.toString() );
                ghRequest.addPoint( new GHPoint( takwaypoint.getLatitude() , takwaypoint.getLongitude() ) );

            }
        } else if ( _waypoints.size() >0 ) {
            _waypoints.add( 0 , origin);
            _waypoints.add( dest); // append
            for (GeoPoint extrawaypoint : _waypoints ) {
                Log.d(TAG , extrawaypoint.toString() );
                ghRequest.addPoint( new GHPoint( extrawaypoint.getLatitude() , extrawaypoint.getLongitude() ) );
            }
        }

        ghRequest.setProfile (takRequest.getProfile().getName() );

        // GET ROUTEAROUDN REGIONS
        RouteAroundRegionManager regionManager = RouteAroundRegionManager.getInstance();
        ArrayList<Shape> shapelist =regionManager.getRegions();
        if ( ! shapelist.isEmpty()) {
            if ( takRequest.getProfile().getWeighting().equals("custom") ) {
                ghRequest.setCustomModel(createRaModel(shapelist));
            }
            else {
                String[] areaBlcoks = createRaBlockArea( shapelist );
                String tmparea="";
                for (String area : areaBlcoks) {
                    tmparea = tmparea + area;
                }
                Log.d(TAG,"adding ra hint" + tmparea );
                ghRequest.putHint(Parameters.Routing.BLOCK_AREA , tmparea );

            }
        }


        ghRequest.setLocale ( Locale.ENGLISH ); // @leo add support for multi locale & support to force it

        if ( ! takRequest.isChCapable() | takRequest.getProfileType() == OTNrequest.ProfileType.BESTFLEXIBLE ) {
            ghRequest.putHint(Parameters.CH.DISABLE , true);
            Log.d(TAG , "ch disabled");
        }

        if(!takRequest.isLmCapable()){
            ghRequest.putHint(Parameters.Landmark.DISABLE , true);
            Log.d(TAG , "lm disabled");
        }




        GHResponse  hopResponse = hopper.route ( ghRequest ) ;
        if ( hopResponse.hasErrors() ) {
            Log.e(TAG , hopResponse.getErrors ( ).toString ( ) );
            return new RoutePointPackage( hopResponse.getErrors ( ).toString( ) );
        }

        ResponsePath  bestResponse = hopResponse.getBest();

        hopper.close( );
        Log.d(TAG,"hopper clesed");


        final InstructionList instructionList = bestResponse.getInstructions();
        final Translation translation = instructionList.getTr();

        for ( int pointIndex = 0 ; pointIndex < bestResponse.getPoints().size() ; pointIndex ++) {
            tmpPoint = new GeoPoint( bestResponse.getPoints().getLat ( pointIndex ) , bestResponse.getPoints().getLon ( pointIndex ) );
            point = new GeoPoint( tmpPoint.getLatitude() , tmpPoint.getLongitude() , ElevationManager.getElevation( tmpPoint , null ) );
            tmpUid = UUID.randomUUID().toString();
            tmpMapPoint = new OTNresponse( point , tmpUid  ) ;// TODO: decorate mapitem better ?
            tmpMapPoint.setMetaString("type" , "b-m-p-c" );
            waypoint.add ( tmpMapPoint );

            if ( bestResponse.getPoints().getLat ( pointIndex ) == instructionList.get( cueIndex ).getPoints().getLat(0) &&
                    bestResponse.getPoints().getLon ( pointIndex ) == instructionList.get( cueIndex ).getPoints().getLon(0) ) {
                // set point as waypoint
                waypoint.get(pointIndex).setMetaString("type" , "b-m-p-w" );
                waypoint.get(pointIndex).setMetaString("title" ,"WP" + Integer.toString(pointIndex +1 ) );
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

    private CustomModel createRaModel (ArrayList<Shape> shapelist ){
        CustomModel customModel = new CustomModel();
            Log.d(TAG, "RA to custom model");
            for (Shape takShape : shapelist ) {
                Log.d(TAG,takShape.toString()  );
                Log.d(TAG, takShape.getType() );

                GeoPoint[] bordePoints= takShape.getPoints();
                double[] latArray = new double[bordePoints.length];
                double[] longArray = new double[bordePoints.length];
                Coordinate[] cordsArray = new Coordinate[bordePoints.length];
                JsonFeature tempFeature;
                String areaname;
                int index =0;

                switch ( takShape.getType( ) ){

                    case( "u-d-r"): // rectangle
                        Log.i(TAG," RA Cm rectangle");
                        for ( GeoPoint bordedpoint : bordePoints ){
                            //latArray[index] = bordedpoint.getLatitude();
                            //longArray[index] = bordedpoint.getLongitude();
                            cordsArray[index] = new Coordinate( bordedpoint.getLatitude() , bordedpoint.getLongitude() );
                            Log.d(TAG, "border point " + Double.toString( latArray[index] ) + " "  + Double.toString( longArray[index] ) );
                        }
                        tempFeature = new JsonFeature();
                        tempFeature.setGeometry( new GeometryFactory( ).createPolygon( cordsArray ) );
                        areaname = "RA"+"rect"+ Integer.toString( index);
                        customModel.getAreas().put( areaname , tempFeature );
                        customModel.addToPriority(Statement.If("in_" + areaname , Statement.Op.MULTIPLY , "0"));


                        break;

                    case( "u-d-f")://polyline
                        Log.i(TAG," RA Cm poly");
                        for ( GeoPoint bordedpoint : bordePoints ){
                            latArray[index] = bordedpoint.getLatitude();
                            longArray[index] = bordedpoint.getLongitude();
                            cordsArray[index] = new Coordinate( bordedpoint.getLatitude() , bordedpoint.getLongitude() );
                            Log.d(TAG, "border point " + Double.toString( latArray[index] ) + " "  + Double.toString( longArray[index] ) );
                        }

                        tempFeature = new JsonFeature();
                        tempFeature.setGeometry( new GeometryFactory( ).createPolygon( cordsArray ) );
                        areaname = "RA"+"poly"+ Integer.toString( index);
                        customModel.getAreas().put( areaname , tempFeature );
                        customModel.addToPriority(Statement.If("in_" + areaname , Statement.Op.MULTIPLY , "0"));

                        break;

                    case("u-d-c-c"):// circle
                        Log.i(TAG," RA Cm poly");
                        DrawingCircle takCircle = (DrawingCircle) takShape;
                        tempFeature = new JsonFeature();
                        //tempFeature.setGeometry( new GeometryFactory( ).createPolygon()
                        //blockArea.add(new Circle( takCircle.getCenterPoint().getLatitude() , takCircle.getCenterPoint().getLongitude() , takCircle.getRadius() ) );
                        Log.d( TAG, "center point " + Double.toString( takCircle.getCenterPoint().getLatitude() ) + " "  + Double.toString( takCircle.getCenterPoint().getLongitude()  ) );
                        break;
                }
            }
        return customModel;
    }

    private String[] createRaBlockArea (ArrayList<Shape> shapelist ){
        Log.d(TAG, "RA to block area");
        String[] areaBlocks = new String[shapelist.size()];
        String tmpAreaBlock;
        int index =0;

        for (Shape takShape : shapelist ) {
            Log.d(TAG,takShape.toString()  );
            Log.d(TAG, takShape.getType() );
            tmpAreaBlock ="";
            GeoPoint[] bordePoints= takShape.getPoints();


            switch ( takShape.getType( ) ){

                case( "u-d-r"): // rectangle
                    Log.i(TAG," RA Ba rectangle");
                    for ( GeoPoint bordedpoint : bordePoints ){
                        tmpAreaBlock = tmpAreaBlock + "," + Double.toString(  bordedpoint.getLatitude()) + "," +  Double.toString(  bordedpoint.getLongitude());
                        //Log.d(TAG, "borber point " + Double.toString( latArray[index] ) + " "  + Double.toString( longArray[index] ) );
                    }
                    tmpAreaBlock = tmpAreaBlock.replaceFirst(",","");
                    tmpAreaBlock = tmpAreaBlock + ";";
                    areaBlocks[index] =tmpAreaBlock;
                    tmpAreaBlock = "";

                    break;

                case( "u-d-f")://polyline
                    Log.i(TAG," RA Ba polyline");
                    for ( GeoPoint bordedpoint : bordePoints ){
                        tmpAreaBlock = tmpAreaBlock + "," + Double.toString(  bordedpoint.getLatitude()) + "," +  Double.toString(  bordedpoint.getLongitude());
                        //Log.d(TAG, "border point " + Double.toString( latArray[index] ) + " "  + Double.toString( longArray[index] ) );
                    }
                    tmpAreaBlock = tmpAreaBlock.replaceFirst(",","");
                    tmpAreaBlock = tmpAreaBlock + ";";
                    areaBlocks[index] =tmpAreaBlock;
                    tmpAreaBlock = "";


                    break;

                case("u-d-c-c"):// circle
                    Log.i(TAG," RA Ba circle");
                    DrawingCircle takCircle = (DrawingCircle) takShape;
                    tmpAreaBlock =  Double.toString( takCircle.getCenterPoint().getLatitude() ) + "," +  Double.toString(  takCircle.getCenterPoint().getLongitude() ) + "," + Integer.toString( (int) takCircle.getRadius() );
                    Log.d( TAG, "center point " + Double.toString( takCircle.getCenterPoint().getLatitude() ) + " "  + Double.toString( takCircle.getCenterPoint().getLongitude()  ) + " "  + Double.toString( takCircle.getRadius()  ) );
                    tmpAreaBlock = tmpAreaBlock + ";";
                    areaBlocks[index] =tmpAreaBlock;
                    tmpAreaBlock = "";
                    break;
            }
            index++;
        }
        return areaBlocks;
    }
}