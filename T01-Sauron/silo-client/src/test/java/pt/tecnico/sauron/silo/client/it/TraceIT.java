package pt.tecnico.sauron.silo.client.it;

import com.google.protobuf.Timestamp;
import com.google.protobuf.util.Timestamps;

import static org.junit.jupiter.api.Assertions.assertEquals;


import org.junit.jupiter.api.Test;
import pt.tecnico.sauron.silo.client.BaseIT;
import pt.tecnico.sauron.silo.client.SiloFrontend;
import pt.tecnico.sauron.silo.grpc.Silo.*;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;

public class TraceIT extends BaseIT {

    @Test
    public void OnlyOneCarOccurrenceTraceTest() throws ZKNamingException {

        SiloFrontend frontend = new SiloFrontend("localhost","2181");

        InitRequest initRequest = InitRequest.newBuilder().setNumberOfReplicas(1).setInitializeReplicaTimestamp(1).build();
        InitResponse initResponse = frontend.ctrlInit(initRequest);

        CamJoinRequest camJoinRequest = CamJoinRequest.newBuilder().setName("Tagus").setLatitude(38.737613).setLongitude(-9.303164).build();
        CamJoinResponse camJoinResponse = frontend.camJoin(camJoinRequest);

        ReportRequest.Builder reportRequestBuilder = ReportRequest.newBuilder();

        ObservationMessage obs1 = ObservationMessage.newBuilder().setCameraName("Tagus").setTipo("car").setIdentifier("20SD20").build();
        reportRequestBuilder.addObservationMessageList(obs1);

        ObservationMessage obs2 = ObservationMessage.newBuilder().setCameraName("Tagus").setTipo("person").setIdentifier("5026726351").build();
        reportRequestBuilder.addObservationMessageList(obs2);

        ReportRequest reportRequest = reportRequestBuilder.build();
        ReportResponse reportResponse = frontend.report(reportRequest);

        TraceRequest traceRequest = TraceRequest.newBuilder().setType("car").setId("20SD20").build();
        TraceResponse traceResponse = frontend.trace(traceRequest);

        ObservationMessage obs = traceResponse.getObservationMessageListList().get(0);
        int size = traceResponse.getObservationMessageListList().size();

        assertEquals(1,size);
        assertEquals("Tagus", obs.getCameraName());
        assertEquals("car", obs.getTipo());
        assertEquals("20SD20", obs.getIdentifier());

        ClearRequest clearRequest = ClearRequest.getDefaultInstance();
        ClearResponse clearResponse = frontend.ctrlClear(clearRequest);

        frontend.get_channel().shutdownNow();

    }

    @Test
    public void VariousCarOccurrencesTraceTest() throws ZKNamingException, InterruptedException {

        SiloFrontend frontend = new SiloFrontend("localhost","2181");

        InitRequest initRequest = InitRequest.newBuilder().setNumberOfReplicas(1).setInitializeReplicaTimestamp(1).build();
        InitResponse initResponse = frontend.ctrlInit(initRequest);

        CamJoinRequest camJoinRequest = CamJoinRequest.newBuilder().setName("Tagus").setLatitude(38.737613).setLongitude(-9.303164).build();
        CamJoinResponse camJoinResponse = frontend.camJoin(camJoinRequest);

        CamJoinRequest camJoinRequest2 = CamJoinRequest.newBuilder().setName("Alameda").setLatitude(38.737613).setLongitude(-9.303164).build();
        CamJoinResponse camJoinResponse2 = frontend.camJoin(camJoinRequest2);

        ReportRequest.Builder reportRequestBuilder = ReportRequest.newBuilder();

        ObservationMessage obs1 = ObservationMessage.newBuilder().setCameraName("Tagus").setTipo("car").setIdentifier("20SD20").build();
        reportRequestBuilder.addObservationMessageList(obs1);

        ObservationMessage obs2 = ObservationMessage.newBuilder().setCameraName("Tagus").setTipo("person").setIdentifier("5026726351").build();
        reportRequestBuilder.addObservationMessageList(obs2);

        ReportRequest reportRequest = reportRequestBuilder.build();
        ReportResponse reportResponse = frontend.report(reportRequest);

        Thread.sleep(1000);

        ReportRequest.Builder reportRequestBuilder2 = ReportRequest.newBuilder();

        ObservationMessage obs3 = ObservationMessage.newBuilder().setCameraName("Alameda").setTipo("car").setIdentifier("20SD20").build();
        reportRequestBuilder2.addObservationMessageList(obs3);

        ReportRequest reportRequest2 = reportRequestBuilder2.build();
        ReportResponse reportResponse2 = frontend.report(reportRequest2);

        TraceRequest traceRequest = TraceRequest.newBuilder().setType("car").setId("20SD20").build();
        TraceResponse traceResponse = frontend.trace(traceRequest);

        int size = traceResponse.getObservationMessageListList().size();
        ObservationMessage obs4 = traceResponse.getObservationMessageListList().get(0);
        ObservationMessage obs5 = traceResponse.getObservationMessageListList().get(1);

        Timestamp timestamp1 = obs4.getObsDate();
        Timestamp timestamp2 = obs5.getObsDate();

        assertEquals(2,size);

        assertEquals(1, Timestamps.compare(timestamp1,timestamp2));
        assertEquals("Alameda", obs4.getCameraName());
        assertEquals("car", obs4.getTipo());
        assertEquals("20SD20", obs4.getIdentifier());
        assertEquals("Tagus", obs5.getCameraName());
        assertEquals("car", obs5.getTipo());
        assertEquals("20SD20", obs5.getIdentifier());

        ClearRequest clearRequest = ClearRequest.getDefaultInstance();
        ClearResponse clearResponse = frontend.ctrlClear(clearRequest);

        frontend.get_channel().shutdownNow();

    }

    @Test
    public void NoCarOccurrencesTraceTest() throws ZKNamingException {

        SiloFrontend frontend = new SiloFrontend("localhost","2181");

        InitRequest initRequest = InitRequest.newBuilder().setNumberOfReplicas(1).setInitializeReplicaTimestamp(1).build();
        InitResponse initResponse = frontend.ctrlInit(initRequest);

        TraceRequest traceRequest = TraceRequest.newBuilder().setType("car").setId("20SD20").build();
        TraceResponse traceResponse = frontend.trace(traceRequest);

        int size = traceResponse.getObservationMessageListList().size();

        assertEquals(0, size);

        ClearRequest clearRequest = ClearRequest.getDefaultInstance();
        ClearResponse clearResponse = frontend.ctrlClear(clearRequest);

        frontend.get_channel().shutdownNow();
    }

    @Test
    public void OnlyOnePersonOccurrenceTraceTest() throws ZKNamingException {

        SiloFrontend frontend = new SiloFrontend("localhost","2181");

        InitRequest initRequest = InitRequest.newBuilder().setNumberOfReplicas(1).setInitializeReplicaTimestamp(1).build();
        InitResponse initResponse = frontend.ctrlInit(initRequest);

        CamJoinRequest camJoinRequest = CamJoinRequest.newBuilder().setName("Tagus").setLatitude(38.737613).setLongitude(-9.303164).build();
        CamJoinResponse camJoinResponse = frontend.camJoin(camJoinRequest);

        ReportRequest.Builder reportRequestBuilder = ReportRequest.newBuilder();

        ObservationMessage obs1 = ObservationMessage.newBuilder().setCameraName("Tagus").setTipo("car").setIdentifier("20SD20").build();
        reportRequestBuilder.addObservationMessageList(obs1);

        ObservationMessage obs2 = ObservationMessage.newBuilder().setCameraName("Tagus").setTipo("person").setIdentifier("5026726351").build();
        reportRequestBuilder.addObservationMessageList(obs2);

        ReportRequest reportRequest = reportRequestBuilder.build();
        ReportResponse reportResponse = frontend.report(reportRequest);

        TraceRequest traceRequest = TraceRequest.newBuilder().setType("person").setId("5026726351").build();
        TraceResponse traceResponse = frontend.trace(traceRequest);

        ObservationMessage obs = traceResponse.getObservationMessageListList().get(0);
        int size = traceResponse.getObservationMessageListList().size();

        assertEquals(1,size);
        assertEquals("Tagus", obs.getCameraName());
        assertEquals("person", obs.getTipo());
        assertEquals("5026726351", obs.getIdentifier());

        ClearRequest clearRequest = ClearRequest.getDefaultInstance();
        ClearResponse clearResponse = frontend.ctrlClear(clearRequest);

        frontend.get_channel().shutdownNow();

    }

    @Test
    public void VariousPersonOccurrencesTraceTest() throws ZKNamingException, InterruptedException {

        SiloFrontend frontend = new SiloFrontend("localhost","2181");

        InitRequest initRequest = InitRequest.newBuilder().setNumberOfReplicas(1).setInitializeReplicaTimestamp(1).build();
        InitResponse initResponse = frontend.ctrlInit(initRequest);

        CamJoinRequest camJoinRequest = CamJoinRequest.newBuilder().setName("Tagus").setLatitude(38.737613).setLongitude(-9.303164).build();
        CamJoinResponse camJoinResponse = frontend.camJoin(camJoinRequest);

        CamJoinRequest camJoinRequest2 = CamJoinRequest.newBuilder().setName("Alameda").setLatitude(38.737613).setLongitude(-9.303164).build();
        CamJoinResponse camJoinResponse2 = frontend.camJoin(camJoinRequest2);

        ReportRequest.Builder reportRequestBuilder = ReportRequest.newBuilder();

        ObservationMessage obs1 = ObservationMessage.newBuilder().setCameraName("Tagus").setTipo("car").setIdentifier("20SD20").build();
        reportRequestBuilder.addObservationMessageList(obs1);

        ObservationMessage obs2 = ObservationMessage.newBuilder().setCameraName("Tagus").setTipo("person").setIdentifier("123").build();
        reportRequestBuilder.addObservationMessageList(obs2);

        ReportRequest reportRequest = reportRequestBuilder.build();
        ReportResponse reportResponse = frontend.report(reportRequest);

        Thread.sleep(1000);

        ReportRequest.Builder reportRequestBuilder2 = ReportRequest.newBuilder();

        ObservationMessage obs3 = ObservationMessage.newBuilder().setCameraName("Alameda").setTipo("person").setIdentifier("123").build();
        reportRequestBuilder2.addObservationMessageList(obs3);

        ReportRequest reportRequest2 = reportRequestBuilder2.build();
        ReportResponse reportResponse2 = frontend.report(reportRequest2);

        Thread.sleep(1000);

        ReportRequest.Builder reportRequestBuilder3 = ReportRequest.newBuilder();

        ObservationMessage obs6 = ObservationMessage.newBuilder().setCameraName("Tagus").setTipo("person").setIdentifier("123").build();
        reportRequestBuilder3.addObservationMessageList(obs6);

        ReportRequest reportRequest3 = reportRequestBuilder3.build();
        ReportResponse reportResponse3 = frontend.report(reportRequest3);

        TraceRequest traceRequest = TraceRequest.newBuilder().setType("person").setId("123").build();
        TraceResponse traceResponse = frontend.trace(traceRequest);

        int size = traceResponse.getObservationMessageListList().size();
        ObservationMessage obs4 = traceResponse.getObservationMessageListList().get(0);
        ObservationMessage obs5 = traceResponse.getObservationMessageListList().get(1);
        ObservationMessage obs7 = traceResponse.getObservationMessageListList().get(2);

        Timestamp timestamp1 = obs4.getObsDate();
        Timestamp timestamp2 = obs5.getObsDate();
        Timestamp timestamp3 = obs7.getObsDate();

        assertEquals(3,size);

        assertEquals(1, Timestamps.compare(timestamp1,timestamp2));
        assertEquals(1, Timestamps.compare(timestamp2,timestamp3));
        assertEquals(1, Timestamps.compare(timestamp1,timestamp3));

        assertEquals("Tagus", obs4.getCameraName());
        assertEquals("person", obs4.getTipo());
        assertEquals("123", obs4.getIdentifier());
        assertEquals("Alameda", obs5.getCameraName());
        assertEquals("person", obs5.getTipo());
        assertEquals("123", obs5.getIdentifier());
        assertEquals("Tagus", obs7.getCameraName());
        assertEquals("person", obs7.getTipo());
        assertEquals("123", obs7.getIdentifier());

        ClearRequest clearRequest = ClearRequest.getDefaultInstance();
        ClearResponse clearResponse = frontend.ctrlClear(clearRequest);

        frontend.get_channel().shutdownNow();

    }

    @Test
    public void NoPersonOccurrencesTraceTest() throws ZKNamingException {

        SiloFrontend frontend = new SiloFrontend("localhost","2181");

        InitRequest initRequest = InitRequest.newBuilder().setNumberOfReplicas(1).setInitializeReplicaTimestamp(1).build();
        InitResponse initResponse = frontend.ctrlInit(initRequest);

        TraceRequest traceRequest = TraceRequest.newBuilder().setType("person").setId("123").build();
        TraceResponse traceResponse = frontend.trace(traceRequest);

        int size = traceResponse.getObservationMessageListList().size();

        assertEquals(0, size);

        ClearRequest clearRequest = ClearRequest.getDefaultInstance();
        ClearResponse clearResponse = frontend.ctrlClear(clearRequest);

        frontend.get_channel().shutdownNow();
    }

    @Test
    public void NoRealTypeOccurrencesTraceTest() throws ZKNamingException {

        SiloFrontend frontend = new SiloFrontend("localhost","2181");

        InitRequest initRequest = InitRequest.newBuilder().setNumberOfReplicas(1).setInitializeReplicaTimestamp(1).build();
        InitResponse initResponse = frontend.ctrlInit(initRequest);

        TraceRequest traceRequest = TraceRequest.newBuilder().setType("type").setId("123").build();
        TraceResponse traceResponse = frontend.trace(traceRequest);

        int size = traceResponse.getObservationMessageListList().size();

        assertEquals(0, size);

        ClearRequest clearRequest = ClearRequest.getDefaultInstance();
        ClearResponse clearResponse = frontend.ctrlClear(clearRequest);

        frontend.get_channel().shutdownNow();
    }


}
