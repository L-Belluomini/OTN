package com.atakmap.android.OTN;

import com.graphhopper.GraphHopperConfig;
import com.graphhopper.config.CHProfile;
import com.graphhopper.config.LMProfile;
import com.graphhopper.config.Profile;

import java.util.List;

public class OTNrequest {
    private String profileName ="";
    private boolean lmCapable = false;
    private boolean chCapable = false;
    private Profile selectedProfile;

    public enum  ProfileType {
        STANDARD ,
        CH ,
        ALT;
    }

    public OTNrequest ( GraphHopperConfig jConfing , int selectedProfileIndex , ProfileType profileType ) {
        List<Profile> profilesList = jConfing.getProfiles();
        List<CHProfile> profilesChList = jConfing.getCHProfiles();
        List<LMProfile> profilesLmList = jConfing.getLMProfiles();

        // check and set profile type
        /*
        for ( CHProfile profile : profilesChList ) {
            if (profile. )
        }
        */
        this.selectedProfile = profilesList.get( selectedProfileIndex);

    }
    public Profile getProfile (){
        return selectedProfile;
    }

}
