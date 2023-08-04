package pt.tecnico.sauron.silo.client;

import pt.tecnico.sauron.silo.grpc.Silo.*;

import java.util.ArrayList;
import java.util.List;

public class CacheEntry {
    private List<Integer> _timestamp = new ArrayList<>();
    private List<ObservationMessage> _observationMessages = new ArrayList<>();

    public CacheEntry(List<Integer> timestamp, ObservationMessage observationMessage) {

        _timestamp.addAll(timestamp);
        _observationMessages.add(observationMessage);

    }

    public CacheEntry(List<Integer> timestamp){
        _timestamp.addAll(timestamp);
    }


    public List<Integer> get_timestamp() {
        return _timestamp;
    }

    public void set_observationMessages(ObservationMessage observationMessage) {
        _observationMessages.add(observationMessage);
    }

    public List<ObservationMessage> get_observationMessages() {
        return _observationMessages;
    }
}
