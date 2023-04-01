package com.atakmap.android.OTN;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.atakmap.android.OTN.plugin.R;
import com.atakmap.android.ipc.AtakBroadcast;
import com.atakmap.android.maps.MapView;
import com.atakmap.coremap.conversions.Area;
import com.atakmap.coremap.conversions.AreaUtilities;
import com.atakmap.coremap.log.Log;

import java.io.Serializable;
import java.util.List;


import com.graphhopper.config.Profile;

public class OTNGraphAdapter extends ArrayAdapter {
    public static final String TAG = OTNDropDownReceiver.class.getSimpleName();
    private Context pContext;
    private int resource;
    private List<OTNGraph> graphs ;
    private OTNGraph selectedGraph;

    public OTNGraphAdapter(@NonNull Context pContext, int resource , List<OTNGraph> graphs , OTNGraph selectedGraph ) {
        super(pContext , resource , graphs );
        this.graphs = graphs;
        this.selectedGraph = selectedGraph;
        this.pContext = pContext;
        this.resource = resource;

    }
    /*@Override
    public View getChildView(int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.pContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.profiles_listitem, null);
        }
        final String childText = (String) getChild(groupPosition, childPosition);
        TextView txtListChild = (TextView) convertView
                .findViewById(R.id.profile_name);

        txtListChild.setText(childText);
        return convertView;
    }*/

    @Override
    public View getView (int position , View convertView , ViewGroup parent){
        View view = convertView;

        if (view == null){
            view = LayoutInflater.from(pContext).inflate( resource , null );
        }
        OTNGraph graph = (OTNGraph) getItem ( position );
        if (graph == null ) {
            return view;
        }

        TextView nameTV = view.findViewById(R.id.graph_name);
        if ( nameTV != null ){
            nameTV.setText( graph.getGraphPath().substring( graph.getGraphPath().lastIndexOf("/")+1 ) );
        }

        // graph select dialog
        AlertDialog.Builder setAdBuilder = new AlertDialog.Builder(MapView._mapView.getContext()) ;
        setAdBuilder.setTitle("Set graph");
        setAdBuilder.setMessage("Are you sure to set" + graph.getGraphPath() + "?" );
        setAdBuilder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Intent intent = new Intent( OTNMapComponent.SET_SELECTED_GRAPH);
                Bundle bundle = new Bundle();
                bundle.putSerializable("GRAPH" ,(Serializable) getItem(position) );
                //Log.d("OTNdropDownButton" , bundle.toString());
                intent.putExtra("GRAPH", bundle );
                AtakBroadcast.getInstance().sendBroadcast(intent);
            }
        });
        setAdBuilder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });
        // sellect callback
        ImageButton selecetdGraphButton = view.findViewById(R.id.graph_button_select);


            selecetdGraphButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog dialog = setAdBuilder.create();
                    dialog.show();
                }
            });

            selecetdGraphButton.setImageResource(R.drawable.btn_check_empty);
            Log.d(TAG, "selectedgraph null");
            // magae seleted graph UI
            if ( selectedGraph != null ) {
                Log.d(TAG, "selGraph " + selectedGraph.getEdgeHash());
                if (graph.getEdgeHash().equals(selectedGraph.getEdgeHash())) {
                    Log.d(TAG, "setting selceted graph button");
                    selecetdGraphButton.setImageResource(R.drawable.btn_check_selected);
                } else {

                }
            }


        LinearLayout profilesLayout =(LinearLayout) view.findViewById(R.id.profile_list);
        profilesLayout.removeAllViews();
        boolean isch = false;
        for (Profile profile: graph.getConfigGH().getProfiles() ) {
            View profileView = LayoutInflater.from(pContext).inflate( R.layout.profiles_listitem , null );
            TextView profileName = profileView.findViewById(R.id.profile_name);
            profileName.setText( profile.getName() );
            if ( graph.isProfileCH( profile ) ) {
                profileName.setTextColor(Color.RED);
                isch = true;
            }
            if ( graph.isProfilelm( profile ) ) {
                if (isch) {
                    profileName.setTextColor(Color.CYAN);
                } else {
                    profileName.setTextColor(Color.BLUE);
                }
            }
            profilesLayout.addView(profileView);
        }


        Button focusBorder = (Button) view.findViewById(R.id.btn_focus_borders);
        focusBorder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG,"border button" + graph.getGraphPath() );
                Intent showBorderIntent = new Intent( OTNMapComponent.FOCUS_BRODER);
                showBorderIntent.putExtra("BorderHash", graph.getEdgeHash() );
                AtakBroadcast.getInstance().sendBroadcast(showBorderIntent);
            }
        });

        ImageButton showInfoB = view.findViewById(R.id.btn_graph_info);


        showInfoB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Log.d(TAG , "launching graph detail");
                Intent intent = new Intent( OTNMapComponent.SHOW_GRAPH_DETAIL);
                AtakBroadcast.getInstance().sendBroadcast(intent);*/

                AlertDialog.Builder getinfoADB = new AlertDialog.Builder(MapView._mapView.getContext()) ;
                getinfoADB.setTitle("graph info");
                View infoview = (View) LayoutInflater.from(pContext).inflate( R.layout.graphinfodialog, null );

                TextView profilesTV = infoview.findViewById(R.id.configGH_profiles_list);
                profilesTV.setText (
                        graph.getConfigGH().getProfiles().toString()
                );

                TextView profilesCHTV = infoview.findViewById(R.id.configGH_CH_profiles_list);
                profilesCHTV.setText (
                        graph.getConfigGH().getCHProfiles().toString()
                );

                TextView profilesLMTV = infoview.findViewById(R.id.configGH_LM_profiles_list);
                profilesLMTV.setText (
                        graph.getConfigGH().getLMProfiles().toString()
                );

                TextView edgesTV = infoview.findViewById(R.id.edge_numbers);
                edgesTV.setText (
                        "Total graph edges: "+ Integer.toString( graph.getEdges() )
                );

                TextView nodesTV = infoview.findViewById(R.id.nodes_numbers);
                nodesTV.setText (
                        "Total graph nodes: "+ Integer.toString( graph.getNodes() )
                );

                TextView bbTV = infoview.findViewById(R.id.graph_bound_box);
                bbTV.setText (
                        "Graph bounding box: "+ Integer.toString( graph.getEdges() )
                );

                TextView areaTV = infoview.findViewById(R.id.graph_bound_box_area);
                areaTV.setText (
                        "Area: " + AreaUtilities.formatArea (Area.METRIC , graph.getArea() ,Area.METER2  )  ); //  todo add area preference and test with no poly

                               /* + "\n"
                        + "edge number: "+ Integer.toString( graph.getEdges() ) + "\n"
                        + "nodes number: " + Integer.toString( graph.getNodes() ) + "\n"
                        + "Bound box: " + graph.getBondBox().toString() + "\n"
                                + " area: " + AreaUtilities.formatArea (Area.METRIC , graph.getArea() ,Area.METER2  )  ); //  todo add area preference
*/
                getinfoADB.setView(infoview);
                AlertDialog infoAD = getinfoADB.create();
                infoAD.show();
            }
        });

        return view;
    }
}
