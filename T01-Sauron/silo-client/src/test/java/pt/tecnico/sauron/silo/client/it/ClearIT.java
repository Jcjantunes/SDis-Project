package pt.tecnico.sauron.silo.client.it;

import org.junit.jupiter.api.Test;
import pt.tecnico.sauron.silo.client.BaseIT;
import pt.tecnico.sauron.silo.client.SiloFrontend;
import pt.tecnico.sauron.silo.grpc.Silo.*;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ClearIT extends BaseIT {

    @Test
    public void resetServerState() throws ZKNamingException {

        SiloFrontend frontend = new SiloFrontend("localhost","2181");

        InitRequest initRequest = InitRequest.newBuilder().setNumberOfReplicas(1).setInitializeReplicaTimestamp(1).build();
        InitResponse initResponse = frontend.ctrlInit(initRequest);

        CamJoinRequest camJoinRequest1 = CamJoinRequest.newBuilder().setName("Tagus").setLatitude(38.737613).setLongitude(-9.303164).build();
        CamJoinResponse camJoinResponse1 = frontend.camJoin(camJoinRequest1);

        CamJoinRequest camJoinRequest2 = CamJoinRequest.newBuilder().setName("Alameda").setLatitude(38.737613).setLongitude(-9.303164).build();
        CamJoinResponse camJoinResponse2 = frontend.camJoin(camJoinRequest2);

        ReportRequest.Builder reportRequestBuilder = ReportRequest.newBuilder();

        ObservationMessage obs1 = ObservationMessage.newBuilder().setCameraName("Tagus").setTipo("car").setIdentifier("20SD20").build();
        reportRequestBuilder.addObservationMessageList(obs1);

        ObservationMessage obs2 = ObservationMessage.newBuilder().setCameraName("Alameda").setTipo("car").setIdentifier("20SD20").build();
        reportRequestBuilder.addObservationMessageList(obs2);

        ReportRequest reportRequest = reportRequestBuilder.build();
        ReportResponse reportResponse = frontend.report(reportRequest);

        TraceRequest traceRequest1 = TraceRequest.newBuilder().setType("car").setId("20SD20").build();
        TraceResponse traceResponse1 = frontend.trace(traceRequest1);

        ObservationMessage observation1 = traceResponse1.getObservationMessageListList().get(0);
        ObservationMessage observation2 = traceResponse1.getObservationMessageListList().get(1);
        int size1 = traceResponse1.getObservationMessageListList().size();

        assertEquals(2,size1);
        assertEquals("Tagus", observation1.getCameraName());
        assertEquals("car", observation1.getTipo());
        assertEquals("20SD20", observation1.getIdentifier());

        assertEquals("Alameda", observation2.getCameraName());
        assertEquals("car", observation2.getTipo());
        assertEquals("20SD20", observation2.getIdentifier());

        ClearRequest clearRequest = ClearRequest.getDefaultInstance();
        ClearResponse clearResponse = frontend.ctrlClear(clearRequest);

        TraceRequest traceRequest2 = TraceRequest.newBuilder().setType("car").setId("20SD20").build();
        TraceResponse traceResponse2 = frontend.trace(traceRequest2);
        int size2 = traceResponse2.getObservationMessageListList().size();

        assertEquals(2,size2);

        frontend.get_channel().shutdownNow();

    }

}
