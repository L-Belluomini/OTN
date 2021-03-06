package com.atakmap.android.OTN;

import com.atakmap.coremap.filesystem.FileSystemUtils;
import com.atakmap.filesystem.HashingUtils;
import com.graphhopper.GraphHopperConfig;
import com.atakmap.coremap.log.Log;

import java.io.Serializable;

public class OTNGraph implements Serializable {
    private String TAG = "OTNgraph" ;
    private String Name ;
    private GraphHopperConfig configGH;
    private  String graphPath;
    private String Hash ="";
    // Private boundingBox;
    // private String dateFile;
    // private String dateBuild;
    // private LinkedList<String> hashes;

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


    @Override
    public String toString(){

        return graphPath;
    }

}
