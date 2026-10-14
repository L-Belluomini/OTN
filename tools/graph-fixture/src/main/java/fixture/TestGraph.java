package fixture;

import com.google.gson.GsonBuilder;
import com.graphhopper.*;
import com.graphhopper.config.Profile;
import com.graphhopper.util.Parameters;
import com.atakmap.android.OTN.router.RouteGeometry;
import java.nio.file.*;
import java.util.*;

/** Fictional 6x6 grid near 0,0. Never use this graph for real navigation. */
public final class TestGraph {
    public static GraphHopperConfig config(Path folder) {
        return new GraphHopperConfig().setProfiles(List.of(new Profile("test_car").setVehicle("car").setWeighting("fastest")))
                .putObject("graph.location", folder.resolve("graph").toString())
                .putObject("datareader.file", folder.resolve("test-grid.osm").toString())
                .putObject("prepare.min_network_size", 0)
                .putObject("graph.dataaccess", "RAM_STORE");
    }
    public static GraphHopper create(Path folder) throws Exception {
        Files.createDirectories(folder);
        StringBuilder osm = new StringBuilder("<?xml version='1.0'?><osm version='0.6' generator='OTN synthetic test'>");
        for (int y = 0; y < 6; y++) for (int x = 0; x < 6; x++)
            osm.append("<node id='").append(1 + y * 6 + x).append("' lat='").append(y * .001)
                    .append("' lon='").append(x * .001).append("'/>");
        for (int axis = 0; axis < 2; axis++) for (int row = 0; row < 6; row++) {
            osm.append("<way id='").append(100 + axis * 6 + row).append("'>");
            for (int col = 0; col < 6; col++) osm.append("<nd ref='")
                    .append(1 + (axis == 0 ? row * 6 + col : col * 6 + row)).append("'/>");
            osm.append("<tag k='highway' v='residential'/><tag k='name' v='Test ")
                    .append(axis == 0 ? "Row " : "Column ").append(row).append("'/></way>");
        }
        osm.append("</osm>");
        Files.writeString(folder.resolve("test-grid.osm"), osm);
        GraphHopperConfig config = config(folder);
        GraphHopper hopper = new GraphHopper().init(config).importOrLoad();
        // OTN expects Gson's GraphHopperConfig representation, including PMap and profile lists.
        Files.writeString(folder.resolve("graph/config.json"), new GsonBuilder().setPrettyPrinting().create().toJson(config));
        Files.writeString(folder.resolve("graph/TEST_ONLY.txt"), "Fictional roads at 0,0. Not for real navigation.\n");
        return hopper;
    }
    public static GHRequest request() {
        return new GHRequest(.001, .001, .004, .004).setProfile("test_car")
                .putHint(Parameters.CH.DISABLE, true).putHint(Parameters.Landmark.DISABLE, true);
    }
    public static void main(String[] args) throws Exception {
        Path folder = Path.of(args.length == 0 ? "build/fixture" : args[0]).toAbsolutePath();
        GraphHopper hopper = create(folder);
        try {
            GHResponse response = hopper.route(request());
            if (response.hasErrors()) throw new IllegalStateException(response.getErrors().toString());
            var vertices = RouteGeometry.from(response.getBest());
            System.out.println("TEST_ROUTE points=" + vertices.size() + " waypoints=" + vertices.stream().filter(v -> v.waypoint).count());
            for (var vertex : vertices) if (vertex.cue != null) System.out.println("CUE " + vertex.cue);
        } finally { hopper.close(); }
    }
}
