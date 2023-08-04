package pt.tecnico.sauron.silo.client;

import com.google.protobuf.Timestamp;
import com.google.protobuf.util.Timestamps;
import pt.tecnico.sauron.silo.grpc.Silo;

import java.util.Comparator;

public class CacheEntryObservationMessageCompare implements Comparator<Silo.ObservationMessage> {

    public int compare(Silo.ObservationMessage observation1, Silo.ObservationMessage observation2) {

        Timestamp timestamp1 = observation1.getObsDate();
        Timestamp timestamp2 = observation2.getObsDate();

        return Timestamps.compare(timestamp2,timestamp1);
    }

}
