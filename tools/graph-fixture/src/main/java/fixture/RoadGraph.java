package fixture;

import com.google.gson.GsonBuilder;
import com.graphhopper.*;
import com.graphhopper.config.Profile;
import com.graphhopper.util.Parameters;
import com.atakmap.android.OTN.router.RouteGeometry;
import java.nio.file.*;
import java.util.*;

/** Reproducible, separate real-road qualification graph. Does not modify the synthetic fixture. */
public final class RoadGraph {
    public static void main(String[] args) throws Exception {
        if (args.length != 3) throw new IllegalArgumentException("Arguments: OSM XML, source metadata JSON, graph output directory");
        Path osm = Path.of(args[0]).toAbsolutePath(), source = Path.of(args[1]).toAbsolutePath(), output = Path.of(args[2]).toAbsolutePath();
        if (!Files.isRegularFile(osm) || !Files.isRegularFile(source)) throw new IllegalArgumentException("Source data missing");
        GraphHopperConfig config = new GraphHopperConfig()
                .setProfiles(List.of(new Profile("car").setVehicle("car").setWeighting("fastest").setTurnCosts(true)))
                .putObject("graph.location", output.toString()).putObject("datareader.file", osm.toString())
                .putObject("graph.dataaccess", "RAM_STORE");
        GraphHopper hopper = new GraphHopper();
        var gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            hopper.init(config).importOrLoad();
            Files.writeString(output.resolve("config.json"), gson.toJson(config));
            Files.copy(source, output.resolve("source.json"), StandardCopyOption.REPLACE_EXISTING);
            Files.writeString(output.resolve("ATTRIBUTION.txt"), "© OpenStreetMap contributors. ODbL 1.0.\nhttps://www.openstreetmap.org/copyright\nDevelopment qualification extract; incomplete outside the selected city boundary.\n");
            // Public coordinates inside the downloaded city; no device locations leave the phone.
            GHRequest request = new GHRequest(38.83, -104.82, 38.85, -104.81).setProfile("car")
                    .putHint(Parameters.CH.DISABLE, true).putHint(Parameters.Landmark.DISABLE, true);
            GHResponse response = hopper.route(request);
            if (response.hasErrors()) throw new IllegalStateException(response.getErrors().toString());
            ResponsePath path = response.getBest();
            var vertices = RouteGeometry.from(path);
            Map<String, Object> report = new LinkedHashMap<>();
            report.put("graphHopperVersion", "6.0"); report.put("turnCosts", true);
            report.put("nodes", hopper.getBaseGraph().getNodes()); report.put("edges", hopper.getBaseGraph().getEdges());
            report.put("distanceMeters", path.getDistance()); report.put("timeMillis", path.getTime());
            report.put("points", vertices.size()); report.put("cues", vertices.stream().filter(v -> v.cue != null).count());
            report.put("instructions", path.getInstructions().stream().map(i -> i.getTurnDescription(hopper.getTranslationMap().getWithFallBack(Locale.US))).toList());
            report.put("scope", "Desktop real-road import and route geometry check; not device or car qualification");
            Files.writeString(output.resolve("desktop-validation.json"), gson.toJson(report));
            System.out.println(gson.toJson(report));
        } finally { hopper.close(); }
    }
}
