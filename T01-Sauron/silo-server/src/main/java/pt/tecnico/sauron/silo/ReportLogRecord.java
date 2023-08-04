package pt.tecnico.sauron.silo;

import pt.tecnico.sauron.silo.grpc.Silo.*;

import java.util.ArrayList;
import java.util.List;

public class ReportLogRecord extends LogRecord {

    private List<ObservationMessage> _observationMessageList =  new ArrayList<>();

    public ReportLogRecord(int replicaNumber, List<Integer> ts, List<Integer> prev, List<ObservationMessage> observationMessageList, int updateID) {
        super(replicaNumber,ts,prev,updateID);
        _observationMessageList.addAll(observationMessageList);
    }

    public List<ObservationMessage> get_observationMessageList() {
        return _observationMessageList;
    }
}
