package pt.tecnico.sauron.silo.domain;

public class Coordinates {
    private double _latitude;
    private double _longitude;

    public Coordinates(double latitude, double longitude){
        _latitude = latitude;
        _longitude = longitude;
    }

    public double get_latitude() {
        return _latitude;
    }

    public double get_longitude() {
        return _longitude;
    }
}
