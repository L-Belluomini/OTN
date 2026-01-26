
package com.atakmap.android.otn.plugin;

import com.atak.plugins.impl.AbstractPlugin;
import com.atak.plugins.impl.PluginContextProvider;
import com.atakmap.android.otn.OTNMapComponent;

import gov.tak.api.plugin.IServiceController;

public class OTNLifeCycle extends AbstractPlugin {

    public OTNLifeCycle(IServiceController serviceController) {
        super(serviceController, new OTNTool(serviceController.getService(PluginContextProvider.class).getPluginContext()), new OTNMapComponent());
    }
}
