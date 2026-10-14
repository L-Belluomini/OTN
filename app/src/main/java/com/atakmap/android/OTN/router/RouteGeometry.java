package com.atakmap.android.OTN.router;

import com.graphhopper.ResponsePath;
import com.graphhopper.util.Instruction;
import com.graphhopper.util.InstructionList;
import com.graphhopper.util.PointList;
import java.util.ArrayList;
import java.util.List;

/** Converts GraphHopper instruction intervals without coordinate-equality matching. */
public final class RouteGeometry {
    public static final class Vertex {
        public final double latitude, longitude;
        public final boolean waypoint;
        public final String cue;
        Vertex(double latitude, double longitude, boolean waypoint, String cue) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.waypoint = waypoint;
            this.cue = cue;
        }
    }

    public static List<Vertex> from(ResponsePath path) {
        PointList points = path.getPoints();
        if (points == null || points.size() < 2)
            throw new IllegalArgumentException("The router returned fewer than two route points");
        String[] cues = new String[points.size()];
        InstructionList instructions = path.getInstructions();
        if (instructions != null && !instructions.isEmpty()) {
            int index = 0;
            for (Instruction instruction : instructions) {
                if (index >= points.size() || instruction.getLength() < 0)
                    throw new IllegalArgumentException("Invalid route instruction interval");
                String cue = instruction.getTurnDescription(instructions.getTr());
                // Via and subsequent departure instructions can share the same vertex.
                cues[index] = cues[index] == null ? cue : cues[index] + ". " + cue;
                index += instruction.getLength();
            }
            if (index != points.size() - 1)
                throw new IllegalArgumentException("Route instructions do not cover the geometry");
        }
        List<Vertex> result = new ArrayList<>(points.size());
        for (int i = 0; i < points.size(); i++) {
            double lat = points.getLat(i), lon = points.getLon(i);
            if (!Double.isFinite(lat) || !Double.isFinite(lon) || Math.abs(lat) > 90 || Math.abs(lon) > 180)
                throw new IllegalArgumentException("Invalid route coordinate");
            result.add(new Vertex(lat, lon, i == 0 || i == points.size() - 1 || cues[i] != null, cues[i]));
        }
        return result;
    }
    private RouteGeometry() {}
}
