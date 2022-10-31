package com.atakmap.android.OTN;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.atakmap.android.OTN.plugin.R;
import com.atakmap.coremap.conversions.CoordinateFormat;
import com.atakmap.coremap.conversions.CoordinateFormatUtilities;

import java.util.List;

import com.atakmap.coremap.maps.coords.GeoPoint;

public class OTNwaypoitRouterOptionAdapter extends ArrayAdapter {
    private Context pContext;
    private int _resource;
    private List<GeoPoint> _waypointsList;

    public OTNwaypoitRouterOptionAdapter(@NonNull Context context, int resource, List<GeoPoint> waypointsList ) {
        super(context, resource);
        pContext = context;
        _resource = resource;
        _waypointsList=waypointsList;
    }
    @Override
    public View getView (int position , View convertView , ViewGroup parent){
        View view = convertView;

        if (view == null){
            view = LayoutInflater.from(pContext).inflate( _resource , null );
        }
        GeoPoint waypoint = (GeoPoint) getItem(position);
        TextView cordsText = view.findViewById(R.id.new_waypoint_coords);
        cordsText.setText(CoordinateFormatUtilities.formatToString (waypoint , CoordinateFormat.MGRS));

        return view;
    }

}
