package com.atakmap.android.OTN;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.atakmap.android.maps.Polyline;
import com.atakmap.coremap.conversions.AreaUtilities;
import com.atakmap.coremap.filesystem.FileSystemUtils;
import com.atakmap.coremap.log.Log;
import com.atakmap.coremap.maps.coords.GeoPoint;
import com.atakmap.filesystem.HashingUtils;
import com.graphhopper.GraphHopper;
import com.graphhopper.GraphHopperConfig;
import com.graphhopper.config.CHProfile;
import com.graphhopper.config.LMProfile;
import com.graphhopper.config.Profile;
import com.graphhopper.storage.BaseGraph;
import com.graphhopper.util.Parameters;
import com.graphhopper.util.shapes.BBox;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;

public class OTNGraph implements Serializable {
    private static String TAG = "OTNgraph" ;
    private GraphHopperConfig configGH;
    private String graphPath;
    private String _hash ="";
    private int nodes =0;
    private int edges = 0;
    private BBox boundBox;
    private double area;
    private long size;
    private int borderColor;
    private int fillColor;
    private int alphaColor;
    private int borderStyle;



    public  OTNGraph ( String graphPath , GraphHopperConfig jconfig ) {
        this.configGH = jconfig;
        this.graphPath = graphPath;

        this.configGH.putObject( "graph.location" , FileSystemUtils.getItem( FileSystemUtils.TOOL_DATA_DIRECTORY + graphPath ).getPath() );

        getNodesAndEdges();
    }

    public String getGraphPath() {
        return graphPath;
    }

    public GraphHopperConfig getConfigGH() { return this.configGH; }

    public  String getEdgeHash () {
        if (_hash.length() == 0 ){
            String val = HashingUtils.sha256sum( FileSystemUtils.getItem(FileSystemUtils.TOOL_DATA_DIRECTORY  + graphPath + "/edges" ) );
            _hash = val;
            return val;
        } else {
            return _hash;
        }
    }

    public boolean isProfileCH(Profile profile ) {
        List<CHProfile> chProfiles = getConfigGH().getCHProfiles();
        for (CHProfile chprofile : chProfiles ) {
            if ( chprofile.getProfile().equals(profile.getName()) ) {
                return true;
            }

        }
            return false;
    }
    public boolean isProfilelm(Profile profile ) {
        List<LMProfile> lmProfiles = getConfigGH().getLMProfiles();
        for (LMProfile lmprofile : lmProfiles ) {
            if ( lmprofile.getProfile().equals(profile.getName()) ) {
                return true;
            }

        }
        return false;
    }

    public Polyline getBorder( ) {
        File polyFile = FileSystemUtils.getItem(FileSystemUtils.TOOL_DATA_DIRECTORY  + graphPath + "/"+ "border" + ".poly" );
        Log.d(TAG,polyFile.toString());
        Polyline borderPoly = new Polyline( UUID.randomUUID().toString() );

        if (! polyFile.exists()) {
            if ( boundBox == null ) {
                return null;
            }
            GeoPoint[] array = new GeoPoint[5];
            array[0] = new GeoPoint( boundBox.maxLat , boundBox.maxLon );
            array[1] = new GeoPoint( boundBox.minLat , boundBox.maxLon );
            array[2] = new GeoPoint( boundBox.minLat , boundBox.minLon );
            array[3] = new GeoPoint( boundBox.maxLat , boundBox.minLon );
            array[4] = new GeoPoint( boundBox.maxLat , boundBox.maxLon );
            borderPoly.setPoints( array );
            area = AreaUtilities.calcShapeArea( borderPoly.getPoints() );
            return borderPoly;

        }
        Log.d(TAG,"file exist");
        PolyLoader polyLoader = new PolyLoader( polyFile );


        /*
        Log.d(TAG,polyLoader.loadPolygon().toString());
        Log.d(TAG,polyLoader.loadPolygon().toArray()[1].toString());
        Log.d(TAG,polyLoader.loadPolygon().toArray()[1].toString());
         */

        GeoPoint[] tmp = new GeoPoint[ polyLoader.loadPolygon().size()];
        polyLoader.loadPolygon().toArray(tmp);

        borderPoly.setPoints( tmp );
        area = AreaUtilities.calcShapeArea( borderPoly.getPoints() );

        return borderPoly;
    }


    private void getNodesAndEdges()  { //todo rename
        GraphHopper hopper = new GraphHopper();
        configGH.putObject( "graph.dataaccess" , "MMAP");
        hopper.init( configGH );
        hopper.load();
        BaseGraph baseGraph =  hopper.getBaseGraph();
        hopper.close();
        configGH.putObject("graph.dataacess" , "");
        edges = baseGraph.getEdges();
        nodes = baseGraph.getNodes();
        boundBox = baseGraph.getBounds();
        try {
            size =  Files.size(FileSystemUtils.getItem(FileSystemUtils.TOOL_DATA_DIRECTORY + graphPath + "/edges").toPath());
        } catch ( Exception e ) {
            Log.e( TAG , e.toString() ) ;

        }


    }

    public int getNodes() {
        return nodes;
    }

    public int getEdges() {
        return edges;
    }

    public long getSize() { return size; }

    public BBox getBoundBox() {
        return boundBox;
    }

    public double getArea() {
        if ( Double.isNaN( area ) ) {
            getBorder();
        }

        return area ;
    }

    @Override
    public String toString(){

        return graphPath;
    }


}
