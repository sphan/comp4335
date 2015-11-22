package com.example.sphan.urbannoise;

import android.location.Location;

import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Created by amac868 on 22/11/2015.
 */
public class DataReconstructor {

    ArrayList<GridPoint> gridpoints;

    public DataReconstructor(ArrayList<Location> latLngs, ArrayList<Double> dBs) {
        if(latLngs == null || dBs == null || latLngs.size() != dBs.size()) {
            gridpoints = null;
            return;
        }
        gridpoints = new ArrayList<>();

        GridPoint m;
        for(int i = 0; i < latLngs.size(); i++) {
            m = new GridPoint(latLngs.get(i).getLatitude(),latLngs.get(i).getLongitude(),dBs.get(i));
            int index = gridpoints.indexOf(m);
            if(index != -1) {
                gridpoints.get(index).updateNoise(dBs.get(i));
            } else {
                gridpoints.add(m);
            }

        }

    }

    private class GridPoint {
        private double lat;
        private double lng;
        private CoordConverter coord;
        private int x;
        private int y;
        private double noise;
        private int freq;

        public GridPoint(double lat, double lng, double decibel) {
            this.lat = lat;
            this.lng = lng;
            coord = new CoordConverter(lat,lng);
            Pattern pattern = Pattern.compile("(\\d{4})(\\d{4})\\s*$");
            Matcher matcher = pattern.matcher(coord.getMGRS());
            matcher.find();
            x = Integer.parseInt(matcher.group());
            matcher.find();
            y = Integer.parseInt(matcher.group());
            noise = decibel;
            freq = 1;
        }

        public int getX() {
            return this.x;
        }

        public int getY() {
            return this.y;
        }

        public void updateNoise(double decibel) {
            noise = (freq*noise + decibel)/(freq+1);
            freq++;
        }

        public double getRawLat() {
            return lat;
        }

        public double getRawLng() {
            return lng;
        }

        public void setNoise(double decibel) {
            noise = decibel;
        }

        public double getNoise() {
            return noise;
        }

        public ArrayList<GridPoint> inBetweeen(GridPoint u, GridPoint v) {
            ArrayList<GridPoint> between = new ArrayList<>();
            between.add(u);
            GridPoint k;
            int ptr = 0;
            double m = (v.getRawLng()-u.getRawLng())/(v.getRawLat()-u.getRawLat());
            double theta = Math.atan(m);
            double dx = Math.cos(theta)*0.0001;
            double dy = m*dx;
            int nsteps = (int)(Math.sqrt((v.getRawLng()-u.getRawLng())*(v.getRawLng()-u.getRawLng())
                    +(v.getRawLat()-u.getRawLat())*(v.getRawLat()-u.getRawLat()))/0.0001);
            double klat = u.getRawLat(), klng = u.getRawLng();
            for(int i = 0; i < nsteps; i++) {
                klat += dx;
                klng += dy;
                k = new GridPoint(klat,klng, 0);
                if(k.toString() != between.get(ptr).toString()) {
                    k.setNoise(u.getNoise() + ((double) i / (double) nsteps) * (v.getNoise() - u.getNoise()));
                    between.add(k);
                    ptr++;
                }
                if(k.toString() == v.toString()) {
                    break;
                }
            }

            return between;
        }

        public double[] getLatLongGridpoint() {
            return coord.getGridpoint();
        }

        public int ManhattanDist(GridPoint x, GridPoint y) {
            int x1 = x.getX(),x2 = y.getX(),y1 = x.getY(),y2 = y.getY();
            return Math.abs(x2-x1)+Math.abs(y2-y1);
        }

        @Override
        public String toString() {
            return coord.getMGRS();
        }

        @Override
        public boolean equals(Object o) {
            if(o instanceof  GridPoint) {
                GridPoint t = (GridPoint) o;
                return t.toString() == this.toString();
            }
            return false;
        }

    }

}
