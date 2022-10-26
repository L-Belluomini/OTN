package com.atakmap.android.OTN;


import android.app.AlertDialog;
import android.app.ExpandableListActivity;
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
import android.widget.ExpandableListAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.atakmap.android.OTN.plugin.R;
import com.atakmap.android.ipc.AtakBroadcast;
import com.atakmap.android.maps.MapView;
import com.atakmap.coremap.log.Log;

import java.io.Serializable;
import java.util.List;


import com.graphhopper.config.CHProfile;
import com.graphhopper.config.LMProfile;
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
        AlertDialog.Builder setAdBuilder = new AlertDialog.Builder(MapView._mapView.getContext()) ;
        setAdBuilder.setTitle("Set graph");
        setAdBuilder.setMessage("Are you sure to set" + graph.getGraphPath() + "?" );
        setAdBuilder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Intent intent = new Intent( OTNDropDownReceiver.SET_SELECTED_GRAPH);
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

        TextView nameTV = view.findViewById(R.id.graph_name);
        if ( nameTV != null ){
            nameTV.setText( graph.getGraphPath().substring( graph.getGraphPath().lastIndexOf("/") ) );
        }

        ImageButton selecetdGraphButton = view.findViewById(R.id.graph_button_select);
        if ( selecetdGraphButton != null) {

            selecetdGraphButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog dialog = setAdBuilder.create();
                    dialog.show();
                }
            });

            if ( ! ( selectedGraph == null) ){
                Log.d(TAG,"selGraph " +selectedGraph.getEdgeHash());
                if ( graph.getEdgeHash().equals(selectedGraph.getEdgeHash() )) {
                    Log.d(TAG, "setting selceted graph button");
                    selecetdGraphButton.setSelected(true); //doesn't work as intended, shows every graph as selected
                }
            }else {
                selecetdGraphButton.setSelected(false);
                Log.d(TAG,"selectedgraph null");
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
        }

        return view;
    }
}
