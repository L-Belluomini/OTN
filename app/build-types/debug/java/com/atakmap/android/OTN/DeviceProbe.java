package com.atakmap.android.OTN;

import android.content.Context;
import android.preference.PreferenceManager;
import com.atakmap.android.maps.PointMapItem;
import com.atakmap.android.routes.*;
import com.atakmap.coremap.filesystem.FileSystemUtils;
import com.atakmap.coremap.maps.coords.GeoPoint;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import org.json.*;

/** One-shot, opt-in synthetic graph probe. Absent from release builds. */
public final class DeviceProbe {
    public static void maybeRun(Context context, RoutePlannerManager manager, OTNGraph graph) {
        if (graph == null || !"/OTN/graphs/Codex-Test-Grid".equals(graph.getGraphPath())) return;
        File dir = FileSystemUtils.getItem(FileSystemUtils.TOOL_DATA_DIRECTORY + graph.getGraphPath());
        File flag = new File(dir, "RUN_DEVICE_PROBE");
        if (!flag.isFile() || !flag.delete()) return;
        RoutePlannerInterface2 planner = OTNMapComponent.plannerById(manager, "OTNOFFlineFast");
        if (planner == null) return;
        RouteGenerationTask task = planner.getRouteGenerationTask(null);
        task.setAlertOnCreation(false);
        new Thread(() -> {
            JSONObject report = new JSONObject();
            try {
                report.put("planner", planner.getUniqueIdenfier());
                report.put("requiresNetwork", planner.isNetworkRequired());
                List<GeoPoint> via = Collections.unmodifiableList(Arrays.asList(new GeoPoint(.004,.001)));
                List<String> fingerprints = new ArrayList<>();
                for (int attempt = 0; attempt < 2; attempt++) {
                    RoutePointPackage result = task.generateRoute(PreferenceManager.getDefaultSharedPreferences(context),
                            new GeoPoint(.001,.001), new GeoPoint(.004,.004), via);
                    if (result == null || result.getError() != null) throw new IllegalStateException(result == null ? "No result" : result.getError());
                    List<PointMapItem> points = result.getRoutePoints();
                    if (points.size() < 3 || result.getPointCues().size() < 3) throw new IllegalStateException("Missing points or cues");
                    Set<String> ids = new HashSet<>();
                    StringBuilder fingerprint = new StringBuilder();
                    for (PointMapItem point : points) {
                        if (!point.getPoint().isValid()) throw new IllegalStateException("Invalid waypoint coordinate");
                        ids.add(point.getUID());
                        fingerprint.append(point.getPoint().getLatitude()).append(',').append(point.getPoint().getLongitude()).append(';');
                    }
                    if (!ids.containsAll(result.getPointCues().keySet())) throw new IllegalStateException("Cue references omitted waypoint");
                    if (Math.abs(points.get(0).getPoint().getLatitude()-.001)>0.00001 ||
                            Math.abs(points.get(points.size()-1).getPoint().getLongitude()-.004)>0.00001)
                        throw new IllegalStateException("Wrong endpoints");
                    fingerprints.add(fingerprint.toString());
                    report.put("points", points.size());
                    report.put("cues", result.getPointCues().size());
                }
                if (!fingerprints.get(0).equals(fingerprints.get(1)) || via.size()!=1)
                    throw new IllegalStateException("Repeated request changed route inputs");
                RoutePointPackage outside = task.generateRoute(PreferenceManager.getDefaultSharedPreferences(context),
                        new GeoPoint(50,50), new GeoPoint(51,51), Collections.emptyList());
                if (outside.getError() == null) throw new IllegalStateException("Outside-graph route unexpectedly succeeded");
                report.put("repeatedRequestStable", true);
                report.put("cueReferencesValid", true);
                report.put("outsideGraphRejected", true);
                report.put("passed", true);
            } catch (Throwable error) {
                try { report.put("passed", false); report.put("error", android.util.Log.getStackTraceString(error)); }
                catch (JSONException ignored) {}
            }
            try { Files.write(new File(dir, "device-probe.json").toPath(), report.toString(2).getBytes(StandardCharsets.UTF_8)); }
            catch (Exception error) { android.util.Log.e("OTNDeviceProbe", "Could not save probe", error); }
        }, "OTN synthetic graph probe").start();
    }
}
