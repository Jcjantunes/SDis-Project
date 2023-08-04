package pt.tecnico.sauron.silo.domain;


import com.google.protobuf.Timestamp;

public class Observation {
    private Type _type;
    private Timestamp _observationDate;
    private String _cameraName;
    private double _latitude;
    private double _longitude;


    public Observation(String cameraName, double latitude, double longitude, String type, String id, Timestamp obsDate) {

        if(type.equals("person")) {
            _type = new Person(id,type);
        }
        else if(type.equals("car")){
            _type = new Car(id,type);
        }
        _observationDate = obsDate;
        _cameraName = cameraName;
        _latitude = latitude;
        _longitude = longitude;

    }


    public Type get_type() {
        return _type;
    }

    public Timestamp get_observationDate() {
        return _observationDate;
    }

    public String get_cameraName() {
        return _cameraName;
    }

    public double get_latitude() {
        return _latitude;
    }

    public double get_longitude() {
        return _longitude;
    }
}
