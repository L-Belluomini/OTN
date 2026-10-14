package com.atakmap.android.OTN.plugin;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import com.atak.plugins.impl.PluginContextProvider;
import com.atakmap.android.OTN.OTNMapComponent;
import com.atakmap.android.ipc.AtakBroadcast;
import com.atakmap.android.maps.MapView;
import gov.tak.api.plugin.IPlugin;
import gov.tak.api.plugin.IServiceController;
import gov.tak.api.ui.IHostUIService;
import gov.tak.api.ui.ToolbarItem;
import gov.tak.api.ui.ToolbarItemAdapter;
import gov.tak.platform.marshal.MarshalManager;

/** Modern ATAK lifecycle; the existing OTN setup and planner remain in the host process. */
public final class OTNPlugin implements IPlugin {
    private final Context context;
    private final MapView map;
    private final IHostUIService ui;
    private final ToolbarItem toolbar;
    private OTNMapComponent component;
    public OTNPlugin(IServiceController services) {
        context = services.getService(PluginContextProvider.class).getPluginContext();
        map = services.getService(MapView.class);
        ui = services.getService(IHostUIService.class);
        context.setTheme(R.style.ATAKPluginTheme);
        toolbar = new ToolbarItem.Builder(context.getString(R.string.app_name),
                MarshalManager.marshal(context.getDrawable(R.drawable.otn_logo), Drawable.class,
                        gov.tak.api.commons.graphics.Bitmap.class))
                .setListener(new ToolbarItemAdapter() {
                    @Override public void onClick(ToolbarItem item) {
                        AtakBroadcast.getInstance().sendBroadcast(new Intent(OTNMapComponent.SHOW_PLUGIN));
                    }
                }).build();
    }
    @Override public void onStart() {
        if (component != null) return;
        component = new OTNMapComponent();
        component.onCreate(context, new Intent(), map);
        ui.addToolbarItem(toolbar);
    }
    @Override public void onStop() {
        ui.removeToolbarItem(toolbar);
        if (component != null) { component.onDestroy(context, map); component = null; }
    }
}
