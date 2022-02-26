
package com.atakmap.android.OTN;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

//import androidx.recyclerview.widget.RecyclerView;

import com.atak.plugins.impl.PluginLayoutInflater;
import com.atakmap.android.OTN.plugin.R;
import com.atakmap.android.dropdown.DropDown.OnStateListener;
import com.atakmap.android.dropdown.DropDownReceiver;
import com.atakmap.android.maps.MapView;
import com.atakmap.coremap.log.Log;

import java.util.List;

public class OTNDropDownReceiver extends DropDownReceiver implements
        OnStateListener {

    public static final String TAG = OTNDropDownReceiver.class
            .getSimpleName();

    public static final String SHOW_PLUGIN = "com.atakmap.android.OTN.SHOW_PLUGIN";
    public static final String SET_GRAPHS = "com.atakmap.android.OTN.SET_GRAPHS";
    private final View templateView;
    private final Context pluginContext;
    private MapView _view;


    /**************************** CONSTRUCTOR *****************************/

    public OTNDropDownReceiver(final MapView mapView,
                               final Context context) {
        super(mapView);
        this._view = mapView;
        this.pluginContext = context;

        // Remember to use the PluginLayoutInflator if you are actually inflating a custom view
        // In this case, using it is not necessary - but I am putting it here to remind
        // developers to look at this Inflator
        templateView = PluginLayoutInflater.inflate(context,
                R.layout.main_layout, null);
        updateView();

    }

    /**************************** PUBLIC METHODS *****************************/

    public void disposeImpl() {
    }

    /**************************** INHERITED METHODS *****************************/

    @Override
    public void onReceive(Context context, Intent intent) {

        final String action = intent.getAction();
        if (action == null)
            return;

        switch (action){
            case (SHOW_PLUGIN): {
                Log.d(TAG, "showing plugin drop down");
                showDropDown(templateView, HALF_WIDTH, FULL_HEIGHT, FULL_WIDTH,
                    HALF_HEIGHT, false, this);
                break;
            }
            case (SET_GRAPHS):{
                                Bundle graphsBundle = intent.getBundleExtra("GRAPHS");
                if (graphsBundle == null) {
                    Log.w(TAG,"failled importing bundle");
                    return;

                }
                List<OTNGraph> graphs = (List<OTNGraph>) graphsBundle.getSerializable("GRAPHS");
                if ( graphs == null ) {
                    Log.w(TAG,"failled importing graph list");
                    return;
                }
                ListView graphListView = (ListView) templateView.findViewById(R.id.graphList);
                ArrayAdapter graphListAdapter = new ArrayAdapter<>(pluginContext , R.layout.graph_textview , graphs);
                graphListView.setAdapter(graphListAdapter);


                //text.setText( graphs.get(0).getGraphPath() +" dir and profile name:"+graphs.get(0).getConfigGH().getProfiles().get(0).getName() );
                break;

            }
        }

    }

    @Override
    public void onDropDownSelectionRemoved() {
    }

    @Override
    public void onDropDownVisible(boolean v) {
    }

    @Override
    public void onDropDownSizeChanged(double width, double height) {
    }

    @Override
    public void onDropDownClose() {
    }


    // cusotm methods

    private void updateView() {
        Context appContext = _view.getContext();

         OTNMapComponent component = OTNMapComponent.getInstance();

        if (component == null) {
            Log.w(TAG , "not found map component");
            return;
        }

        Log.d(TAG , component.toString());
    }
}
