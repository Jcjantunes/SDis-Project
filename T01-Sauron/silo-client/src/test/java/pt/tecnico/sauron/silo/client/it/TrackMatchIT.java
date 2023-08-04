package pt.tecnico.sauron.silo.client.it;

import com.google.protobuf.Timestamp;
import com.google.protobuf.util.Timestamps;
import org.junit.jupiter.api.Test;
import pt.tecnico.sauron.silo.client.BaseIT;
import pt.tecnico.sauron.silo.client.SiloFrontend;
import pt.tecnico.sauron.silo.grpc.Silo.*;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TrackMatchIT extends BaseIT {

    @Test
    public void OnlyOneCarOccurrenceTrackMatchTest() throws ZKNamingException {

        SiloFrontend frontend = new SiloFrontend("localhost","2181");

        InitRequest initRequest = InitRequest.newBuilder().setNumberOfReplicas(1).setInitializeReplicaTimestamp(1).build();
        InitResponse initResponse = frontend.ctrlInit(initRequest);

        CamJoinRequest camJoinRequest = CamJoinRequest.newBuilder().setName("Tagus").setLatitude(38.737613).setLongitude(-9.303164).build();
        CamJoinResponse camJoinResponse = frontend.camJoin(camJoinRequest);

        ReportRequest.Builder reportRequestBuilder = ReportRequest.newBuilder();

        ObservationMessage obs1 = ObservationMessage.newBuilder().setCameraName("Tagus").setTipo("car").setIdentifier("20SD20").build();
        reportRequestBuilder.addObservationMessageList(obs1);

        ObservationMessage obs2 = ObservationMessage.newBuilder().setCameraName("Tagus").setTipo("person").setIdentifier("2026726320").build();
        reportRequestBuilder.addObservationMessageList(obs2);

        ReportRequest reportRequest = reportRequestBuilder.build();
        ReportResponse reportResponse = frontend.report(reportRequest);

        TrackMatchRequest trackMatchRequest = TrackMatchRequest.newBuilder().setType("car").setId("20*").build();
        TrackMatchResponse trackMatchResponse = frontend.trackMatch(trackMatchRequest);

        ObservationMessage obs3 = trackMatchResponse.getObservationMessageListList().get(0);
        int size = trackMatchResponse.getObservationMessageListList().size();

        assertEquals(1,size);
        assertEquals("Tagus", obs3.getCameraName());
        assertEquals("car", obs3.getTipo());
        assertEquals("20SD20", obs3.getIdentifier());

        TrackMatchRequest trackMatchRequest2 = TrackMatchRequest.newBuilder().setType("car").setId("*20").build();
        TrackMatchResponse trackMatchResponse2 = frontend.trackMatch(trackMatchRequest2);

        ObservationMessage obs4 = trackMatchResponse2.getObservationMessageListList().get(0);
        int size2 = trackMatchResponse2.getObservationMessageListList().size();

        assertEquals(1,size2);
        assertEquals("Tagus", obs4.getCameraName());
        assertEquals("car", obs4.getTipo());
        assertEquals("20SD20", obs4.getIdentifier());

        TrackMatchRequest trackMatchRequest3 = TrackMatchRequest.newBuilder().setType("car").setId("*20").build();
        TrackMatchResponse trackMatchResponse3 = frontend.trackMatch(trackMatchRequest3);

        ObservationMessage obs5 = trackMatchResponse3.getObservationMessageListList().get(0);
        int size3 = trackMatchResponse3.getObservationMessageListList().size();

        assertEquals(1,size3);
        assertEquals("Tagus", obs5.getCameraName());
        assertEquals("car", obs5.getTipo());
        assertEquals("20SD20", obs5.getIdentifier());

        TrackMatchRequest trackMatchRequest4 = TrackMatchRequest.newBuilder().setType("car").setId("20*20").build();
        TrackMatchResponse trackMatchResponse4 = frontend.trackMatch(trackMatchRequest4);

        ObservationMessage obs6 = trackMatchResponse4.getObservationMessageListList().get(0);
        int size4 = trackMatchResponse4.getObservationMessageListList().size();

        assertEquals(1,size4);
        assertEquals("Tagus", obs6.getCameraName());
        assertEquals("car", obs6.getTipo());
        assertEquals("20SD20", obs6.getIdentifier());

        ClearRequest clearRequest = ClearRequest.getDefaultInstance();
        ClearResponse clearResponse = frontend.ctrlClear(clearRequest);

        frontend.get_channel().shutdownNow();

    }

    @Test
    public void VariousCarOccurrencesTrackMatchTest() throws ZKNamingException {

        SiloFrontend frontend = new SiloFrontend("localhost","2181");

        InitRequest initRequest = InitRequest.newBuilder().setNumberOfReplicas(1).setInitializeReplicaTimestamp(1).build();
        InitResponse initResponse = frontend.ctrlInit(initRequest);

        CamJoinRequest camJoinRequest1 = CamJoinRequest.newBuilder().setName("Tagus").setLatitude(38.737613).setLongitude(-9.303164).build();
        CamJoinResponse camJoinResponse1 = frontend.camJoin(camJoinRequest1);

        ReportRequest.Builder reportRequestBuilder1 = ReportRequest.newBuilder();

        ObservationMessage obs1 = ObservationMessage.newBuilder().setCameraName("Tagus").setTipo("car").setIdentifier("20SD20").build();
        reportRequestBuilder1.addObservationMessageList(obs1);

        ObservationMessage obs2 = ObservationMessage.newBuilder().setCameraName("Tagus").setTipo("person").setIdentifier("2026726324").build();
        reportRequestBuilder1.addObservationMessageList(obs2);

        ReportRequest reportRequest1 = reportRequestBuilder1.build();
        ReportResponse reportResponse1 = frontend.report(reportRequest1);

        CamJoinRequest camJoinRequest2 = CamJoinRequest.newBuilder().setName("Alameda").setLatitude(40.737613).setLongitude(-10.303164).build();
        CamJoinResponse camJoinResponse2 = frontend.camJoin(camJoinRequest2);

        ObservationMessage obs3 = ObservationMessage.newBuilder().setCameraName("Alameda").setTipo("car").setIdentifier("20SD24").build();

        ReportRequest.Builder reportRequestBuilder2 = ReportRequest.newBuilder();
        reportRequestBuilder2.addObservationMessageList(obs3);

        ReportRequest reportRequest2 = reportRequestBuilder2.build();
        ReportResponse reportResponse2 = frontend.report(reportRequest2);

        TrackMatchRequest trackMatchRequest = TrackMatchRequest.newBuilder().setType("car").setId("20*").build();
        TrackMatchResponse trackMatchResponse = frontend.trackMatch(trackMatchRequest);

        List<ObservationMessage> obsList = trackMatchResponse.getObservationMessageListList();
        int size = trackMatchResponse.getObservationMessageListList().size();

        ObservationMessage observation1 = obsList.get(0);
        ObservationMessage observation2 = obsList.get(1);

        assertEquals(2,size);
        assertEquals("Tagus", observation1.getCameraName());
        assertEquals("Alameda", observation2.getCameraName());
        assertEquals("car", observation1.getTipo());
        assertEquals("car", observation2.getTipo());
        assertEquals("20SD20", observation1.getIdentifier());
        assertEquals("20SD24", observation2.getIdentifier());

        ObservationMessage obs4 = ObservationMessage.newBuilder().setCameraName("Alameda").setTipo("car").setIdentifier("30SD24").build();

        ObservationMessage obs5 = ObservationMessage.newBuilder().setCameraName("Tagus").setTipo("car").setIdentifier("40SD24").build();

        ReportRequest.Builder reportRequestBuilder3 = ReportRequest.newBuilder();

        reportRequestBuilder3.addObservationMessageList(obs4);
        reportRequestBuilder3.addObservationMessageList(obs5);

        ReportRequest reportRequest3 = reportRequestBuilder3.build();
        ReportResponse reportResponse3 = frontend.report(reportRequest3);

        TrackMatchRequest trackMatchRequest2 = TrackMatchRequest.newBuilder().setType("car").setId("*24").build();
        TrackMatchResponse trackMatchResponse2 = frontend.trackMatch(trackMatchRequest2);

        List<ObservationMessage> obsList2 = trackMatchResponse2.getObservationMessageListList();
        int size2 = trackMatchResponse2.getObservationMessageListList().size();

        ObservationMessage observation3 = obsList2.get(0);
        ObservationMessage observation4 = obsList2.get(1);
        ObservationMessage observation5 = obsList2.get(2);

        assertEquals(3,size2);
        assertEquals("Alameda", observation3.getCameraName());
        assertEquals("Tagus", observation4.getCameraName());
        assertEquals("Alameda", observation5.getCameraName());
        assertEquals("car", observation3.getTipo());
        assertEquals("car", observation4.getTipo());
        assertEquals("car", observation5.getTipo());
        assertEquals("30SD24", observation3.getIdentifier());
        assertEquals("40SD24", observation4.getIdentifier());
        assertEquals("20SD24", observation5.getIdentifier());

        CamJoinRequest camJoinRequest3 = CamJoinRequest.newBuilder().setName("Porto").setLatitude(50.737613).setLongitude(-10.303164).build();
        CamJoinResponse camJoinResponse3 = frontend.camJoin(camJoinRequest3);

        ObservationMessage obs6 = ObservationMessage.newBuilder().setCameraName("Porto").setTipo("car").setIdentifier("31SD34").build();

        ReportRequest.Builder reportRequestBuilder4 = ReportRequest.newBuilder();
        reportRequestBuilder4.addObservationMessageList(obs6);

        ReportRequest reportRequest4 = reportRequestBuilder4.build();
        ReportResponse reportResponse4 = frontend.report(reportRequest4);

        TrackMatchRequest trackMatchRequest3 = TrackMatchRequest.newBuilder().setType("car").setId("3*4").build();
        TrackMatchResponse trackMatchResponse3 = frontend.trackMatch(trackMatchRequest3);

        List<ObservationMessage> obsList3 = trackMatchResponse3.getObservationMessageListList();
        int size3 = trackMatchResponse3.getObservationMessageListList().size();

        ObservationMessage observation6 = obsList3.get(0);
        ObservationMessage observation7 = obsList3.get(1);

        assertEquals(2,size3);
        assertEquals("Porto", observation6.getCameraName());
        assertEquals("Alameda", observation7.getCameraName());
        assertEquals("car", observation6.getTipo());
        assertEquals("car", observation7.getTipo());
        assertEquals("31SD34", observation6.getIdentifier());
        assertEquals("30SD24", observation7.getIdentifier());

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

        TrackMatchRequest trackMatchRequest = TrackMatchRequest.newBuilder().setType("car").setId("20*").build();
        TrackMatchResponse trackMatchResponse = frontend.trackMatch(trackMatchRequest);

        Thread.sleep(3000);

        ReportRequest.Builder reportRequestBuilder2 = ReportRequest.newBuilder();

        ObservationMessage obs3 = ObservationMessage.newBuilder().setCameraName("Tagus").setTipo("car").setIdentifier("20SD20").build();
        reportRequestBuilder2.addObservationMessageList(obs3);

        ReportRequest reportRequest2 = reportRequestBuilder2.build();
        ReportResponse reportResponse2 = frontend.report(reportRequest2);


        TrackMatchRequest trackMatchRequest2 = TrackMatchRequest.newBuilder().setType("car").setId("*20").build();
        TrackMatchResponse trackMatchResponse2 = frontend.trackMatch(trackMatchRequest2);

        ObservationMessage obs4 = trackMatchResponse.getObservationMessageList(0);
        ObservationMessage obs5 = trackMatchResponse2.getObservationMessageList(0);

        Timestamp timestamp1 = obs4.getObsDate();
        Timestamp timestamp2 = obs5.getObsDate();


        assertEquals("Tagus", obs5.getCameraName());
        assertEquals("car", obs5.getTipo());
        assertEquals("20SD20", obs5.getIdentifier());
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

        TrackMatchRequest trackMatchRequest = TrackMatchRequest.newBuilder().setType("car").setId("20*").build();
        TrackMatchResponse trackMatchResponse = frontend.trackMatch(trackMatchRequest);

        Thread.sleep(3000);

        ReportRequest.Builder reportRequestBuilder2 = ReportRequest.newBuilder();

        ObservationMessage obs3 = ObservationMessage.newBuilder().setCameraName("Alameda").setTipo("car").setIdentifier("20SD20").build();
        reportRequestBuilder2.addObservationMessageList(obs3);

        ReportRequest reportRequest2 = reportRequestBuilder2.build();
        ReportResponse reportResponse2 = frontend.report(reportRequest2);


        TrackMatchRequest trackMatchRequest2 = TrackMatchRequest.newBuilder().setType("car").setId("20*").build();
        TrackMatchResponse trackMatchResponse2 = frontend.trackMatch(trackMatchRequest2);


        ObservationMessage obs4 = trackMatchResponse.getObservationMessageList(0);
        ObservationMessage obs5 = trackMatchResponse2.getObservationMessageList(0);

        Timestamp timestamp1 = obs4.getObsDate();
        Timestamp timestamp2 = obs5.getObsDate();


        assertEquals("Tagus", obs4.getCameraName());
        assertEquals("car", obs4.getTipo());
        assertEquals("20SD20", obs4.getIdentifier());
        assertEquals("Alameda", obs5.getCameraName());
        assertEquals("car", obs5.getTipo());
        assertEquals("20SD20", obs5.getIdentifier());
        assertEquals(-1, Timestamps.compare(timestamp1,timestamp2));

        ClearRequest clearRequest = ClearRequest.getDefaultInstance();
        ClearResponse clearResponse = frontend.ctrlClear(clearRequest);

        frontend.get_channel().shutdownNow();

    }

    @Test
    public void NoCarOccurrencesTrackMatchTest() throws ZKNamingException {

        SiloFrontend frontend = new SiloFrontend("localhost","2181");

        InitRequest initRequest = InitRequest.newBuilder().setNumberOfReplicas(1).setInitializeReplicaTimestamp(1).build();
        InitResponse initResponse = frontend.ctrlInit(initRequest);

        TrackMatchRequest trackMatchRequest = TrackMatchRequest.newBuilder().setType("car").setId("*30").build();
        TrackMatchResponse trackMatchResponse = frontend.trackMatch(trackMatchRequest);

        int size = trackMatchResponse.getObservationMessageListList().size();

        assertEquals(0, size);

        ClearRequest clearRequest = ClearRequest.getDefaultInstance();
        ClearResponse clearResponse = frontend.ctrlClear(clearRequest);

        frontend.get_channel().shutdownNow();

    }

    @Test
    public void OnlyOnePersonOccurrenceTrackMatchTest() throws ZKNamingException {

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

        TrackMatchRequest trackMatchRequest3 = TrackMatchRequest.newBuilder().setType("car").setId("20*").build();
        TrackMatchResponse trackMatchResponse3 = frontend.trackMatch(trackMatchRequest3);

        ObservationMessage obs3 = trackMatchResponse3.getObservationMessageListList().get(0);
        int size3 = trackMatchResponse3.getObservationMessageListList().size();

        assertEquals(1,size3);
        assertEquals("Tagus", obs3.getCameraName());
        assertEquals("car", obs3.getTipo());
        assertEquals("20SD20", obs3.getIdentifier());

        TrackMatchRequest trackMatchRequest4 = TrackMatchRequest.newBuilder().setType("person").setId("*51").build();
        TrackMatchResponse trackMatchResponse4 = frontend.trackMatch(trackMatchRequest4);

        ObservationMessage obs4 = trackMatchResponse4.getObservationMessageListList().get(0);
        int size4 = trackMatchResponse4.getObservationMessageListList().size();

        assertEquals(1,size4);
        assertEquals("Tagus", obs4.getCameraName());
        assertEquals("person", obs4.getTipo());
        assertEquals("5026726351", obs4.getIdentifier());

        ClearRequest clearRequest = ClearRequest.getDefaultInstance();
        ClearResponse clearResponse = frontend.ctrlClear(clearRequest);

        frontend.get_channel().shutdownNow();

    }

    @Test
    public void VariousPersonsOccurrencesTrackMatchTest() throws ZKNamingException {

        SiloFrontend frontend = new SiloFrontend("localhost","2181");

        InitRequest initRequest = InitRequest.newBuilder().setNumberOfReplicas(1).setInitializeReplicaTimestamp(1).build();
        InitResponse initResponse = frontend.ctrlInit(initRequest);

        CamJoinRequest camJoinRequest1 = CamJoinRequest.newBuilder().setName("Tagus").setLatitude(38.737613).setLongitude(-9.303164).build();
        CamJoinResponse camJoinResponse1 = frontend.camJoin(camJoinRequest1);

        ReportRequest.Builder reportRequestBuilder1 = ReportRequest.newBuilder();

        ObservationMessage obs1 = ObservationMessage.newBuilder().setCameraName("Tagus").setTipo("car").setIdentifier("20SD20").build();
        reportRequestBuilder1.addObservationMessageList(obs1);

        ObservationMessage obs2 = ObservationMessage.newBuilder().setCameraName("Tagus").setTipo("person").setIdentifier("2026726324").build();
        reportRequestBuilder1.addObservationMessageList(obs2);

        ReportRequest reportRequest1 = reportRequestBuilder1.build();
        ReportResponse reportResponse1 = frontend.report(reportRequest1);

        CamJoinRequest camJoinRequest2 = CamJoinRequest.newBuilder().setName("Alameda").setLatitude(40.737613).setLongitude(-10.303164).build();
        CamJoinResponse camJoinResponse2 = frontend.camJoin(camJoinRequest2);

        ObservationMessage obs3 = ObservationMessage.newBuilder().setCameraName("Alameda").setTipo("person").setIdentifier("20123456").build();

        ReportRequest.Builder reportRequestBuilder2 = ReportRequest.newBuilder();
        reportRequestBuilder2.addObservationMessageList(obs3);

        ReportRequest reportRequest2 = reportRequestBuilder2.build();
        ReportResponse reportResponse2 = frontend.report(reportRequest2);

        TrackMatchRequest trackMatchRequest = TrackMatchRequest.newBuilder().setType("person").setId("20*").build();
        TrackMatchResponse trackMatchResponse = frontend.trackMatch(trackMatchRequest);

        List<ObservationMessage> obsList = trackMatchResponse.getObservationMessageListList();
        int size = trackMatchResponse.getObservationMessageListList().size();

        ObservationMessage observation1 = obsList.get(0);
        ObservationMessage observation2 = obsList.get(1);

        assertEquals(2,size);
        assertEquals("Tagus", observation1.getCameraName());
        assertEquals("Alameda", observation2.getCameraName());
        assertEquals("person", observation1.getTipo());
        assertEquals("person", observation2.getTipo());
        assertEquals("2026726324", observation1.getIdentifier());
        assertEquals("20123456", observation2.getIdentifier());

        ObservationMessage obs4 = ObservationMessage.newBuilder().setCameraName("Alameda").setTipo("person").setIdentifier("2012324").build();

        ObservationMessage obs5 = ObservationMessage.newBuilder().setCameraName("Tagus").setTipo("person").setIdentifier("20432124").build();

        ReportRequest.Builder reportRequestBuilder3 = ReportRequest.newBuilder();

        reportRequestBuilder3.addObservationMessageList(obs4);
        reportRequestBuilder3.addObservationMessageList(obs5);

        ReportRequest reportRequest3 = reportRequestBuilder3.build();
        ReportResponse reportResponse3 = frontend.report(reportRequest3);

        TrackMatchRequest trackMatchRequest2 = TrackMatchRequest.newBuilder().setType("person").setId("*24").build();
        TrackMatchResponse trackMatchResponse2 = frontend.trackMatch(trackMatchRequest2);

        List<ObservationMessage> obsList2 = trackMatchResponse2.getObservationMessageListList();
        int size2 = trackMatchResponse2.getObservationMessageListList().size();

        ObservationMessage observation3 = obsList2.get(0);
        ObservationMessage observation4 = obsList2.get(1);
        ObservationMessage observation5 = obsList2.get(2);

        assertEquals(3,size2);
        assertEquals("Tagus", observation3.getCameraName());
        assertEquals("Tagus", observation4.getCameraName());
        assertEquals("Alameda", observation5.getCameraName());
        assertEquals("person", observation3.getTipo());
        assertEquals("person", observation4.getTipo());
        assertEquals("person", observation5.getTipo());
        assertEquals("2026726324", observation3.getIdentifier());
        assertEquals("20432124", observation4.getIdentifier());
        assertEquals("2012324", observation5.getIdentifier());

        CamJoinRequest camJoinRequest3 = CamJoinRequest.newBuilder().setName("Porto").setLatitude(50.737613).setLongitude(-10.303164).build();
        CamJoinResponse camJoinResponse3 = frontend.camJoin(camJoinRequest3);

        ObservationMessage obs6 = ObservationMessage.newBuilder().setCameraName("Porto").setTipo("person").setIdentifier("202124").build();

        ReportRequest.Builder reportRequestBuilder4 = ReportRequest.newBuilder();
        reportRequestBuilder4.addObservationMessageList(obs6);

        ReportRequest reportRequest4 = reportRequestBuilder4.build();
        ReportResponse reportResponse4 = frontend.report(reportRequest4);

        TrackMatchRequest trackMatchRequest3 = TrackMatchRequest.newBuilder().setType("person").setId("20*24").build();
        TrackMatchResponse trackMatchResponse3 = frontend.trackMatch(trackMatchRequest3);

        List<ObservationMessage> obsList3 = trackMatchResponse3.getObservationMessageListList();
        int size3 = trackMatchResponse3.getObservationMessageListList().size();

        ObservationMessage observation6 = obsList3.get(0);
        ObservationMessage observation7 = obsList3.get(1);
        ObservationMessage observation8 = obsList3.get(2);
        ObservationMessage observation9 = obsList3.get(3);

        assertEquals(4,size3);
        assertEquals("Tagus", observation6.getCameraName());
        assertEquals("Porto", observation7.getCameraName());
        assertEquals("Tagus", observation8.getCameraName());
        assertEquals("Alameda", observation9.getCameraName());
        assertEquals("person", observation6.getTipo());
        assertEquals("person", observation7.getTipo());
        assertEquals("person", observation8.getTipo());
        assertEquals("person", observation9.getTipo());
        assertEquals("2026726324", observation6.getIdentifier());
        assertEquals("202124", observation7.getIdentifier());
        assertEquals("20432124", observation8.getIdentifier());
        assertEquals("2012324", observation9.getIdentifier());

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

        TrackMatchRequest trackMatchRequest = TrackMatchRequest.newBuilder().setType("person").setId("50267*").build();
        TrackMatchResponse trackMatchResponse = frontend.trackMatch(trackMatchRequest);

        Thread.sleep(3000);

        ReportRequest.Builder reportRequestBuilder2 = ReportRequest.newBuilder();

        ObservationMessage obs3 = ObservationMessage.newBuilder().setCameraName("Tagus").setTipo("person").setIdentifier("5026726351").build();
        reportRequestBuilder2.addObservationMessageList(obs3);

        ReportRequest reportRequest2 = reportRequestBuilder2.build();
        ReportResponse reportResponse2 = frontend.report(reportRequest2);


        TrackMatchRequest trackMatchRequest2 = TrackMatchRequest.newBuilder().setType("person").setId("*351").build();
        TrackMatchResponse trackMatchResponse2 = frontend.trackMatch(trackMatchRequest2);


        ObservationMessage obs4 = trackMatchResponse.getObservationMessageList(0);
        ObservationMessage obs5 = trackMatchResponse2.getObservationMessageList(0);

        Timestamp timestamp1 = obs4.getObsDate();
        Timestamp timestamp2 = obs5.getObsDate();


        assertEquals("Tagus", obs5.getCameraName());
        assertEquals("person", obs5.getTipo());
        assertEquals("5026726351", obs5.getIdentifier());
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

        TrackMatchRequest trackMatchRequest = TrackMatchRequest.newBuilder().setType("person").setId("5026*").build();
        TrackMatchResponse trackMatchResponse = frontend.trackMatch(trackMatchRequest);

        Thread.sleep(3000);

        ReportRequest.Builder reportRequestBuilder2 = ReportRequest.newBuilder();

        ObservationMessage obs3 = ObservationMessage.newBuilder().setCameraName("Alameda").setTipo("person").setIdentifier("5026726351").build();
        reportRequestBuilder2.addObservationMessageList(obs3);

        ReportRequest reportRequest2 = reportRequestBuilder2.build();
        ReportResponse reportResponse2 = frontend.report(reportRequest2);


        TrackMatchRequest trackMatchRequest2 = TrackMatchRequest.newBuilder().setType("person").setId("50*").build();
        TrackMatchResponse trackMatchResponse2 = frontend.trackMatch(trackMatchRequest2);


        ObservationMessage obs4 = trackMatchResponse.getObservationMessageList(0);
        ObservationMessage obs5 = trackMatchResponse2.getObservationMessageList(0);

        Timestamp timestamp1 = obs4.getObsDate();
        Timestamp timestamp2 = obs5.getObsDate();


        assertEquals("Tagus", obs4.getCameraName());
        assertEquals("person", obs4.getTipo());
        assertEquals("5026726351", obs4.getIdentifier());
        assertEquals("Alameda", obs5.getCameraName());
        assertEquals("person", obs5.getTipo());
        assertEquals("5026726351", obs5.getIdentifier());
        assertEquals(-1, Timestamps.compare(timestamp1,timestamp2));

        ClearRequest clearRequest = ClearRequest.getDefaultInstance();
        ClearResponse clearResponse = frontend.ctrlClear(clearRequest);

        frontend.get_channel().shutdownNow();

    }


    @Test
    public void NoPersonOccurrencesTrackMatchTest() throws ZKNamingException {

        SiloFrontend frontend = new SiloFrontend("localhost","2181");

        InitRequest initRequest = InitRequest.newBuilder().setNumberOfReplicas(1).setInitializeReplicaTimestamp(1).build();
        InitResponse initResponse = frontend.ctrlInit(initRequest);

        TrackMatchRequest trackMatchRequest = TrackMatchRequest.newBuilder().setType("person").setId("9234*").build();
        TrackMatchResponse trackMatchResponse = frontend.trackMatch(trackMatchRequest);

        int size = trackMatchResponse.getObservationMessageListList().size();

        assertEquals(0, size);

        ClearRequest clearRequest = ClearRequest.getDefaultInstance();
        ClearResponse clearResponse = frontend.ctrlClear(clearRequest);

        frontend.get_channel().shutdownNow();

    }

    @Test
    public void NoRealTypeOccurrencesTrackMatchTest() throws ZKNamingException {

        SiloFrontend frontend = new SiloFrontend("localhost","2181");

        InitRequest initRequest = InitRequest.newBuilder().setNumberOfReplicas(1).setInitializeReplicaTimestamp(1).build();
        InitResponse initResponse = frontend.ctrlInit(initRequest);

        TrackMatchRequest trackMatchRequest = TrackMatchRequest.newBuilder().setType("type").setId("9234*").build();
        TrackMatchResponse trackMatchResponse = frontend.trackMatch(trackMatchRequest);

        int size = trackMatchResponse.getObservationMessageListList().size();

        assertEquals(0, size);

        ClearRequest clearRequest = ClearRequest.getDefaultInstance();
        ClearResponse clearResponse = frontend.ctrlClear(clearRequest);

        frontend.get_channel().shutdownNow();

    }
}
