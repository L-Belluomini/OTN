package com.atakmap.android.OTN;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.atakmap.android.OTN.plugin.R;

import java.util.List;

public class OTNArrayAdapter extends ArrayAdapter {
    private Context pContext;
    private int resource;
    public OTNArrayAdapter(@NonNull Context pContext, int resource , List<OTNGraph> graphs ) {
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


        return view;
    }
}
