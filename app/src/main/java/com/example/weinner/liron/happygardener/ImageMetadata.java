package com.example.weinner.liron.happygardener;

/**
 * Created by Liron Weinner
 */

public class ImageMetadata {
    private String date_time;
    private String gps_lat;
    private String gps_lat_ref;
    private String gps_lng;
    private String gps_lng_ref;

    public ImageMetadata(String date_time, String gps_lat, String gps_lat_ref, String gps_lng, String gps_lng_ref) {
        this.date_time = date_time;
        this.gps_lat = gps_lat;
        this.gps_lat_ref = gps_lat_ref;
        this.gps_lng = gps_lng;
        this.gps_lng_ref = gps_lng_ref;
    }

    public String getDate_time() {
        return date_time;
    }

    public String getGps_lat() {
        return gps_lat;
    }

    public String getGps_lat_ref() {
        return gps_lat_ref;
    }

    public String getGps_lng() {
        return gps_lng;
    }

    public String getGps_lng_ref() {
        return gps_lng_ref;
    }
}
