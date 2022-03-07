package com.atakmap.android.OTN;

import com.atakmap.coremap.filesystem.FileSystemUtils;
import com.atakmap.filesystem.HashingUtils;
import com.graphhopper.GraphHopperConfig;

import java.io.Serializable;

public class OTNGraph implements Serializable {
    private String Name ;
    private GraphHopperConfig configGH;
    private  String graphPath;
    // Private boundingBox;
    // private String dateFile;
    //private String dateBuild;
    // private LinkedList<String> hashes;

    public  OTNGraph (String graphPath , GraphHopperConfig jconfig){
        this.configGH = configGH;
        this.graphPath = graphPath;


    }

    public String getGraphPath() {
        return graphPath;
    }
    public GraphHopperConfig getConfigGH() { return this.configGH; }
    public  String getEdgeHash () { return HashingUtils.sha256sum( FileSystemUtils.getItem(graphPath + "/edges")); }


    @Override
    public String toString(){

        return graphPath;
    }

}
