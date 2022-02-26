package com.atakmap.android.OTN;

import com.graphhopper.GraphHopperConfig;

import java.io.Serializable;

public class OTNGraph implements Serializable {
    private String Name ;
    private GraphHopperConfig configGH;
    private  String GraphPath ;
    // Private boundingBox;
    // private String dateFile;
    //private String dateBuild;
    // private LinkedList<String> hashes;


    public void setConfigGH(GraphHopperConfig configGH) {
        this.configGH = configGH;
    }
    public void setGraphPath (String path) {
        this.GraphPath = path;
    }

    public GraphHopperConfig getConfigGH(){
        return configGH;
    }
    public String getGraphPath() {
        return GraphPath;
    }

    @Override
    public String toString(){

        return GraphPath;
    }

}
