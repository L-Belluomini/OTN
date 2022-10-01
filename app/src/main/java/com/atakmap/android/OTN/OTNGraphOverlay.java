package com.atakmap.android.OTN;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.atakmap.android.OTN.plugin.R;
import com.atakmap.android.hierarchy.HierarchyListFilter;
import com.atakmap.android.hierarchy.HierarchyListItem;
import com.atakmap.android.hierarchy.action.Search;
import com.atakmap.android.hierarchy.action.Visibility;
import com.atakmap.android.hierarchy.action.Visibility2;
import com.atakmap.android.hierarchy.items.AbstractHierarchyListItem2;
import com.atakmap.android.hierarchy.items.MapGroupHierarchyListItem;
import com.atakmap.android.ipc.AtakBroadcast;
import com.atakmap.android.maps.DeepMapItemQuery;
import com.atakmap.android.maps.DefaultMapGroup;
import com.atakmap.android.maps.MapGroup;
import com.atakmap.android.maps.MapView;
import com.atakmap.android.maps.Marker;
import com.atakmap.android.maps.Polyline;
import com.atakmap.android.overlay.AbstractMapOverlay2;
import com.atakmap.coremap.log.Log;
import com.atakmap.coremap.maps.coords.GeoPoint;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class OTNGraphOverlay extends AbstractMapOverlay2 {
    private final static String TAG = "OTNgraphOverlay";
    private MapView _mapView;
    private Context _pluginContext;
    private DefaultMapGroup _group;
    // private OTNWorldListModel listItem;
    private MapGroupHierarchyListItem currentList;
    // private MapGroup.MapItemsCallback filter;
    private List<OTNGraph> grapsh = new LinkedList<OTNGraph>();

    OTNGraphOverlay(MapView mapView, Context pluginContext) {
        _pluginContext = pluginContext;
        _mapView = mapView;
        _group = new DefaultMapGroup(TAG);
        _group.setMetaString("iconURI" , "android.resource://" + _pluginContext.getPackageName()
                + "/" + R.drawable.ic_launcher);
        Log.d(TAG, "created garph overlay");

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
                    case (OTNDropDownReceiver.SET_GRAPHS):{
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

                        for ( OTNGraph graph: graphs ) {
                            Polyline tmp =graph.getBorder();
                            if ( tmp != null ) {
                                _group.addItem(tmp);
                            } else {
                                Log.w(TAG , "graph does not have polyy");
                            }
                        }

                        break;
                    }
                }
            }
        };
        Log.d(TAG,"setting  broadcast reciver callback");
        AtakBroadcast.DocumentedIntentFilter mcFilter = new AtakBroadcast.DocumentedIntentFilter();
        mcFilter.addAction(OTNDropDownReceiver.FIND_GRAPHS);
        mcFilter.addAction(OTNDropDownReceiver.SET_GRAPHS);
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

        if (this.currentList == null || this.currentList.isDisposed())
            this.currentList = new MapGroupHierarchyListItem(null,
                    this._mapView, this._group, null , hierarchyListFilter,
                    baseAdapter );
        else
            this.currentList.refresh(baseAdapter, hierarchyListFilter);

        return this.currentList;
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
    public MapGroup getMapGroup() {
        return _group;
    }
/*
    public class OTNWorldListModel extends AbstractHierarchyListItem2
            implements Search, Visibility2, View.OnClickListener {

        private final static String TAG = "OTNWorldListModel";

        private View _header, _footer;

        public OTNWorldListModel() {
            this.asyncRefresh = true;
        }

        @Override
        public String getTitle() {
            return OTNGraphOverlay.this.getName();
        }

        @Override
        public String getIconUri() {
            return "android.resource://" + _pluginContext.getPackageName()
                    + "/" + R.drawable.ic_launcher;

        }

        public int getPreferredListIndex() {
            return 5;
        }

        @Override
        public int getDescendantCount() {
            return 0;
        }

        @Override
        public Object getUserObject() {
            return this;
        }

        @Override
        public View getExtraView() {
            return null;
        }

        @Override
        public View getHeaderView() {
            if (_header == null) {
                _header = LayoutInflater.from(_pluginContext).inflate(
                        R.layout.overlay_header, _mapView, false);
                _header.findViewById(R.id.header_button)
                        .setOnClickListener(this);
            }
            return _header;
        }

        @Override
        public View getFooterView() {
            if (_footer == null) {
                _footer = LayoutInflater.from(_pluginContext).inflate(
                        R.layout.overlay_footer, _mapView, false);
                _footer.findViewById(R.id.footer_button)
                        .setOnClickListener(this);
            }
            return _footer;
        }

        @Override
        public void refreshImpl() {

            List<HierarchyListItem> filtered = new ArrayList<>();

            List<ExampleLayer> layers = getLayers();
            for (ExampleLayer l : layers) {
                LayerHierarchyListItem item = new LayerHierarchyListItem(l);
                if (this.filter.accept(item))
                    filtered.add(item);
            }
            List<ExampleMultiLayer> multilayers = getMultiLayers();
            for (ExampleMultiLayer ml : multilayers) {
                LayerHierarchyListItem item = new LayerHierarchyListItem(ml);
                if (this.filter.accept(item))
                    filtered.add(item);
            }

            // Sort
            sortItems(filtered);

            // Update
            updateChildren(filtered);

        }



        @Override
        public void dispose() {
            disposeChildren();
        }

        @Override
        public boolean hideIfEmpty() {
            return true;
        }

        @Override
        public boolean isMultiSelectSupported() {
            return false;
        }

        @Override
        public boolean setVisible(boolean visible) {
            List<Visibility> actions = getChildActions(Visibility.class);
            boolean ret = !actions.isEmpty();
            for (Visibility del : actions)
                ret &= del.setVisible(visible);
            return ret;
        }

        @Override
        public Set<HierarchyListItem> find(String searchTerms) {
            searchTerms = searchTerms.toLowerCase();
            Set<HierarchyListItem> results = new HashSet<>();
            List<HierarchyListItem> items = getChildren();
            for (HierarchyListItem item : items) {
                if (item.getTitle().toLowerCase().contains(searchTerms))
                    results.add(item);
            }
            return results;
        }

        @Override
        public void onClick(View v) {
            if (v instanceof Button)
                Toast.makeText(_mapView.getContext(),
                        ((Button) v).getText(),
                        Toast.LENGTH_LONG).show();
        }
    }

    //public  class OTNmapgGRoup extends MapGroup {

    //}

 */
}

