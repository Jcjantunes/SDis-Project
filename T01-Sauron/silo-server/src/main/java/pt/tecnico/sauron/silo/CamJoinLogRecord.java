package pt.tecnico.sauron.silo;

import java.util.List;

public class CamJoinLogRecord extends LogRecord{
    String _name;
    double _latitude;
    double _longitude;

    public CamJoinLogRecord(int replicaNumber, List<Integer> ts, List<Integer> prev, String name, double latitude, double longitude, int updateID) {
        super(replicaNumber,ts,prev,updateID);
        _name = name;
        _latitude = latitude;
        _longitude = longitude;
    }

    public String get_name() {
        return _name;
    }

    public double get_latitude() {
        return _latitude;
    }

    public double get_longitude() {
        return _longitude;
    }
}
