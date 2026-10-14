package fixture;

import com.atakmap.android.OTN.router.RouteGeometry;
import com.google.gson.Gson;
import com.graphhopper.*;
import com.graphhopper.util.shapes.GHPoint;
import java.nio.file.*;
import java.util.*;

/** Graph load/routing regression without the desktop XML/elevation dependencies.
 * This is a JVM check, not Android or minified-APK engine qualification. */
public final class AndroidRuntimeRouting {
    public static void main(String[] args) throws Exception {
        for (String excluded : List.of("com.fasterxml.jackson.dataformat.xml.XmlMapper",
                "org.apache.xmlgraphics.image.codec.tiff.TIFFImageDecoder",
                "com.ctc.wstx.stax.WstxInputFactory")) {
            try {
                Class.forName(excluded);
                throw new IllegalStateException("Excluded dependency still available: " + excluded);
            } catch (ClassNotFoundException expected) { }
        }
        Path folder = Path.of(args[0]);
        GraphHopperConfig config;
        try (var reader = Files.newBufferedReader(folder.resolve("graph/config.json"))) {
            config = new Gson().fromJson(reader, GraphHopperConfig.class);
        }
        config.putObject("graph.dataaccess", "MMAP");
        int runs = 0;
        for (int attempt = 0; attempt < 3; attempt++) {
            GraphHopper hopper = new GraphHopper();
            try {
                hopper.init(new GraphHopperConfig(config));
                if (!hopper.load()) throw new IllegalStateException("Prebuilt graph did not load");
                List<GHPoint> points = List.of(new GHPoint(.001, .001),
                        new GHPoint(.004, .001), new GHPoint(.004, .004));
                List<GHPoint> before = new ArrayList<>(points);
                GHRequest request = TestGraph.request().setPoints(points);
                GHResponse response = hopper.route(request);
                if (response.hasErrors()) throw new IllegalStateException(response.getErrors().toString());
                var vertices = RouteGeometry.from(response.getBest());
                if (!points.equals(before) || vertices.size() < 3
                        || !vertices.get(0).waypoint || !vertices.get(vertices.size() - 1).waypoint
                        || vertices.stream().filter(v -> v.cue != null).count() < 2)
                    throw new IllegalStateException("Lost immutable via request, endpoints or cues");
                if (!hopper.route(TestGraph.request().setPoints(List.of(
                        new GHPoint(50, 50), new GHPoint(51, 51)))).hasErrors())
                    throw new IllegalStateException("Out-of-graph request unexpectedly succeeded");
                runs++;
            } finally { hopper.close(); }
        }
        System.out.println("ANDROID_DEPENDENCY_LOAD_ROUTE_PASS runs=" + runs
                + " mmap=true via=true cues=true outOfGraphRejected=true desktopXmlAbsent=true");
    }
}
