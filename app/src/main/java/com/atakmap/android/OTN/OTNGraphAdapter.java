package com.atakmap.android.OTN;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.atakmap.android.OTN.plugin.R;
import com.atakmap.android.ipc.AtakBroadcast;
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
    public OTNGraphAdapter(@NonNull Context pContext, int resource , List<OTNGraph> graphs , OTNGraph selectedGraph ) {
        super(pContext, resource, graphs);
        this.pContext = pContext;
        this.resource = resource;

    }
    @Override
    public View getView (int position , View convertView , ViewGroup parent){
        View view = convertView;

        if (view == null){
            view = LayoutInflater.from(pContext).inflate( resource , null );
        }
        OTNGraph graph = (OTNGraph) getItem( position );
        if (graph == null ) {
            return view;
        }
        AlertDialog.Builder setAdBuilder = new AlertDialog.Builder(pContext) ;
        setAdBuilder.setTitle("Set graph");
        setAdBuilder.setMessage("Are you sure to set" + graph.getGraphPath() + "?" );
        setAdBuilder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Intent intent = new Intent( OTNDropDownReceiver.SET_GRAPHS);
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
            nameTV.setText( graph.getGraphPath() );
        }
        Button selecetdGraphButton = view.findViewById(R.id.graph_button_select);
        if ( selecetdGraphButton != null) {
            selecetdGraphButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog dialog = setAdBuilder.create();
                }
            });
            ListView profilesLayout = view.findViewById(R.id.profile_list);

            ArrayAdapter profilesAdapter = new OTNProfileAdapter(pContext , R.layout.profiles_listitem , graph );
            profilesLayout.setAdapter(profilesAdapter);
        }

        return view;
    }
}
