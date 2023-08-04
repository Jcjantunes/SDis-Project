package pt.tecnico.sauron.silo.domain;
import com.google.protobuf.Timestamp;
import com.google.protobuf.util.Timestamps;

import java.util.Comparator;

public class ObservationCompare implements Comparator<Observation>{
    public int compare(Observation observation1, Observation observation2) {

        Timestamp timestamp1 = observation1.get_observationDate();
        Timestamp timestamp2 = observation2.get_observationDate();

        return Timestamps.compare(timestamp2,timestamp1);
    }
}
