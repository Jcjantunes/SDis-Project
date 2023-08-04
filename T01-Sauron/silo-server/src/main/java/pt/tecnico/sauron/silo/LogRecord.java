package pt.tecnico.sauron.silo;

import java.util.ArrayList;
import java.util.List;


public abstract class LogRecord {
    private int _replicaNumber;
    private int _updateID;
    private List<Integer> _ts = new ArrayList<>();
    private List<Integer> _prev = new ArrayList<>();


    public LogRecord(int replicaNumber, List<Integer> ts, List<Integer> prev, int updateID){
        _replicaNumber = replicaNumber;
        _updateID = updateID;
        _ts.addAll(ts);
        _prev.addAll(prev);
    }

    public int get_replicaNumber() {
        return _replicaNumber;
    }

    public List<Integer> get_ts() {
        return _ts;
    }

    public List<Integer> get_prev() {
        return _prev;
    }

    public int get_updateID() {
        return _updateID;
    }
}
