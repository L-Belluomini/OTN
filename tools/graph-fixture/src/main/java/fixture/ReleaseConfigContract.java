package fixture;

import com.google.gson.Gson;
import com.graphhopper.GraphHopperConfig;

/** Runs after R8 classfile obfuscation against the original desktop JSON contract. */
public final class ReleaseConfigContract {
    public static void main(String[] args) {
        String json = "{\"map\":{\"map\":{\"graph.location\":\"qualified-graph\","
                + "\"routing.max_visited_nodes\":12345,\"graph.dataaccess\":\"MMAP\"}},"
                + "\"profiles\":[{\"name\":\"car\",\"vehicle\":\"car\",\"weighting\":\"fastest\","
                + "\"turnCosts\":true,\"hints\":{\"map\":{\"profile_test_hint\":77}}}],"
                + "\"chProfiles\":[],\"lmProfiles\":[]}";
        GraphHopperConfig config = new Gson().fromJson(json, GraphHopperConfig.class);
        if (!"qualified-graph".equals(config.getString("graph.location", ""))
                || config.getInt("routing.max_visited_nodes", -1) != 12345
                || !"MMAP".equals(config.getString("graph.dataaccess", "")))
            throw new IllegalStateException("Graph configuration properties lost after obfuscation");
        if (config.getProfiles().size() != 1 || !config.getProfiles().get(0).isTurnCosts()
                || config.getProfiles().get(0).getHints().getInt("profile_test_hint", -1) != 77)
            throw new IllegalStateException("Routing profile hints lost after obfuscation");
        System.out.println("RELEASE_GSON_CONFIG_PASS graphProperties=true profileHints=true turnCosts=true");
    }
}
