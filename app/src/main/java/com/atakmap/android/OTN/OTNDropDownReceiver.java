
package com.atakmap.android.OTN;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

//import androidx.recyclerview.widget.RecyclerView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.atak.plugins.impl.PluginLayoutInflater;
import com.atakmap.android.OTN.plugin.R;
import com.atakmap.android.dropdown.DropDown.OnStateListener;
import com.atakmap.android.dropdown.DropDownReceiver;
import com.atakmap.android.ipc.AtakBroadcast;
import com.atakmap.android.maps.MapView;
import com.atakmap.coremap.log.Log;
import com.graphhopper.GraphHopper;
import com.graphhopper.util.Constants;

import java.util.List;

public class OTNDropDownReceiver extends DropDownReceiver implements
        OnStateListener {

    public static final String TAG = OTNDropDownReceiver.class
            .getSimpleName();

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

        ImageButton refreshButton =  templateView.findViewById(R.id.refresh_button);
        if (refreshButton == null) {
            Log.w( TAG , "refresh button not working");
        }
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG , "refreshcallback");
                Intent intent = new Intent( OTNMapComponent.FIND_GRAPHS);
                AtakBroadcast.getInstance().sendBroadcast(intent);
            }
        });

        ImageButton infoBtton =templateView.findViewById( R.id.help_button);
        infoBtton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder getinfoADB = new AlertDialog.Builder(MapView._mapView.getContext()) ;
                getinfoADB.setTitle("OTN info");
                View infoview = (View) LayoutInflater.from( pluginContext ).inflate( R.layout.otnhelpdialog, null );
                TextView gh_versionTV = infoview.findViewById(R.id.gh_version);
                gh_versionTV.setText (
                        "GraphHopper version: " + Constants.VERSION);

                TextView builddateTV = infoview.findViewById(R.id.otn_build_date);
                builddateTV.setText(
                        "Build date: " + Constants.BUILD_DATE );

                /*        + "Color of profile indicates type of profile" + "\n"
                        + "red : Only ch" +"\n"
                        + "cyan : Both" +"\n"
                        + "blue : Only lm" +"\n"
                        + "white : normal" */


                        //+ "OTN verrsion " + System.print(  )

                getinfoADB.setView(infoview);
                AlertDialog infoAD = getinfoADB.create();
                infoAD.show();
            }
        });




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
            case (OTNMapComponent.SHOW_PLUGIN): {
                Log.d(TAG, "showing plugin drop down");
                showDropDown(templateView, HALF_WIDTH, FULL_HEIGHT, FULL_WIDTH,
                    HALF_HEIGHT, false, this);
                break;
            }
            case (OTNMapComponent.SET_GRAPHS):{
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

                Bundle graphBundle = intent.getBundleExtra("GRAPH");
                if (graphBundle == null) {
                    Log.w(TAG,"failled importing bundle");
                    return;
                }

                OTNGraph selectedGraph = (OTNGraph) graphBundle.getSerializable( "GRAPH" );

                Toast.makeText(pluginContext, "OTN graph list updated, found " + Integer.toString(graphs.size()) + " graphs", Toast.LENGTH_LONG).show();

                ListView graphListView = (ListView) templateView.findViewById(R.id.graph_list);
                ArrayAdapter graphListAdapter = new OTNGraphAdapter(pluginContext , R.layout.graph_listitem , graphs , selectedGraph );
                graphListView.setAdapter(graphListAdapter);

                /*graphListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        OTNGraph graph = (OTNGraph) adapterView.getAdapter().getItem(i);
                        Log.d(TAG,"list item click" + graph.getGraphPath() );
                        Intent showBorderIntent = new Intent( OTNMapComponent.FOCUS_BRODER);
                        showBorderIntent.putExtra("BorderHash", graph.getEdgeHash() );
                        AtakBroadcast.getInstance().sendBroadcast(showBorderIntent);
                    }
                });*/

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



}
