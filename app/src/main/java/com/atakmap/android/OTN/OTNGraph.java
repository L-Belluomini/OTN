package com.atakmap.android.OTN;

import com.atakmap.android.maps.Polyline;
import com.atakmap.coremap.filesystem.FileSystemUtils;
import com.atakmap.coremap.maps.coords.GeoPoint;
import com.atakmap.filesystem.HashingUtils;
import com.graphhopper.GraphHopperConfig;
import com.graphhopper.config.CHProfile;
import com.graphhopper.config.LMProfile;
import com.graphhopper.config.Profile;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

public class OTNGraph implements Serializable {
    private String TAG = "OTNgraph" ;
    private String Name ;
    private GraphHopperConfig configGH;
    private  String graphPath;
    private String Hash ="";
    // Private boundingBox;
    // private String dateFile;
    // private String dateBuild;


    public  OTNGraph (String graphPath , GraphHopperConfig jconfig){
        this.configGH = jconfig;
        this.graphPath = graphPath;
    }

    public String getGraphPath() {
        return graphPath;
    }

    public GraphHopperConfig getConfigGH() { return this.configGH; }

    public  String getEdgeHash () {
        if (Hash.length() == 0 ){
            String val = HashingUtils.sha256sum( FileSystemUtils.getItem(FileSystemUtils.TOOL_DATA_DIRECTORY  + graphPath + "/edges" ) );
            Hash = val;
            return val;
        } else {
            return Hash;
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
        PolyLoader polyLoader = new PolyLoader( FileSystemUtils.getItem(FileSystemUtils.TOOL_DATA_DIRECTORY  + graphPath + "/"+ Name+".poly" ) );
        Polyline borderPoly = new Polyline( UUID.randomUUID().toString() );

        borderPoly.setPoints( (GeoPoint[]) polyLoader.loadPolygon().toArray() );
        return borderPoly;
    }

    @Override
    public String toString(){

        return graphPath;
    }


}
