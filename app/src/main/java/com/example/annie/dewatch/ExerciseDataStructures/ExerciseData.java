package com.example.annie.dewatch.ExerciseDataStructures;

import android.graphics.Color;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Annie on 2018-03-08.
 * todo: Make this a cache
 * todo: make this parcelable
 */

public class ExerciseData {
    private String date; // todo: Insert date at the beginning of the exercise
    private double totalDist;
    private int totalTime; // in seconds
    private double avgSpeed;
    public PolylineOptions pathOptions;
    public Polyline path;
    private List<LatLng> pathPoints;
    private ArrayList<Double> speedsList;
    private String recordType; // for use in best record db
    private String latLngJson;
    private String speedGraphPointsJson;
    private ArrayList<SpeedPoint> speedGraphPoints;

    public static final String HAS_STATS = "hasStats";

    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd-HH-mm-ss";

    public static final String LAST_DATE = "lastDate";
    public static final String LAST_TIME = "lastTime";
    public static final String LAST_DISTANCE = "lastDistance";
    public static final String LAST_SPEED = "lastSpeed";

    public static final String SPEED_RECORD_DATE = "speedRecordDate";
    public static final String SPEED_RECORD_TIME = "speedRecordTime";
    public static final String SPEED_RECORD_DISTANCE = "speedRecordDist";
    public static final String SPEED_RECORD_SPEED = "speedRecordSpeed";

    public static final String TIME_RECORD_DATE = "timeRecordDate";
    public static final String TIME_RECORD_TIME = "timeRecordTime";
    public static final String TIME_RECORD_TIME_SEC = "timeRecordSec";
    public static final String TIME_RECORD_DISTANCE = "timeRecordDist";
    public static final String TIME_RECORD_SPEED = "timeRecordSpeed";

    public static final String DISTANCE_RECORD_DATE = "distRecordDate";
    public static final String DISTANCE_RECORD_TIME = "distRecordTime";
    public static final String DISTANCE_RECORD_DISTANCE = "distRecordDist";
    public static final String DISTANCE_RECORD_SPEED = "distRecordSpeed";


    public ExerciseData() {
        totalDist = 0;
        totalTime = -1;
        avgSpeed = 0;
        pathOptions = new PolylineOptions().
                geodesic(true).
                color(Color.rgb(0, 155, 224)).
                width(10);
        speedsList = new ArrayList<>();
        pathPoints = new ArrayList<>();
        speedGraphPoints = new ArrayList<>();
    }

    public ExerciseData(String date, int time, double distance, double speed, String coordinates, String speedGraphPoints) {
        this.date = date;
        this.totalTime = time;
        this.totalDist = distance;
        this.avgSpeed = speed;
        this.latLngJson = coordinates;
        this.speedGraphPointsJson = speedGraphPoints;
    }

    public ExerciseData(String recordType, String date, int time, double distance, double speed) {
        this.date = date;
        this.totalTime = time;
        this.totalDist = distance;
        this.avgSpeed = speed;
        this.recordType = recordType;
    }

    private double updateAverage(double stat, double average, ArrayList<Double> statList) {
        statList.add(stat);

        if(stat == 0)
            return average;
        else {
            return average + (stat - average) / statList.size();
        }
    }

    public void setTime(int time) {
        totalTime = time;
    }

    public synchronized void setDistance(double distance) {
        totalDist = distance + totalDist;
        avgSpeed = calculateSpeed(totalDist, totalTime);
        speedsList.add(avgSpeed);
    }

    /**
     *
     * @param dist in km
     * @param time in seconds
     * @return speed in km/h
     */
    private double calculateSpeed(double dist, double time) {
        if(time == 0) {
            return 0;
        } else {
            return dist / time * 3600;
        }
    }

    public double getTotalDist() {
        return totalDist;
    }

    public int getTotalTime() {
        return totalTime;
    }

    public List<LatLng> getPathPoints() {
        return pathPoints;
    }

    public void setPathPoints(List<LatLng> pathPoints) {
        this.pathPoints = pathPoints;
    }

    public ArrayList<Double> getSpeedsList() {
        return speedsList;
    }

    public double getAvgSpeed() {
        return avgSpeed;
    }

    public void addSpeedGraphPoint(int time, double speed) {
        speedGraphPoints.add(new SpeedPoint(time, speed));
    }

    public ArrayList<SpeedPoint> getSpeedGraphPoints() {
        return speedGraphPoints;
    }

    /**
     * Calculate the distance in metres between two LatLng
     *
     * @param p1 previous LatLng
     * @param p2 new LatLng
     * @return distance in km
     */
    public static double calculateDistance(LatLng p1, LatLng p2) {
        final double D2R = Math.PI / 180.0;
        final double EARTH_RADIUS = 6378.137;

        double dLong = (p2.longitude - p1.longitude) * D2R;
        double dLat = (p2.latitude - p1.latitude) * D2R;
        double a = Math.pow(Math.sin(dLat / 2), 2) + Math.cos(p1.latitude * D2R) * Math.cos(p2.latitude * D2R) * Math.pow(Math.sin(dLong / 2), 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return c * EARTH_RADIUS;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getRecordType() {
        return recordType;
    }

    public String getCoordinatesJson() {
        return latLngJson;
    }

    public String getSpeedGraphPointsJson() {
        return speedGraphPointsJson;
    }

    public LatLng getPathCentre() {
        return getPathCentre(this.pathPoints);
    }

    public static LatLng getPathCentre(List<LatLng> points) {
        if(points.isEmpty()) {
            Log.e("getPathCentre", "List<LatLng> points is empty");
            return new LatLng(0, 0);
        }

        double minLat = points.get(0).latitude;
        double minLng = points.get(0).longitude;
        double maxLat = points.get(0).latitude;
        double maxLng = points.get(0).longitude;

        for (LatLng point : points) {
            if (point.latitude < minLat)
                minLat = point.latitude;
            if (point.latitude > maxLat)
                maxLat = point.latitude;

            if (point.longitude < minLng)
                minLng = point.longitude;
            if (point.longitude > maxLng)
                maxLng = point.longitude;
        }

        return new LatLng((minLat + maxLat) / 2, (minLng + maxLng) / 2);
    }
}
