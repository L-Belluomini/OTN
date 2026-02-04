
package com.atakmap.android.OTN.plugin;

import com.atak.plugins.impl.AbstractPlugin;
import com.atak.plugins.impl.PluginContextProvider;
import com.atakmap.android.OTN.OTNMapComponent;
import com.atakmap.coremap.log.Log;

import gov.tak.api.plugin.IServiceController;

/**
 * OTN Plugin Lifecycle for ATAK 5.6.0
 * Updated from AbstractPluginLifecycle to AbstractPlugin for new plugin API
 */
public class OTNLifecycle extends AbstractPlugin {

    private final static String TAG = "OTNLifecycle";

    /**
     * Constructor for ATAK 5.x plugin API
     * @param serviceController The service controller provided by ATAK
     */
    public OTNLifecycle(IServiceController serviceController) {
        super(serviceController,
              new OTNTool(serviceController.getService(PluginContextProvider.class).getPluginContext()),
              new OTNMapComponent());
        Log.d(TAG, "OTN Plugin initialized for ATAK 5.6.0");
    }

}
