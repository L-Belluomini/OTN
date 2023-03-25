package com.atakmap.android.OTN;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.atak.plugins.impl.PluginLayoutInflater;
import com.atakmap.android.OTN.plugin.R;
import com.atakmap.android.dropdown.DropDownReceiver;
import com.atakmap.android.maps.MapView;
import com.atakmap.coremap.log.Log;

public class OTNDropDownGraphDetails extends DropDownReceiver {

    final static String TAG = "OTNDropDownGraphDetails";
    private View templateView;

    @Override
    protected void disposeImpl() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (action == null)
            return;

        if (action.equals(OTNMapComponent.SHOW_GRAPH_DETAIL)) {

            Log.d(TAG, "showing plugin drop down");
            showDropDown(templateView, HALF_WIDTH, FULL_HEIGHT, FULL_WIDTH,
                    HALF_HEIGHT, false, null);
        }
    }

    ////////////////// CONSTRUCTOR////////////////////

    public OTNDropDownGraphDetails(MapView mapView, Context context ){
        super(mapView);

        templateView = PluginLayoutInflater.inflate(context,
                R.layout.graph_details, null);


    }

}

