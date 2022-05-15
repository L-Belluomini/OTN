package com.atakmap.android.OTN;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.atakmap.android.OTN.plugin.R;
import com.atakmap.android.ipc.AtakBroadcast;
import com.atakmap.coremap.log.Log;
import com.graphhopper.config.Profile;

import java.io.Serializable;
import java.util.List;

public class OTNProfileAdapter extends ArrayAdapter {
    public static final String TAG = OTNDropDownReceiver.class.getSimpleName();
    private Context pContext;
    private int resource;
    private OTNGraph graph;

    public OTNProfileAdapter(@NonNull Context pContext, int resource , OTNGraph graph  ) {
        super(pContext, resource, graph.getConfigGH().getProfiles());
        this.graph = graph;
        this.pContext = pContext;
        this.resource = resource;

    }

    @Override
    public View getView (int position , View convertView , ViewGroup parent){
        View view = convertView;

        if (view == null){
            view = LayoutInflater.from(pContext).inflate( resource , null );
        }
        Profile profile = (Profile) getItem( position );
        if (profile == null ) {
            return view;
        }
        TextView nameTV = view.findViewById(R.id.profile_name);
        if ( nameTV != null ){
            nameTV.setText( profile.getName() );
        }


        return view;
    }
}
