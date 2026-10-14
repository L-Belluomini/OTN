package fixture;

import com.atakmap.android.OTN.router.RouteGeometry;
import com.graphhopper.*;
import com.graphhopper.util.*;
import com.graphhopper.util.shapes.GHPoint;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import java.util.*;
import static org.junit.Assert.*;

public class RoutingTest {
    @Rule public TemporaryFolder folder = new TemporaryFolder();
    private GraphHopper hopper;
    @Before public void create() throws Exception { hopper = TestGraph.create(folder.getRoot().toPath()); }
    @After public void close() { if (hopper != null) hopper.close(); }
    private ResponsePath route(GHRequest request) {
        GHResponse response = hopper.route(request);
        assertFalse(response.getErrors().toString(), response.hasErrors());
        return response.getBest();
    }
    @Test public void geometryIncludesEveryPointAndCueWithCorrectEndpoints() {
        ResponsePath path = route(TestGraph.request());
        List<RouteGeometry.Vertex> vertices = RouteGeometry.from(path);
        assertEquals(path.getPoints().size(), vertices.size());
        assertEquals(.001, vertices.get(0).latitude, .00001);
        assertEquals(.004, vertices.get(vertices.size()-1).longitude, .00001);
        assertTrue(vertices.get(0).waypoint);
        assertTrue(vertices.get(vertices.size()-1).waypoint);
        assertTrue(vertices.stream().anyMatch(v -> v.cue != null && v.cue.toLowerCase(Locale.ROOT).contains("turn")));
        assertEquals(path.getInstructions().size(), vertices.stream().filter(v -> v.cue != null).count());
    }
    @Test public void viaInstructionsShareVertexWithoutLosingFollowingCue() {
        GHRequest request = TestGraph.request().setPoints(List.of(new GHPoint(.001,.001), new GHPoint(.004,.001), new GHPoint(.004,.004)));
        ResponsePath path = route(request);
        List<RouteGeometry.Vertex> vertices = RouteGeometry.from(path);
        assertEquals(path.getPoints().size(), vertices.size());
        assertTrue(vertices.stream().anyMatch(v -> v.cue != null && v.cue.contains("waypoint")));
        assertEquals(path.getInstructions().size()-1, vertices.stream().filter(v -> v.cue != null).count());
    }
    @Test public void regionAvoidanceChangesRoute() {
        ResponsePath base = route(TestGraph.request());
        // Block the street immediately east of the origin, which is on the baseline route.
        double lat = base.getPoints().getLat(1), lon = base.getPoints().getLon(1);
        ResponsePath avoided = route(TestGraph.request().putHint(Parameters.Routing.BLOCK_AREA, lat + "," + lon + ",20"));
        assertNotEquals(base.getPoints().toString(), avoided.getPoints().toString());
        assertFalse(RouteGeometry.from(avoided).isEmpty());
    }
    @Test public void rejectsBrokenInstructionIntervals() {
        ResponsePath path = route(TestGraph.request());
        path.getInstructions().remove(0);
        try { RouteGeometry.from(path); fail("Expected invalid instruction coverage"); }
        catch (IllegalArgumentException expected) { assertTrue(expected.getMessage().contains("cover")); }
    }
    @Test public void unreachableCoordinatesReturnError() {
        assertTrue(hopper.route(TestGraph.request().setPoints(List.of(new GHPoint(50,50), new GHPoint(51,51)))).hasErrors());
    }
}
