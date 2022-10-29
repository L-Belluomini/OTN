package com.atakmap.android.OTN;

import com.atakmap.coremap.log.Log;

import com.atakmap.coremap.maps.coords.GeoPoint;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class PolyLoader {
    private String TAG = "otnPlyLoader";
    /**
     * The file containing polygon data.
     */
    private File polygonFile;

    /**
     * The name of the polygon as stated in the file-header.
     */
    private String myPolygonName;

    /**
     * Creates a new instance.
     *
     * @param polygonFile
     *            The file to read polygon units from.
     */
    public PolyLoader(final File polygonFile) {
        this.polygonFile = polygonFile;
    }


    /**
     * Builds an Area configured with the polygon information defined in the
     * file.
     *
     * @return A fully configured area.
     */
    public List<GeoPoint> loadPolygon() {
        BufferedReader bufferedReader = null;
        String sectionLine;
        double[] coordinates;
        List<GeoPoint> polygonPath = null;
        GeoPoint tmp;
        try {

            bufferedReader = new BufferedReader(new FileReader(polygonFile));

            polygonPath = new LinkedList<GeoPoint>();

            while (true) {

                // Read until a non-empty line is obtained.
                do {

                    sectionLine = bufferedReader.readLine();

                    // It is invalid for the file to end without a section "END" record.
                    if (sectionLine == null) {
                        break;
                    }

                    // Remove any whitespace.
                    sectionLine = sectionLine.trim();

                } while (sectionLine.length() == 0);

                // Stop reading when the section END record is reached.
                if ("END".equals(sectionLine)) {
                    break;
                }

                // Parse the line into its coordinates
                coordinates = parseCoordinates(sectionLine);
                if( coordinates == null ) {
                    continue;
                }
                tmp = new GeoPoint( coordinates[1] , coordinates[0]  );
                polygonPath.add ( tmp);
            }

        } catch (Exception e){
            Log.e("polyloader" , e.toString() );
        }
        return polygonPath ;

    }


            /**
             * Parses a coordinate line into its constituent double precision
             * coordinates.
             *
             * @param coordinateLine
             *            The raw file line.
             * @return A pair of coordinate values, first is longitude, second is
             *         latitude.
             */
            private double[] parseCoordinates(String coordinateLine) {
                String[] rawTokens;
                double[] results;
                int tokenCount;


                //Log.d(TAG,coordinateLine );
                // Split the line into its sub strings separated by whitespace.
                rawTokens = coordinateLine.split("\\s");

                // Copy the non-zero tokens into a result array.
                tokenCount = 0;
                results = new double[2];
                for (int i = 0; i < rawTokens.length; i++) {
                    if (rawTokens[i].length() > 0) {
                        if (tokenCount > 2) {
                            continue;
                        }

                        try {
                            //Log.d(TAG,"token"+rawTokens[i]);
                            Double tmpDouble = Double.parseDouble(rawTokens[i]);
                            if( tmpDouble == null){
                                return null;
                            }
                            results[tokenCount] = tmpDouble;
                            tokenCount++;
                        } catch (NumberFormatException e) {
                            Log.e(TAG,e.toString());
                        }
                    }
                }


                return results;
            }

            /**
             * This method must only be called after {@link #loadPolygon()}.
             * @return The name of the polygon as stated in the file-header.
             */
            public String getPolygonName() {
                return myPolygonName;
            }

}




/**
 * Reads the contents of a polygon file into an Area instance.
 * <p>
 * The file format is defined at http://www.maproom.psu.edu/dcw/. An example is
 * provided here. The first line contains the name of the file, the second line
 * contains the name of an individual polygon and if it is prefixed with ! it
 * means it is a negative polygon to be subtracted from the resultant extraction
 * polygon.
 * <pre>
 * australia_v
 * 1
 *      0.1446763E+03    -0.3825659E+02
 *      0.1446693E+03    -0.3826255E+02
 *      0.1446627E+03    -0.3825661E+02
 *      0.1446763E+03    -0.3824465E+02
 *      0.1446813E+03    -0.3824343E+02
 *      0.1446824E+03    -0.3824484E+02
 *      0.1446826E+03    -0.3825356E+02
 *      0.1446876E+03    -0.3825210E+02
 *      0.1446919E+03    -0.3824719E+02
 *      0.1447006E+03    -0.3824723E+02
 *      0.1447042E+03    -0.3825078E+02
 *      0.1446758E+03    -0.3826229E+02
 *      0.1446693E+03    -0.3826255E+02
 * END
 * !2
 *      0.1422483E+03    -0.3839481E+02
 *      0.1422436E+03    -0.3839315E+02
 *      0.1422496E+03    -0.3839070E+02
 *      0.1422543E+03    -0.3839025E+02
 *      0.1422574E+03    -0.3839155E+02
 *      0.1422467E+03    -0.3840065E+02
 *      0.1422433E+03    -0.3840048E+02
 *      0.1422420E+03    -0.3839857E+02
 *      0.1422436E+03    -0.3839315E+02
 * END
 * END
 * </pre>
 *
 * original
 * @author Brett Henderson
 *
 * edited
 * L.B.
 */
