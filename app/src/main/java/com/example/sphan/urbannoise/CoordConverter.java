package com.example.sphan.urbannoise;

import java.lang.Math;

import android.location.Location;
import android.util.Log;

/**
 * Created by amac868 on 17/11/2015.
 * Adapted from https://github.com/proj4js/mgrs/blob/master/dist/mgrs.js
 */
public class CoordConverter {
    private static final String TAG = CoordConverter.class.getSimpleName();

    private static int NUM_100k_SETS = 6;
    private static String SET_ORIGIN_COLUMN_LETTERS = "AJSAJS";
    private static String SET_ORIGIN_ROW_LETTERS = "AFAFAF";

    //WGS84 ellipsoid
    private static double EQU_RADIUS = 6378137.0;
    private static double k0 = 0.9996;
    private static double eccsq = 0.00669438;
    private static double eccPrimeSq = (eccsq) / (1 - eccsq);

    private static int _A = 65; // A
    private static int I = 73; // I
    private static int O = 79; // O
    private static int V = 86; // V
    private static int Z = 90; // Z

    private final double lat;  //Latitude
    private final double latRad;
    private final double lng;  //Longitude
    private final double longRad;

    private final double longOrigin;
    private final double longOriginRad;

    private final double easting;
    private final double northing;
    private int zone;
    private final char zoneLetter;

    private final double[] gridpoint;

    /*
     * From a given (lat,long) generate the zone, zoneLetter, easting and northing (UTM coord)
     * Note that MGRS is based upon UTM so it is simple to convert UTM to MGRS
     * The gridpoint is the (lat,long) -> MGRS -> (lat',long')
     */
    public CoordConverter(double latitude, double longitude) {

        lng = longitude;
        lat = latitude;
        latRad = Math.toRadians(lat);
        longRad = Math.toRadians(lng);
        zone = (int) Math.floor((lng+180)/6)+1;
        //Make sure the longitude 180.00 is in Zone 60
        if (lng == 180) {
            zone = 60;
        }
        // Special zone for Norway
        if (lat >= 56.0 && lat < 64.0 && lng >= 3.0 && lng < 12.0) {
            zone = 32;
        }
        // Special zones for Svalbard
        if (lat >= 72.0 && lat < 84.0) {
            if (lng >= 0.0 && lng < 9.0) {
                zone = 31;
            }
            else if (lng >= 9.0 && lng < 21.0) {
                zone = 33;
            }
            else if (lng >= 21.0 && lng < 33.0) {
                zone = 35;
            }
            else if (lng >= 33.0 && lng < 42.0) {
                zone = 37;
            }
        }

        longOrigin = (zone - 1) * 6 - 180 + 3; //+3 puts origin in middle of zone
        longOriginRad = Math.toRadians(longOrigin);
        double N = EQU_RADIUS / Math.sqrt(1 - eccsq * Math.sin(latRad) * Math.sin(latRad));
        double T = Math.tan(latRad) * Math.tan(latRad);
        double C = eccPrimeSq * Math.cos(latRad) * Math.cos(latRad);
        double A = Math.cos(latRad) * (longRad - longOriginRad);

        double M = EQU_RADIUS * ((1 - eccsq / 4 - 3 * eccsq * eccsq / 64 - 5 * eccsq * eccsq * eccsq / 256) *
                latRad - (3 * eccsq / 8 + 3 * eccsq * eccsq / 32 + 45 * eccsq * eccsq * eccsq / 1024) *
                Math.sin(2 * latRad) + (15 * eccsq * eccsq / 256 + 45 * eccsq * eccsq * eccsq / 1024) *
                Math.sin(4 * latRad) - (35 * eccsq * eccsq * eccsq / 3072) * Math.sin(6 * latRad));

        double UTMEasting = (k0 * N * (A + (1 - T + C) *A*A*A / 6.0 +
                (5 - 18 * T + T * T + 72 * C - 58 * eccPrimeSq)*A*A*A*A*A / 120.0) + 500000.0);

        double UTMNorthing = (k0 * (M + N * Math.tan(latRad) * (A * A / 2 + (5 - T + 9 * C + 4 * C * C) *
                A * A * A * A / 24.0 + (61 - 58 * T + T * T + 600 * C - 330 * eccPrimeSq) *
                A * A * A * A * A * A / 720.0)));

        if (lat < 0.0) {
            UTMNorthing += 10000000.0; //10000000 meter offset for southern hemisphere
        }
        northing = Math.round(UTMNorthing);
        easting =  Math.round(UTMEasting);
        zoneLetter = letterDesignation();
        gridpoint = MGRStoLL();
    }

    private char letterDesignation() {
        char designator = ' ';
        if ((84 >= lat) && (lat >= 72)) {
            designator = 'X';
        }
        else if ((72 > lat) && (lat >= 64)) {
            designator = 'W';
        }
        else if ((64 > lat) && (lat >= 56)) {
            designator = 'V';
        }
        else if ((56 > lat) && (lat >= 48)) {
            designator = 'U';
        }
        else if ((48 > lat) && (lat >= 40)) {
            designator = 'T';
        }
        else if ((40 > lat) && (lat >= 32)) {
            designator = 'S';
        }
        else if ((32 > lat) && (lat >= 24)) {
            designator = 'R';
        }
        else if ((24 > lat) && (lat >= 16)) {
            designator = 'Q';
        }
        else if ((16 > lat) && (lat >= 8)) {
            designator = 'P';
        }
        else if ((8 > lat) && (lat >= 0)) {
            designator = 'N';
        }
        else if ((0 > lat) && (lat >= -8)) {
            designator = 'M';
        }
        else if ((-8 > lat) && (lat >= -16)) {
            designator = 'L';
        }
        else if ((-16 > lat) && (lat >= -24)) {
            designator = 'K';
        }
        else if ((-24 > lat) && (lat >= -32)) {
            designator = 'J';
        }
        else if ((-32 > lat) && (lat >= -40)) {
            designator = 'H';
        }
        else if ((-40 > lat) && (lat >= -48)) {
            designator = 'G';
        }
        else if ((-48 > lat) && (lat >= -56)) {
            designator = 'F';
        }
        else if ((-56 > lat) && (lat >= -64)) {
            designator = 'E';
        }
        else if ((-64 > lat) && (lat >= -72)) {
            designator = 'D';
        }
        else if ((-72 > lat) && (lat >= -80)) {
            designator = 'C';
        }
        return designator;
    }

    private String get100kID() {
        int param = zone % NUM_100k_SETS == 0 ? NUM_100k_SETS : zone % NUM_100k_SETS;
        int col = (int)Math.floor(easting/100000);
        int row = (int)Math.floor(northing/100000) % 20;
        int A = 65;
        int I = 73;
        int O = 79;
        int V = 86;
        int Z = 90;
        int index = param - 1;
        char colOrigin = SET_ORIGIN_COLUMN_LETTERS.charAt(index);
        char rowOrigin = SET_ORIGIN_ROW_LETTERS.charAt(index);

        // colInt and rowInt are the letters to build to return
        int colInt = colOrigin + col - 1;
        int rowInt = rowOrigin + row;
        boolean rollover = false;
        if (colInt > Z) {
            colInt = colInt - Z + A - 1;
            rollover = true;
        }
        if (colInt == I || (colOrigin < I && colInt > I) || ((colInt > I || colOrigin < I) && rollover)) {
            colInt++;
        }
        if (colInt == O || (colOrigin < O && colInt > O) || ((colInt > O || colOrigin < O) && rollover)) {
            colInt++;
            if (colInt == I) {
                colInt++;
            }
        }
        if (colInt > Z) {
            colInt = colInt - Z + A - 1;
        }
        if (rowInt > V) {
            rowInt = rowInt - V + A - 1;
            rollover = true;
        } else {
            rollover = false;
        }
        if (((rowInt == I) || ((rowOrigin < I) && (rowInt > I))) || (((rowInt > I) || (rowOrigin < I)) && rollover)) {
            rowInt++;
        }
        if (((rowInt == O) || ((rowOrigin < O) && (rowInt > O))) || (((rowInt > O) || (rowOrigin < O)) && rollover)) {
            rowInt++;

            if (rowInt == I) {
                rowInt++;
            }
        }
        if (rowInt > V) {
            rowInt = rowInt - V + A - 1;
        }
        return String.valueOf((char)colInt) + String.valueOf((char)rowInt);
    }


    private double[] MGRStoLL() {
        String mgrsString = this.getMGRS();
        double[] ll = {-1.0,-1.0};
        int length = mgrsString.length();
        char[] mgrs = mgrsString.toCharArray();
        int zoneNum;
        char zLetter;
        int i = 0;
        while(mgrs[i] < 'A' || mgrs[i] > 'Z') {
            i++;
        }
        if(i > 2) {
            Log.i(TAG,"MGRS conversion error from: "+mgrsString+", "+mgrsString.substring(0,i));
            return null;
        }
        zoneNum = Integer.parseInt(mgrsString.substring(0,i),10);
        zLetter = mgrs[i++];
        if (zoneLetter <= 'A' || zoneLetter == 'B' || zoneLetter == 'Y' || zoneLetter >= 'Z' || zoneLetter == 'I' || zoneLetter == 'O') {
            Log.i(TAG,"MGRSPoint zone letter " + zoneLetter + " not handled: " + mgrsString);
            return null;
        }
        String hunK = mgrsString.substring(i,i+2);
        i+=2;
        int set = zoneNum % NUM_100k_SETS == 0 ? NUM_100k_SETS : zoneNum % NUM_100k_SETS;
        double east100k = getEastingFromChar(hunK.charAt(0),set);
        double north100k = getNorthingFromChar(hunK.charAt(1),set);
        double minNorthing = getMinNorthing(zLetter);
        if(east100k == -1 || north100k == -1 || minNorthing == -1) {
            Log.i(TAG,"MGRStoLL failed: " + mgrsString);
            return null;
        }
        while(north100k < minNorthing) {
            north100k += 2000000;
        }
        int rem = length -i;
        if(rem % 2 != 0) {
            Log.i(TAG, "MGRS has to have same number of digits for easting and northing respectively");
            return null;
        }
        int sep = rem/2;
        double sepEasting = 0.0;
        double sepNorthing = 0.0;
        double accuracyBonus, UTMeasting, UTMnorthing;
        if(sep > 0) {
            accuracyBonus = 100000.0 / Math.pow(10, sep);
            String sepEastingString = mgrsString.substring(i, i + sep);
            sepEasting = Double.parseDouble(sepEastingString) * accuracyBonus;
            String sepNorthingString = mgrsString.substring(i + sep);
            sepNorthing = Double.parseDouble(sepNorthingString) * accuracyBonus;
        }
        UTMeasting = sepEasting + east100k;
        UTMnorthing = sepNorthing + north100k;

        double e1 = (1 - Math.sqrt(1 - eccsq)) / (1 + Math.sqrt(1 - eccsq));
        double N1, T1, C1, R1, D, M;
        double mu, phi1Rad;

        // remove 500,000 meter offset for longitude
        double x = UTMeasting - 500000.0;
        double y = UTMnorthing;

        // We must know somehow if we are in the Northern or Southern
        // hemisphere, this is the only time we use the letter So even
        // if the Zone letter isn't exactly correct it should indicate
        // the hemisphere correctly
        if ((int)zLetter < (int)'N') {
            y -= 10000000.0; // remove 10,000,000 meter offset used for southern hemisphere
        }

        double lngOrigin = (zoneNum - 1) * 6 - 180 + 3;
        M = y / k0;
        mu = M / (EQU_RADIUS * (1 - eccsq / 4 - 3 * eccsq * eccsq / 64 - 5 * eccsq * eccsq * eccsq / 256));

        phi1Rad = mu + (3 * e1 / 2 - 27 * e1 * e1 * e1 / 32) * Math.sin(2 * mu) + (21 * e1 * e1 / 16 - 55 * e1 * e1 * e1 * e1 / 32) * Math.sin(4 * mu) + (151 * e1 * e1 * e1 / 96) * Math.sin(6 * mu);

        N1 = EQU_RADIUS / Math.sqrt(1 - eccsq * Math.sin(phi1Rad) * Math.sin(phi1Rad));
        T1 = Math.tan(phi1Rad) * Math.tan(phi1Rad);
        C1 = eccPrimeSq * Math.cos(phi1Rad) * Math.cos(phi1Rad);
        R1 = EQU_RADIUS * (1 - eccsq) / Math.pow(1 - eccsq * Math.sin(phi1Rad) * Math.sin(phi1Rad), 1.5);
        D = x / (N1 * k0);

        double latitude = phi1Rad - (N1 * Math.tan(phi1Rad) / R1) * (D * D / 2 - (5 + 3 * T1 + 10 * C1 - 4 * C1 * C1 - 9 * eccPrimeSq) * D * D * D * D / 24 + (61 + 90 * T1 + 298 * C1 + 45 * T1 * T1 - 252 * eccPrimeSq - 3 * C1 * C1) * D * D * D * D * D * D / 720);
        latitude = Math.toDegrees(latitude);

        double longitude = (D - (1 + 2 * T1 + C1) * D * D * D / 6 + (5 - 2 * C1 + 28 * T1 - 3 * C1 * C1 + 8 * eccPrimeSq + 24 * T1 * T1) * D * D * D * D * D / 120) / Math.cos(phi1Rad);
        longitude = lngOrigin + Math.toDegrees(longitude);
        ll[0] = latitude;
        ll[1] = longitude;
        return ll;
    }

    public double[] getGridpoint() {
        return this.gridpoint;
    }
    public double getLatitude() {
        return this.lat;
    }
    public double getLongitude() {
        return this.lng;
    }
    public String getMGRS() {
        StringBuilder str = new StringBuilder();
        str.append(zone);
        str.append(zoneLetter);
        //Get the two letter 100k designator
        str.append(get100kID());
        //Accuracy to 10m
        str.append(String.valueOf(easting).substring(1,5));
        str.append(String.valueOf(northing).substring(2,6));
        return str.toString();
    }
    public String getUTM() {
        StringBuilder str = new StringBuilder();
        str.append(zone);
        str.append(zoneLetter);
        String UTMeasting = " " + String.valueOf(easting) + "mE";
        String UTMnorthing = " " + String.valueOf(northing) + "mN";
        str.append(UTMeasting);
        str.append(UTMnorthing);
        return str.toString();
    }

    private double getEastingFromChar(char e, int set) {
        // colOrigin is the letter at the origin of the set for the column
        int curCol = SET_ORIGIN_COLUMN_LETTERS.charAt(set - 1);
        double eastingValue = 100000.0;
        boolean rewindMarker = false;

        while (curCol != e) {
            curCol++;
            if (curCol == I) {
                curCol++;
            }
            if (curCol == O) {
                curCol++;
            }
            if (curCol > Z) {
                if (rewindMarker) {
                    Log.i(TAG, "Bad character: " + e);
                    return -1;
                }
                curCol = _A;
                rewindMarker = true;
            }
            eastingValue += 100000.0;
        }

        return eastingValue;
    }

    public double getNorthingFromChar(char n, int set) {

        if (n > 'V') {
            Log.i(TAG, "MGRSPoint given invalid Northing " + n);
            return -1;
        }

        // rowOrigin is the letter at the origin of the set for the column
        int curRow = SET_ORIGIN_ROW_LETTERS.charAt(set - 1);
        double northingValue = 0.0;
        boolean rewindMarker = false;

        while (curRow != n) {
            curRow++;
            if (curRow == I) {
                curRow++;
            }
            if (curRow == O) {
                curRow++;
            }
            // fixing a bug making whole application hang in this loop
            // when 'n' is a wrong character
            if (curRow > V) {
                if (rewindMarker) { // making sure that this loop ends
                    Log.i(TAG, "Bad character: " + n);
                    return -1;
                }
                curRow = _A;
                rewindMarker = true;
            }
            northingValue += 100000.0;
        }

        return northingValue;
    }

    private double getMinNorthing(char zLetter) {
        double northing;
        switch (zLetter) {
            case 'C':
                northing = 1100000.0;
                break;
            case 'D':
                northing = 2000000.0;
                break;
            case 'E':
                northing = 2800000.0;
                break;
            case 'F':
                northing = 3700000.0;
                break;
            case 'G':
                northing = 4600000.0;
                break;
            case 'H':
                northing = 5500000.0;
                break;
            case 'J':
                northing = 6400000.0;
                break;
            case 'K':
                northing = 7300000.0;
                break;
            case 'L':
                northing = 8200000.0;
                break;
            case 'M':
                northing = 9100000.0;
                break;
            case 'N':
                northing = 0.0;
                break;
            case 'P':
                northing = 800000.0;
                break;
            case 'Q':
                northing = 1700000.0;
                break;
            case 'R':
                northing = 2600000.0;
                break;
            case 'S':
                northing = 3500000.0;
                break;
            case 'T':
                northing = 4400000.0;
                break;
            case 'U':
                northing = 5300000.0;
                break;
            case 'V':
                northing = 6200000.0;
                break;
            case 'W':
                northing = 7000000.0;
                break;
            case 'X':
                northing = 7900000.0;
                break;
            default:
                northing = -1.0;
        }
        if (northing >= 0.0) {
            return northing;
        }
        else {
            Log.i(TAG,"Invalid zone letter: " + zLetter);
            return -1;
        }

    }
}
