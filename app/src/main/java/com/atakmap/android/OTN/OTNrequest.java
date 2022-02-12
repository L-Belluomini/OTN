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
    private ProfileType selectedType = ProfileType.BEST;

    public enum  ProfileType {
        STANDARD,
        CH,
        ALT,
        BEST;
    }

    public OTNrequest ( GraphHopperConfig jConfing , int selectedProfileIndex , ProfileType selectedType ) {
        List<Profile> profilesList = jConfing.getProfiles();
        List<CHProfile> profilesChList = jConfing.getCHProfiles();
        List<LMProfile> profilesLmList = jConfing.getLMProfiles();
        this.selectedType = selectedType;

        // check and set profile type

        for ( CHProfile profile : profilesChList ) {
            if (  profile.getProfile().toString().equals( profilesList.get(selectedProfileIndex).getName() ) ){
                this.chCapable = true;
            }
        }

        for ( LMProfile profile : profilesLmList ) {
            if (  profile.getProfile().toString().equals( profilesList.get(selectedProfileIndex).getName() ) ){
                this.lmCapable = true;
            }
        }

        this.selectedProfile = profilesList.get( selectedProfileIndex );

    }
    public Profile getProfile (){
        return selectedProfile;
    }

    public boolean isChCapable() {
        return chCapable;
    }
    public boolean isLmCapable() {
        return  lmCapable;

    }
    public  OTNrequest.ProfileType getProfileType (){
        return selectedType;
    }
}
