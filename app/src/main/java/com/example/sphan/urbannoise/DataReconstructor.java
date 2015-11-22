package com.example.sphan.urbannoise;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Created by amac868 on 22/11/2015.
 */
public class DataReconstructor {

    private ArrayList<GridPoint> gridpoints;
    private ArrayList<Edge> edges;
    private ArrayList<Edge> mst;
    private ArrayList<GridPoint> recon;

    private ArrayList<LatLng> locationData;
    private ArrayList<Double> noiseData;

    public DataReconstructor(ArrayList<LatLng> latLngs, ArrayList<Double> dBs) {
        if(latLngs == null || dBs == null || latLngs.size() != dBs.size()) {
            gridpoints = null;
            return;
        }
        gridpoints = new ArrayList<>();
        recon = new ArrayList<>();
        mst = new ArrayList<>();
        GridPoint m;
        Edge e;
        for(int i = 0; i < latLngs.size(); i++) {
            m = new GridPoint(latLngs.get(i).latitude,latLngs.get(i).longitude,dBs.get(i));
            int index = gridpoints.indexOf(m);
            if(index != -1) {
                gridpoints.get(index).updateNoise(dBs.get(i));
            } else {
                gridpoints.add(m);
                for(int j = 0; j < gridpoints.size(); j++) {
                    e = new Edge(gridpoints.get(j),m);
                    if(e.getCost() < 200 && e.getCost() != 0) {
                        edges.add(e);
                    }
                }
            }
        }
        calcMST();
        for(Edge edge : mst) {
            ArrayList<GridPoint> toAdd = edge.inBetween();
            if(!recon.contains(edge.u)) {
                recon.add(edge.u);
            }
            if(!recon.contains(edge.v)) {
                recon.add(edge.v);
            }
            for(int i = 0; i < toAdd.size(); i++) {
                if(!recon.contains(toAdd.get(i))) {
                    recon.add(toAdd.get(i));
                }
            }
        }
        LatLng nLocation;
        Double nNoise;
        for(GridPoint p : gridpoints) {
            nLocation = new LatLng(p.getRawLat(),p.getRawLng());
            nNoise = new Double(p.getNoise());
            locationData.add(nLocation);
            noiseData.add(nNoise);
        }
    }

    public ArrayList<LatLng> getLocationData() {
        return this.locationData;
    }

    public ArrayList<Double> getNoiseData() {
        return this.noiseData;
    }

    private ArrayList<Edge> calcMST() {
        return null;
    }

    private class Edge {
        private GridPoint u, v;
        private double cost;

        public Edge(GridPoint u, GridPoint v) {
            this.u = u;
            this.v = v;
            cost = u.dist(v);
        }

        public double getCost() {
            return cost;
        }

        public ArrayList<GridPoint> inBetween() {
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
    }

    private class GridPoint {
        private double lat;
        private double lng;
        private CoordConverter coord;
        private double noise;
        private int freq;

        public GridPoint(double lat, double lng, double decibel) {
            this.lat = lat;
            this.lng = lng;
            coord = new CoordConverter(lat,lng);
            noise = decibel;
            freq = 1;
        }

//        public int getX() {
//            return this.x;
//        }
//
//        public int getY() {
//            return this.y;
//        }

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

//        public ArrayList<GridPoint> inBetweeen(GridPoint v) {
//            ArrayList<GridPoint> between = new ArrayList<>();
//            between.add(this);
//            GridPoint k;
//            int ptr = 0;
//            double m = (v.getRawLng()-this.getRawLng())/(v.getRawLat()-this.getRawLat());
//            double theta = Math.atan(m);
//            double dx = Math.cos(theta)*0.0001;
//            double dy = m*dx;
//            int nsteps = (int)(Math.sqrt((v.getRawLng()-this.getRawLng())*(v.getRawLng()-this.getRawLng())
//                    +(v.getRawLat()-this.getRawLat())*(v.getRawLat()-u.getRawLat()))/0.0001);
//            double klat = this.getRawLat(), klng = this.getRawLng();
//            for(int i = 0; i < nsteps; i++) {
//                klat += dx;
//                klng += dy;
//                k = new GridPoint(klat,klng, 0);
//                if(k.toString() != between.get(ptr).toString()) {
//                    k.setNoise(this.getNoise() + ((double) i / (double) nsteps) * (v.getNoise() - this.getNoise()));
//                    between.add(k);
//                    ptr++;
//                }
//                if(k.toString() == v.toString()) {
//                    break;
//                }
//            }
//
//            return between;
//        }

        public double[] getLatLongGridpoint() {
            return coord.getGridpoint();
        }

        /*
         * from http://www.movable-type.co.uk/scripts/latlong.html
         */
        public double dist(GridPoint y) {
            double R = 6371000.0; // metres
            double lat1 = Math.toRadians(this.getRawLat());
            double lat2 = Math.toRadians(y.getRawLat());
            double dlat = (lat2-lat1);
            double dlong = Math.toRadians(y.getRawLng()-this.getRawLng());
            double a = Math.sin(dlat/2) * Math.sin(dlat/2) +
                    Math.cos(lat1) * Math.cos(lat2) *
                            Math.sin(dlong/2) * Math.sin(dlong/2);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
            return R * c;
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
