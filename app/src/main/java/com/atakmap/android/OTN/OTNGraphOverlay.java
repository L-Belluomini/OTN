package com.atakmap.android.OTN;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.widget.BaseAdapter;

import com.atakmap.android.OTN.plugin.R;
import com.atakmap.android.hierarchy.HierarchyListFilter;
import com.atakmap.android.hierarchy.HierarchyListItem;
import com.atakmap.android.hierarchy.items.MapGroupHierarchyListItem;
import com.atakmap.android.ipc.AtakBroadcast;
import com.atakmap.android.maps.DeepMapItemQuery;
import com.atakmap.android.maps.DefaultMapGroup;
import com.atakmap.android.maps.MapGroup;
import com.atakmap.android.maps.MapView;
import com.atakmap.android.maps.Marker;
import com.atakmap.android.maps.Polyline;
import com.atakmap.android.maps.Shape;
import com.atakmap.android.overlay.AbstractMapOverlay2;
import com.atakmap.android.preference.AtakPreferences;
import com.atakmap.android.user.FocusBroadcastReceiver;
import com.atakmap.coremap.log.Log;
import com.atakmap.coremap.maps.coords.GeoPoint;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class OTNGraphOverlay extends AbstractMapOverlay2 {
    private final static String TAG = "OTNgraphOverlay";
    final static String ID_COLOR_STROKE = "otn_color_stroke";
    final static String ID_COLOR_FILL = "otn_color_fill";
    private final MapView _mapView;
    private final Context _pluginContext;
    private final DefaultMapGroup _group;
    // private OTNWorldListModel listItem;
    private MapGroupHierarchyListItem currentList;
    // private MapGroup.MapItemsCallback filter;
    private final List<OTNGraph> graphs = new LinkedList<OTNGraph>();
    private final Map<String , String> bordersMap;
    private int strokeColor;
    private int fillColor;
    private AtakPreferences _prefs;

    OTNGraphOverlay(MapView mapView, Context pluginContext) {
        _pluginContext = pluginContext;
        _mapView = mapView;
        _group = new DefaultMapGroup(TAG);
        _group.setMetaString("iconUri" , "android.resource://"+  _pluginContext.getPackageName() + "/" + R.drawable.otn_logo);
        Log.d(TAG , _group.getMetaString("iconUri" , "not"));
        bordersMap = new HashMap<String , String>();
        Log.d(TAG, "created garph overlay");

        _prefs = new AtakPreferences(mapView);
        fillColor = _prefs.get ( ID_COLOR_FILL, Color.BLUE );
        strokeColor = _prefs.get ( ID_COLOR_STROKE, Color.BLUE );

        Marker point = new Marker(
                new GeoPoint(43.85866, 11.10757),
                UUID.randomUUID().toString());
        point.setType("a-u-g");
        point.setTitle("ovTest");
        point.setMetaString("callsign", "ovTest");
        point.setMetaString("title", "ovTest");

        point.setMetaBoolean("readiness", true);
        point.setMetaBoolean("archive", false);
        point.setMetaBoolean("editable", true);
        point.setMetaBoolean("movable", true);
        point.setMetaBoolean("removable", true);
        point.setMetaString("entry", "user");
        point.setShowLabel(true);

        //_group.addItem(point);


        final BroadcastReceiver selectegraphReciver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context pluginContext, Intent intent) {
                Log.d(TAG,"broadcast reciver firng");
                final String action = intent.getAction();
                if (action == null)
                    return;

                switch (action) {
                    case (OTNMapComponent.SET_GRAPHS):{
                        Bundle graphsBundle = intent.getBundleExtra("GRAPHS");
                        if (graphsBundle == null) {
                            Log.w(TAG,"failled importing bundle");
                            return;

                        }

                        List<OTNGraph> tmpgraphs = (List<OTNGraph>) graphsBundle.getSerializable("GRAPHS");
                        if ( tmpgraphs == null ) {
                            Log.w(TAG,"failled importing graph list");
                            return;
                        }

                        Bundle graphBundle = intent.getBundleExtra("GRAPH");
                        if (graphBundle == null) {
                            Log.w(TAG,"failled importing bundle");
                            return;
                        }

                        OTNGraph selectedGraph = (OTNGraph) graphBundle.getSerializable( "GRAPH" );

                        // remove old graphs
                        _group.clearItems();
                        _group.clearGroups();

                        bordersMap.clear();

                        // add new graphs
                        for ( OTNGraph graph: tmpgraphs ) {
                            Polyline tmp = graph.getBorder();

                            if ( tmp != null ) {
                                bordersMap.put( graph.getEdgeHash() , tmp.getUID() );

                                tmp.setStrokeColor(strokeColor);
                                tmp.setFillColor( fillColor);

                                // @ gabri have fun
                                tmp.setStrokeWeight( 1 ); // from 1 to 6
                                tmp.setFillAlpha(50); // 0 - 255
                                //tmp.setStyle(Shape.BASIC_LINE_STYLE_SOLID );
                                tmp.setStyle(Shape.BASIC_LINE_STYLE_DOTTED );

                                tmp.setTitle( graph.getGraphPath().substring( graph.getGraphPath().lastIndexOf("/")+1 ) );
                                tmp.setMetaString("iconUri" , "android.resource://"+  _pluginContext.getPackageName() + "/" + R.drawable.otn_logo);

                                _group.addItem(tmp);

                            } else {
                                Log.w(TAG , "graph does not have polyy");
                            }
                        }

                        break;
                    }
                    case (OTNMapComponent.FOCUS_BRODER): {
                        Log.d(TAG, "focus intne launched");
                        String borderHash = intent.getStringExtra("BorderHash");

                        if (borderHash == null){
                            Log.d(TAG, "focus NO hash" );
                            return;
                        }
                        Log.d(TAG, "focus hash" + borderHash);
                        Intent focusIntent = new Intent(FocusBroadcastReceiver.FOCUS );
                        focusIntent.putExtra("uid", bordersMap.get(borderHash) );
                        focusIntent.putExtra("useTightZoom",true);
                        AtakBroadcast.getInstance().sendBroadcast(focusIntent);
                        break;

                    }
                    case (OTNMapComponent.SET_BORDER_COLOR): {
                        strokeColor = intent.getIntExtra ( ID_COLOR_STROKE , strokeColor );
                        fillColor = intent.getIntExtra ( ID_COLOR_FILL , strokeColor );
                        _prefs.set ( ID_COLOR_FILL , fillColor );
                        _prefs.set ( ID_COLOR_STROKE , strokeColor );
                    }
                }
            }
        };

        Log.d(TAG,"setting  broadcast reciver callback");
        AtakBroadcast.DocumentedIntentFilter mcFilter = new AtakBroadcast.DocumentedIntentFilter();
        mcFilter.addAction(OTNMapComponent.SET_GRAPHS);
        mcFilter.addAction(OTNMapComponent.FOCUS_BRODER);
        AtakBroadcast.getInstance().registerReceiver(selectegraphReciver, mcFilter );
    }

    @Override
    public HierarchyListItem getListModel(BaseAdapter baseAdapter, long l, HierarchyListFilter hierarchyListFilter) {
      /* if (listItem == null)
            listItem = new OTNWorldListModel();
        listItem.refresh(baseAdapter, hierarchyListFilter);
        return listItem;

       */
        if (!MapGroupHierarchyListItem.addToObjList(this._group))
            return null;

        if (this.currentList == null || this.currentList.isDisposed() ) {
            this.currentList = new MapGroupHierarchyListItem(null, this._mapView, this._group, null, hierarchyListFilter, baseAdapter);
            Log.d(TAG, "new list");
        } else {
            this.currentList.refresh(baseAdapter, hierarchyListFilter);
            Log.d(TAG, "old lis");
        }

        return currentList;
    }

    @Override
    public String getIdentifier() {
        return TAG;
    }

    @Override
    public String getName() {
        return TAG;
    }

    @Override
    public MapGroup getRootGroup() {
        return _group;
    }

    @Override
    public DeepMapItemQuery getQueryFunction() {
        return null;
    }


}

