package com.atakmap.android.OTN.router;

import android.content.SharedPreferences;
import android.util.Log;
import com.atakmap.android.OTN.OTNGraph;
import com.atakmap.android.OTN.OTNMapComponent;
import com.atakmap.android.OTN.OTNrequest;
import com.atakmap.android.drawing.mapItems.DrawingCircle;
import com.atakmap.android.maps.Marker;
import com.atakmap.android.maps.PointMapItem;
import com.atakmap.android.maps.Shape;
import com.atakmap.android.routes.Route;
import com.atakmap.android.routes.RouteGenerationTask;
import com.atakmap.android.routes.RoutePointPackage;
import com.atakmap.android.routes.nav.NavigationCue;
import com.atakmap.android.routes.routearound.RouteAroundRegionManager;
import com.atakmap.coremap.maps.coords.GeoPoint;
import com.atakmap.coremap.maps.coords.GeoPointMetaData;
import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.GraphHopperConfig;
import com.graphhopper.util.Parameters;
import com.graphhopper.util.shapes.GHPoint;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class OTNOfflineroutingTask extends RouteGenerationTask {
    private static final String TAG = "OTNOfflineroutingTask";
    private final OTNGraph graph;
    private final OTNrequest takRequest;
    private final List<GeoPoint> waypoints;

    public OTNOfflineroutingTask(RouteGenerationEventListener listener, OTNGraph graph, OTNrequest request) {
        this(listener, graph, request, Collections.emptyList());
    }
    public OTNOfflineroutingTask(RouteGenerationEventListener listener, OTNGraph graph,
                                OTNrequest request, List<GeoPoint> waypoints) {
        super(listener);
        this.graph = graph;
        this.takRequest = request;
        this.waypoints = copyPoints(waypoints);
    }

    private static List<GeoPoint> copyPoints(List<GeoPoint> source) {
        List<GeoPoint> copy = new ArrayList<>();
        if (source != null) for (GeoPoint point : source) copy.add(new GeoPoint(point));
        return copy;
    }

    @Override public RoutePointPackage generateRoute(SharedPreferences prefs, GeoPoint origin,
                                                    GeoPoint destination, List<GeoPoint> via) {
        if (graph == null || takRequest == null)
            return new RoutePointPackage("Select an OTN graph with a routing profile on the phone");
        if (origin == null || destination == null || !origin.isValid() || !destination.isValid())
            return new RoutePointPackage("Route endpoints are unavailable");
        GraphHopper hopper = new GraphHopper();
        try {
            if (isCancelled()) return new RoutePointPackage("Route generation cancelled");
            GraphHopperConfig config = graph.getConfigGH();
            config.putObject("graph.dataaccess", prefs.getBoolean(OTNMapComponent.GRAPH_MEMORY_MAP, false)
                    ? "RAM_STORE" : "MMAP");
            hopper.init(config);
            if (!hopper.load()) return new RoutePointPackage("OTN graph could not be loaded");

            // ATAK's supplied via points take precedence over the phone's planner options.
            // Never modify either list: ATAK reuses these during rerouting.
            List<GeoPoint> intermediate = via == null || via.isEmpty() ? waypoints : copyPoints(via);
            GHRequest request = new GHRequest();
            request.addPoint(new GHPoint(origin.getLatitude(), origin.getLongitude()));
            for (GeoPoint point : intermediate) {
                if (!point.isValid()) return new RoutePointPackage("Invalid intermediate route point");
                request.addPoint(new GHPoint(point.getLatitude(), point.getLongitude()));
            }
            request.addPoint(new GHPoint(destination.getLatitude(), destination.getLongitude()));
            request.setProfile(takRequest.getProfile().getName());
            String language = prefs.getString(OTNMapComponent.INSTRUCTION_LANG, "en_us");
            request.setLocale(Locale.forLanguageTag(language == null ? "en-US" : language.replace('_', '-')));
            request.putHint("instructions", true);

            List<Shape> regions = new ArrayList<>(RouteAroundRegionManager.getInstance().getRegions());
            if (!regions.isEmpty()) {
                // GraphHopper 6 can wrap any base profile's weighting with block_area.
                // This also works for custom profiles, without replacing their own custom model.
                request.putHint(Parameters.Routing.BLOCK_AREA, blockAreas(regions));
            }
            boolean flexible = takRequest.getProfileType() == OTNrequest.ProfileType.BESTFLEXIBLE
                    || takRequest.getProfileType() == OTNrequest.ProfileType.STANDARD || !regions.isEmpty();
            request.putHint(Parameters.CH.DISABLE, !takRequest.isChCapable() || flexible);
            request.putHint(Parameters.Landmark.DISABLE, !takRequest.isLmCapable() || flexible);
            if (isCancelled()) return new RoutePointPackage("Route generation cancelled");
            GHResponse response = hopper.route(request);
            if (isCancelled()) return new RoutePointPackage("Route generation cancelled");
            if (response.hasErrors()) return new RoutePointPackage(response.getErrors().toString());

            List<PointMapItem> routePoints = new ArrayList<>();
            Map<String, NavigationCue> cues = new HashMap<>();
            for (RouteGeometry.Vertex vertex : RouteGeometry.from(response.getBest())) {
                GeoPoint point = new GeoPoint(vertex.latitude, vertex.longitude);
                String uid = UUID.randomUUID().toString();
                if (vertex.waypoint) {
                    Marker waypoint = Route.createWayPoint(GeoPointMetaData.wrap(point), uid);
                    routePoints.add(waypoint);
                    if (vertex.cue != null && !vertex.cue.isEmpty()) {
                        NavigationCue cue = new NavigationCue(uid, vertex.cue, vertex.cue);
                        cue.addCue(NavigationCue.TriggerMode.DISTANCE, 50);
                        cues.put(uid, cue);
                    }
                } else {
                    routePoints.add(new Route.ControlPointMapItem(point, uid));
                }
            }
            return new RoutePointPackage(routePoints, cues);
        } catch (RuntimeException error) {
            Log.e(TAG, "Offline route generation failed", error);
            return new RoutePointPackage("OTN could not generate this route: " + error.getMessage());
        } finally {
            hopper.close();
        }
    }

    private static String blockAreas(List<Shape> shapes) {
        List<String> areas = new ArrayList<>();
        for (Shape shape : shapes) {
            if (shape instanceof DrawingCircle) {
                DrawingCircle circle = (DrawingCircle) shape;
                GeoPoint center = circle.getCenterPoint();
                if (!center.isValid() || !Double.isFinite(circle.getRadius()) || circle.getRadius() <= 0)
                    throw new IllegalArgumentException("Invalid circle in route avoidance regions");
                areas.add(center.getLatitude() + "," + center.getLongitude() + "," + (long) Math.ceil(circle.getRadius()));
            } else if ("u-d-r".equals(shape.getType()) || "u-d-f".equals(shape.getType())) {
                GeoPoint[] points = shape.getPoints();
                if (points.length < 3) throw new IllegalArgumentException("Route avoidance region needs at least three vertices");
                StringBuilder area = new StringBuilder();
                for (GeoPoint point : points) {
                    if (!point.isValid()) throw new IllegalArgumentException("Invalid route avoidance region coordinate");
                    if (area.length() > 0) area.append(',');
                    area.append(point.getLatitude()).append(',').append(point.getLongitude());
                }
                areas.add(area.toString());
            } else {
                throw new IllegalArgumentException("Unsupported route avoidance region: " + shape.getType());
            }
        }
        return String.join(";", areas);
    }
}
