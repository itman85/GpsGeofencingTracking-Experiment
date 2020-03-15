package phannguyen.sample.gpsgeofencingtrackingexperiment.models;

public class GeoFencingPlaceModel {
    protected double lat;
    protected double lng;
    protected int radius;
    protected String name;


    public GeoFencingPlaceModel(double lat, double lng, int radius, String name) {
        this.lat = lat;
        this.lng = lng;
        this.radius = radius;
        this.name = name;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
