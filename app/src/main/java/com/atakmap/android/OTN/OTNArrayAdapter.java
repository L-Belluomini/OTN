package com.atakmap.android.OTN;


import android.content.Context;
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

public class OTNArrayAdapter extends ArrayAdapter {
    public static final String TAG = OTNDropDownReceiver.class.getSimpleName();
    private Context pContext;
    private int resource;
    public OTNArrayAdapter(@NonNull Context pContext, int resource , List<OTNGraph> graphs , OTNGraph selectedGraph ) {
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
        TextView nameTV = view.findViewById(R.id.graph_name);
        if ( nameTV != null ){
            nameTV.setText( graph.getGraphPath() );
        }
        Button selecetdGraphButton = view.findViewById(R.id.graph_button_select);
        if ( selecetdGraphButton != null) {
            selecetdGraphButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent( OTNDropDownReceiver.SET_GRAPHS);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("GRAPH" ,(Serializable) getItem(position) );
                    Log.d("OTNdropDownButton" , bundle.toString());
                    intent.putExtra("GRAPH", bundle );
                    AtakBroadcast.getInstance().sendBroadcast(intent);
                }
            });
            ListView profilesLayout = view.findViewById(R.id.available_profiles);

            ArrayAdapter profilesAdapter = new ArrayAdapter(pContext , android.R.layout.simple_list_item_1 , graph.getConfigGH().getProfiles() );
            profilesLayout.setAdapter(profilesAdapter);
        }

        return view;
    }
}
