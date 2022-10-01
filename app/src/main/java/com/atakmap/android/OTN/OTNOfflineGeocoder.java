package com.atakmap.android.OTN;

import android.location.Address;
import android.location.Geocoder;


import com.atakmap.android.routes.RoutePointPackage;
import com.atakmap.android.user.geocode.GeocodeManager;
import com.atakmap.coremap.filesystem.FileSystemUtils;
import com.atakmap.coremap.locale.LocaleUtil;
import com.atakmap.coremap.maps.coords.GeoBounds;
import com.atakmap.coremap.maps.coords.GeoPoint;
import com.atakmap.coremap.log.Log;

import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.index.LocationIndex;
import com.graphhopper.storage.index.Snap;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.EdgeIteratorState;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;


public class OTNOfflineGeocoder implements GeocodeManager.Geocoder {
    private LinkedList<OTNGraph> graphs ;
    private String TAG = "OTNOfflineGeocoder";

    OTNOfflineGeocoder (LinkedList<OTNGraph> graphs ){
        Log.d(TAG , " geocoder created");
        this.graphs = graphs;
    }

    @Override
    public String getUniqueIdentifier() {
        return "OTNOfflineGeocoder";
    }

    @Override
    public String getTitle() {
        return "OTNOfflineGeocoder";
    }

    @Override
    public String getDescription() {
        return "BEST effort offline Geocoeder";
    }

    @Override
    public boolean testServiceAvailable() {
        Log.d(TAG , "testng geocoder");
        return true;
    }

    @Override
    public List<Address> getLocation(GeoPoint geoPoint) {
        Snap snap = null;
        LinkedList<Address> vals = new LinkedList<>();
        Address tmp = new Address(Locale.ENGLISH);
        for (OTNGraph graph : graphs){
            if (graph == null ){
                Log.e(TAG , "jConfig could not be loaded" );
                continue;
            }
            GraphHopper hopper = new GraphHopper();
            hopper.setGraphHopperLocation(FileSystemUtils.getItem(FileSystemUtils.TOOL_DATA_DIRECTORY  +  graph.getGraphPath() ).getPath() );
            Log.d("OTN" , hopper.getGraphHopperLocation() );
            hopper.init(graph.getConfigGH());
            //hopper.load( FileSystemUtils.getItem(FileSystemUtils.TOOL_DATA_DIRECTORY  +   graph.getGraphPath()).getPath() );
            hopper.load();
            LocationIndex index = hopper.getLocationIndex();
            snap = index.findClosest(geoPoint.getLatitude(), geoPoint.getLongitude() , EdgeFilter.ALL_EDGES );
            if ( snap.isValid() ){
                break;
            }
            hopper.close();

        }
        if (snap == null ||  ! snap.isValid() ) {
            return vals;
        }
        EdgeIteratorState edge = snap.getClosestEdge();
        Log.d(TAG , edge.getName() );
        tmp.setAddressLine(0, edge.getName());
        vals.add(tmp);

        return vals;
    }

    @Override
    public List<Address> getLocation(String s, GeoBounds geoBounds) {

        return new LinkedList<Address>();
    }
}
