package pt.tecnico.sauron.silo.client.it;

import com.google.protobuf.Timestamp;
import com.google.protobuf.util.Timestamps;


import static org.junit.jupiter.api.Assertions.assertEquals;


import org.junit.jupiter.api.Test;
import pt.tecnico.sauron.silo.client.BaseIT;
import pt.tecnico.sauron.silo.client.SiloFrontend;
import pt.tecnico.sauron.silo.grpc.Silo.*;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;


public class TrackIT  extends BaseIT {

    @Test
    public void OnlyOneCarOccurrenceTrackTest() throws ZKNamingException {

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

        TrackRequest trackRequest = TrackRequest.newBuilder().setType("car").setId("20SD20").build();
        TrackResponse trackResponse = frontend.track(trackRequest);


        assertEquals("Tagus", trackResponse.getCameraName());
        assertEquals("car", trackResponse.getTipo());
        assertEquals("20SD20", trackResponse.getIdentifier());

        ClearRequest clearRequest = ClearRequest.getDefaultInstance();
        ClearResponse clearResponse = frontend.ctrlClear(clearRequest);

        frontend.get_channel().shutdownNow();

    }

    @Test
    public void VariousCarOccurrencesTrackTest() throws ZKNamingException {

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

        ObservationMessage obs3 = ObservationMessage.newBuilder().setCameraName("Tagus").setTipo("car").setIdentifier("20SD20").build();
        reportRequestBuilder.addObservationMessageList(obs3);
        ReportRequest reportRequest = reportRequestBuilder.build();
        ReportResponse reportResponse = frontend.report(reportRequest);


        TrackRequest trackRequest = TrackRequest.newBuilder().setType("car").setId("20SD20").build();
        TrackResponse trackResponse = frontend.track(trackRequest);

        TraceRequest traceRequest = TraceRequest.newBuilder().setType("car").setId("20SD20").build();
        TraceResponse traceResponse = frontend.trace(traceRequest);

        Timestamp timestamp1 = trackResponse.getObsDate();
        Timestamp timestamp2 = traceResponse.getObservationMessageListList().get(0).getObsDate();


        assertEquals("Tagus", trackResponse.getCameraName());
        assertEquals("car", trackResponse.getTipo());
        assertEquals("20SD20", trackResponse.getIdentifier());
        assertEquals(0, Timestamps.compare(timestamp1,timestamp2));

        ClearRequest clearRequest = ClearRequest.getDefaultInstance();
        ClearResponse clearResponse = frontend.ctrlClear(clearRequest);

        frontend.get_channel().shutdownNow();

    }

    @Test
    public void MostRecentCarOccurrenceTrackTest() throws ZKNamingException, InterruptedException {

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

        TrackRequest trackRequest = TrackRequest.newBuilder().setType("car").setId("20SD20").build();
        TrackResponse trackResponse = frontend.track(trackRequest);

        Thread.sleep(3000);

        ReportRequest.Builder reportRequestBuilder2 = ReportRequest.newBuilder();

        ObservationMessage obs3 = ObservationMessage.newBuilder().setCameraName("Tagus").setTipo("car").setIdentifier("20SD20").build();
        reportRequestBuilder2.addObservationMessageList(obs3);

        ReportRequest reportRequest2 = reportRequestBuilder2.build();
        ReportResponse reportResponse2 = frontend.report(reportRequest2);


        TrackRequest trackRequest2 = TrackRequest.newBuilder().setType("car").setId("20SD20").build();
        TrackResponse trackResponse2 = frontend.track(trackRequest2);


        Timestamp timestamp1 = trackResponse.getObsDate();
        Timestamp timestamp2 = trackResponse2.getObsDate();


        assertEquals("Tagus", trackResponse2.getCameraName());
        assertEquals("car", trackResponse2.getTipo());
        assertEquals("20SD20", trackResponse2.getIdentifier());
        assertEquals(-1, Timestamps.compare(timestamp1,timestamp2));

        ClearRequest clearRequest = ClearRequest.getDefaultInstance();
        ClearResponse clearResponse = frontend.ctrlClear(clearRequest);

        frontend.get_channel().shutdownNow();

    }

    @Test
    public void MostRecentCarDifferentCameraOccurrenceTrackTest() throws ZKNamingException, InterruptedException {

        SiloFrontend frontend = new SiloFrontend("localhost","2181");

        InitRequest initRequest = InitRequest.newBuilder().setNumberOfReplicas(1).setInitializeReplicaTimestamp(1).build();
        InitResponse initResponse = frontend.ctrlInit(initRequest);

        CamJoinRequest camJoinRequest = CamJoinRequest.newBuilder().setName("Tagus").setLatitude(38.737613).setLongitude(-9.303164).build();
        CamJoinResponse camJoinResponse = frontend.camJoin(camJoinRequest);

        CamJoinRequest camJoinRequest2 = CamJoinRequest.newBuilder().setName("Alameda").setLatitude(40.737613).setLongitude(-9.303164).build();
        CamJoinResponse camJoinResponse2 = frontend.camJoin(camJoinRequest2);

        ReportRequest.Builder reportRequestBuilder = ReportRequest.newBuilder();
        ObservationMessage obs1 = ObservationMessage.newBuilder().setCameraName("Tagus").setTipo("car").setIdentifier("20SD20").build();
        reportRequestBuilder.addObservationMessageList(obs1);
        ObservationMessage obs2 = ObservationMessage.newBuilder().setCameraName("Tagus").setTipo("person").setIdentifier("5026726351").build();
        reportRequestBuilder.addObservationMessageList(obs2);

        ReportRequest reportRequest = reportRequestBuilder.build();
        ReportResponse reportResponse = frontend.report(reportRequest);

        TrackRequest trackRequest = TrackRequest.newBuilder().setType("car").setId("20SD20").build();
        TrackResponse trackResponse = frontend.track(trackRequest);

        Thread.sleep(3000);

        ReportRequest.Builder reportRequestBuilder2 = ReportRequest.newBuilder();

        ObservationMessage obs3 = ObservationMessage.newBuilder().setCameraName("Alameda").setTipo("car").setIdentifier("20SD20").build();
        reportRequestBuilder2.addObservationMessageList(obs3);

        ReportRequest reportRequest2 = reportRequestBuilder2.build();
        ReportResponse reportResponse2 = frontend.report(reportRequest2);


        TrackRequest trackRequest2 = TrackRequest.newBuilder().setType("car").setId("20SD20").build();
        TrackResponse trackResponse2 = frontend.track(trackRequest2);


        Timestamp timestamp1 = trackResponse.getObsDate();
        Timestamp timestamp2 = trackResponse2.getObsDate();


        assertEquals("Tagus", trackResponse.getCameraName());
        assertEquals("car", trackResponse.getTipo());
        assertEquals("20SD20", trackResponse.getIdentifier());
        assertEquals("Alameda", trackResponse2.getCameraName());
        assertEquals("car", trackResponse2.getTipo());
        assertEquals("20SD20", trackResponse2.getIdentifier());
        assertEquals(-1, Timestamps.compare(timestamp1,timestamp2));

        ClearRequest clearRequest = ClearRequest.getDefaultInstance();
        ClearResponse clearResponse = frontend.ctrlClear(clearRequest);

        frontend.get_channel().shutdownNow();

    }

    @Test
    public void NoOccurrencesCarTrackTest() throws ZKNamingException {

        SiloFrontend frontend = new SiloFrontend("localhost","2181");

        InitRequest initRequest = InitRequest.newBuilder().setNumberOfReplicas(1).setInitializeReplicaTimestamp(1).build();
        InitResponse initResponse = frontend.ctrlInit(initRequest);

        TrackRequest trackRequest = TrackRequest.newBuilder().setType("car").setId("20SD30").build();
        TrackResponse trackResponse = frontend.track(trackRequest);

        assertEquals("", trackResponse.getCameraName());

        ClearRequest clearRequest = ClearRequest.getDefaultInstance();
        ClearResponse clearResponse = frontend.ctrlClear(clearRequest);

        frontend.get_channel().shutdownNow();
    }


    @Test
    public void OnlyOnePersonOccurrenceTrackTest() throws ZKNamingException {

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

        TrackRequest trackRequest = TrackRequest.newBuilder().setType("person").setId("5026726351").build();
        TrackResponse trackResponse = frontend.track(trackRequest);


        assertEquals("Tagus", trackResponse.getCameraName());
        assertEquals("person", trackResponse.getTipo());
        assertEquals("5026726351", trackResponse.getIdentifier());

        ClearRequest clearRequest = ClearRequest.getDefaultInstance();
        ClearResponse clearResponse = frontend.ctrlClear(clearRequest);

        frontend.get_channel().shutdownNow();

    }


    @Test
    public void VariousPersonOccurrencesTrackTest() throws ZKNamingException {

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

        ObservationMessage obs3 = ObservationMessage.newBuilder().setCameraName("Tagus").setTipo("car").setIdentifier("20SD20").build();
        reportRequestBuilder.addObservationMessageList(obs3);

        ObservationMessage obs4 = ObservationMessage.newBuilder().setCameraName("Tagus").setTipo("person").setIdentifier("5026726351").build();
        reportRequestBuilder.addObservationMessageList(obs4);

        ReportRequest reportRequest = reportRequestBuilder.build();
        ReportResponse reportResponse = frontend.report(reportRequest);


        TrackRequest trackRequest = TrackRequest.newBuilder().setType("person").setId("5026726351").build();
        TrackResponse trackResponse = frontend.track(trackRequest);

        TraceRequest traceRequest = TraceRequest.newBuilder().setType("person").setId("5026726351").build();
        TraceResponse traceResponse = frontend.trace(traceRequest);

        Timestamp timestamp1 = trackResponse.getObsDate();
        Timestamp timestamp2 = traceResponse.getObservationMessageListList().get(0).getObsDate();


        assertEquals("Tagus", trackResponse.getCameraName());
        assertEquals("person", trackResponse.getTipo());
        assertEquals("5026726351", trackResponse.getIdentifier());
        assertEquals(0, Timestamps.compare(timestamp1,timestamp2));

        ClearRequest clearRequest = ClearRequest.getDefaultInstance();
        ClearResponse clearResponse = frontend.ctrlClear(clearRequest);

        frontend.get_channel().shutdownNow();
    }

    @Test
    public void MostRecentPersonOccurrenceTrackTest() throws ZKNamingException, InterruptedException {

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

        TrackRequest trackRequest = TrackRequest.newBuilder().setType("person").setId("5026726351").build();
        TrackResponse trackResponse = frontend.track(trackRequest);

        Thread.sleep(3000);

        ReportRequest.Builder reportRequestBuilder2 = ReportRequest.newBuilder();

        ObservationMessage obs3 = ObservationMessage.newBuilder().setCameraName("Tagus").setTipo("person").setIdentifier("5026726351").build();
        reportRequestBuilder2.addObservationMessageList(obs3);

        ReportRequest reportRequest2 = reportRequestBuilder2.build();
        ReportResponse reportResponse2 = frontend.report(reportRequest2);


        TrackRequest trackRequest2 = TrackRequest.newBuilder().setType("person").setId("5026726351").build();
        TrackResponse trackResponse2 = frontend.track(trackRequest2);


        Timestamp timestamp1 = trackResponse.getObsDate();
        Timestamp timestamp2 = trackResponse2.getObsDate();


        assertEquals("Tagus", trackResponse2.getCameraName());
        assertEquals("person", trackResponse2.getTipo());
        assertEquals("5026726351", trackResponse2.getIdentifier());
        assertEquals(-1, Timestamps.compare(timestamp1,timestamp2));

        ClearRequest clearRequest = ClearRequest.getDefaultInstance();
        ClearResponse clearResponse = frontend.ctrlClear(clearRequest);

        frontend.get_channel().shutdownNow();

    }

    @Test
    public void MostRecentPersonDifferentCameraOccurrenceTrackTest() throws ZKNamingException, InterruptedException {

        SiloFrontend frontend = new SiloFrontend("localhost","2181");

        InitRequest initRequest = InitRequest.newBuilder().setNumberOfReplicas(1).setInitializeReplicaTimestamp(1).build();
        InitResponse initResponse = frontend.ctrlInit(initRequest);

        CamJoinRequest camJoinRequest = CamJoinRequest.newBuilder().setName("Tagus").setLatitude(38.737613).setLongitude(-9.303164).build();
        CamJoinResponse camJoinResponse = frontend.camJoin(camJoinRequest);

        CamJoinRequest camJoinRequest2 = CamJoinRequest.newBuilder().setName("Alameda").setLatitude(40.737613).setLongitude(-9.303164).build();
        CamJoinResponse camJoinResponse2 = frontend.camJoin(camJoinRequest2);

        ReportRequest.Builder reportRequestBuilder = ReportRequest.newBuilder();
        ObservationMessage obs1 = ObservationMessage.newBuilder().setCameraName("Tagus").setTipo("car").setIdentifier("20SD20").build();
        reportRequestBuilder.addObservationMessageList(obs1);
        ObservationMessage obs2 = ObservationMessage.newBuilder().setCameraName("Tagus").setTipo("person").setIdentifier("5026726351").build();
        reportRequestBuilder.addObservationMessageList(obs2);

        ReportRequest reportRequest = reportRequestBuilder.build();
        ReportResponse reportResponse = frontend.report(reportRequest);

        TrackRequest trackRequest = TrackRequest.newBuilder().setType("person").setId("5026726351").build();
        TrackResponse trackResponse = frontend.track(trackRequest);

        Thread.sleep(3000);

        ReportRequest.Builder reportRequestBuilder2 = ReportRequest.newBuilder();

        ObservationMessage obs3 = ObservationMessage.newBuilder().setCameraName("Alameda").setTipo("person").setIdentifier("5026726351").build();
        reportRequestBuilder2.addObservationMessageList(obs3);

        ReportRequest reportRequest2 = reportRequestBuilder2.build();
        ReportResponse reportResponse2 = frontend.report(reportRequest2);


        TrackRequest trackRequest2 = TrackRequest.newBuilder().setType("person").setId("5026726351").build();
        TrackResponse trackResponse2 = frontend.track(trackRequest2);


        Timestamp timestamp1 = trackResponse.getObsDate();
        Timestamp timestamp2 = trackResponse2.getObsDate();


        assertEquals("Tagus", trackResponse.getCameraName());
        assertEquals("person", trackResponse.getTipo());
        assertEquals("5026726351", trackResponse.getIdentifier());
        assertEquals("Alameda", trackResponse2.getCameraName());
        assertEquals("person", trackResponse2.getTipo());
        assertEquals("5026726351", trackResponse2.getIdentifier());
        assertEquals(-1, Timestamps.compare(timestamp1,timestamp2));

        ClearRequest clearRequest = ClearRequest.getDefaultInstance();
        ClearResponse clearResponse = frontend.ctrlClear(clearRequest);

        frontend.get_channel().shutdownNow();

    }

    @Test
    public void NoOccurrencesPersonTrackTest() throws ZKNamingException {

        SiloFrontend frontend = new SiloFrontend("localhost","2181");

        InitRequest initRequest = InitRequest.newBuilder().setNumberOfReplicas(1).setInitializeReplicaTimestamp(1).build();
        InitResponse initResponse = frontend.ctrlInit(initRequest);

        TrackRequest trackRequest = TrackRequest.newBuilder().setType("person").setId("5026726351").build();
        TrackResponse trackResponse = frontend.track(trackRequest);

        assertEquals("", trackResponse.getCameraName());

        ClearRequest clearRequest = ClearRequest.getDefaultInstance();
        ClearResponse clearResponse = frontend.ctrlClear(clearRequest);

        frontend.get_channel().shutdownNow();
    }

    @Test
    public void NoRealTypeTrackTest() throws ZKNamingException {

        SiloFrontend frontend = new SiloFrontend("localhost","2181");

        InitRequest initRequest = InitRequest.newBuilder().setNumberOfReplicas(1).setInitializeReplicaTimestamp(1).build();
        InitResponse initResponse = frontend.ctrlInit(initRequest);

        TrackRequest trackRequest = TrackRequest.newBuilder().setType("type").setId("5026726351").build();
        TrackResponse trackResponse = frontend.track(trackRequest);

        assertEquals("", trackResponse.getCameraName());

        ClearRequest clearRequest = ClearRequest.getDefaultInstance();
        ClearResponse clearResponse = frontend.ctrlClear(clearRequest);

        frontend.get_channel().shutdownNow();
    }
}
