package pt.tecnico.sauron.silo.client.it;

import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.Test;


import static io.grpc.Status.Code.INVALID_ARGUMENT;
import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.junit.jupiter.api.Assertions.assertThrows;

import pt.tecnico.sauron.silo.client.BaseIT;
import pt.tecnico.sauron.silo.client.SiloFrontend;
import pt.tecnico.sauron.silo.grpc.Silo.*;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;

public class ReportIT extends BaseIT {

    @Test
    public void reportObservationsTest() throws ZKNamingException {

        SiloFrontend frontend = new SiloFrontend("localhost","2181");

        InitRequest initRequest = InitRequest.newBuilder().setNumberOfReplicas(1).setInitializeReplicaTimestamp(1).build();
        InitResponse initResponse = frontend.ctrlInit(initRequest);

        CamJoinRequest camJoinRequest = CamJoinRequest.newBuilder().setName("Tagus").setLatitude(38.737613).setLongitude(-9.303164).build();
        CamJoinResponse camJoinResponse = frontend.camJoin(camJoinRequest);

        ReportRequest.Builder reportRequestBuilder = ReportRequest.newBuilder();

        ObservationMessage obs1 = ObservationMessage.newBuilder().setCameraName("Tagus").setTipo("car").setIdentifier("20SD24").build();
        reportRequestBuilder.addObservationMessageList(obs1);

        ObservationMessage obs2 = ObservationMessage.newBuilder().setCameraName("Tagus").setTipo("person").setIdentifier("123").build();
        reportRequestBuilder.addObservationMessageList(obs2);

        ReportRequest reportRequest = reportRequestBuilder.build();
        ReportResponse reportResponse = frontend.report(reportRequest);

        TraceRequest traceRequest = TraceRequest.newBuilder().setType("person").setId("123").build();
        TraceResponse traceResponse = frontend.trace(traceRequest);

        ObservationMessage obs = traceResponse.getObservationMessageListList().get(0);
        int size = traceResponse.getObservationMessageListList().size();

        assertEquals(1,size);
        assertEquals("Tagus", obs.getCameraName());
        assertEquals("person", obs.getTipo());
        assertEquals("123", obs.getIdentifier());

        ClearRequest clearRequest = ClearRequest.getDefaultInstance();
        ClearResponse clearResponse = frontend.ctrlClear(clearRequest);

        frontend.get_channel().shutdownNow();

    }

    @Test
    public void cameraDoesNotExistTest() throws ZKNamingException {

        SiloFrontend frontend = new SiloFrontend("localhost","2181");

        InitRequest initRequest = InitRequest.newBuilder().setNumberOfReplicas(1).setInitializeReplicaTimestamp(1).build();
        InitResponse initResponse = frontend.ctrlInit(initRequest);

        ObservationMessage obs = ObservationMessage.newBuilder().setCameraName("Tagus").setTipo("car").setIdentifier("20SD24").build();

        ReportRequest.Builder reportRequestBuilder = ReportRequest.newBuilder();

        reportRequestBuilder.addObservationMessageList(obs);

        ReportRequest reportRequest = reportRequestBuilder.build();

        assertEquals(INVALID_ARGUMENT,assertThrows(StatusRuntimeException.class,
                () -> frontend.report(reportRequest)).getStatus().getCode());

        ClearRequest clearRequest = ClearRequest.getDefaultInstance();
        ClearResponse clearResponse = frontend.ctrlClear(clearRequest);

        frontend.get_channel().shutdownNow();
    }

    @Test
    public void incorrectPersonId() throws ZKNamingException {

        SiloFrontend frontend = new SiloFrontend("localhost","2181");

        InitRequest initRequest = InitRequest.newBuilder().setNumberOfReplicas(1).setInitializeReplicaTimestamp(1).build();
        InitResponse initResponse = frontend.ctrlInit(initRequest);

        CamJoinRequest camJoinRequest = CamJoinRequest.newBuilder().setName("Tagus").setLatitude(38.737613).setLongitude(-9.303164).build();
        CamJoinResponse camJoinResponse = frontend.camJoin(camJoinRequest);

        ObservationMessage obs = ObservationMessage.newBuilder().setCameraName("Tagus").setTipo("person").setIdentifier("0").build();

        ReportRequest.Builder reportRequestBuilder = ReportRequest.newBuilder();

        reportRequestBuilder.addObservationMessageList(obs);

        ReportRequest reportRequest = reportRequestBuilder.build();

        assertEquals(INVALID_ARGUMENT,assertThrows(StatusRuntimeException.class,
                () -> frontend.report(reportRequest)).getStatus().getCode());

        ObservationMessage obs2 = ObservationMessage.newBuilder().setCameraName("Tagus").setTipo("person").setIdentifier("9223372036854775808").build();

        ReportRequest.Builder reportRequestBuilder2 = ReportRequest.newBuilder();

        reportRequestBuilder2.addObservationMessageList(obs);

        ReportRequest reportRequest2 = reportRequestBuilder2.build();

        assertEquals(INVALID_ARGUMENT,assertThrows(StatusRuntimeException.class,
                () -> frontend.report(reportRequest)).getStatus().getCode());

        ClearRequest clearRequest = ClearRequest.getDefaultInstance();
        ClearResponse clearResponse = frontend.ctrlClear(clearRequest);

        frontend.get_channel().shutdownNow();
    }

    @Test
    public void incorrectCarPlate() throws ZKNamingException {

        SiloFrontend frontend = new SiloFrontend("localhost","2181");

        InitRequest initRequest = InitRequest.newBuilder().setNumberOfReplicas(1).setInitializeReplicaTimestamp(1).build();
        InitResponse initResponse = frontend.ctrlInit(initRequest);

        CamJoinRequest camJoinRequest = CamJoinRequest.newBuilder().setName("Tagus").setLatitude(38.737613).setLongitude(-9.303164).build();
        CamJoinResponse camJoinResponse = frontend.camJoin(camJoinRequest);

        ObservationMessage obs = ObservationMessage.newBuilder().setCameraName("Tagus").setTipo("car").setIdentifier("000AAB1").build();

        ReportRequest.Builder reportRequestBuilder = ReportRequest.newBuilder();

        reportRequestBuilder.addObservationMessageList(obs);

        ReportRequest reportRequest = reportRequestBuilder.build();



        assertEquals(INVALID_ARGUMENT,assertThrows(StatusRuntimeException.class,
                () -> frontend.report(reportRequest)).getStatus().getCode());

        ObservationMessage obs2 = ObservationMessage.newBuilder().setCameraName("Tagus").setTipo("car").setIdentifier("AA").build();

        ReportRequest.Builder reportRequestBuilder2 = ReportRequest.newBuilder();

        reportRequestBuilder2.addObservationMessageList(obs2);

        ReportRequest reportRequest2 = reportRequestBuilder2.build();

        assertEquals(INVALID_ARGUMENT,assertThrows(StatusRuntimeException.class,
                () -> frontend.report(reportRequest2)).getStatus().getCode());


        ObservationMessage obs3 = ObservationMessage.newBuilder().setCameraName("Tagus").setTipo("car").setIdentifier("AABBCC").build();

        ReportRequest.Builder reportRequestBuilder3 = ReportRequest.newBuilder();

        reportRequestBuilder3.addObservationMessageList(obs3);

        ReportRequest reportRequest3 = reportRequestBuilder3.build();

        assertEquals(INVALID_ARGUMENT,assertThrows(StatusRuntimeException.class,
                () -> frontend.report(reportRequest3)).getStatus().getCode());


        ObservationMessage obs4 = ObservationMessage.newBuilder().setCameraName("Tagus").setTipo("car").setIdentifier("123456").build();

        ReportRequest.Builder reportRequestBuilder4 = ReportRequest.newBuilder();

        reportRequestBuilder4.addObservationMessageList(obs4);

        ReportRequest reportRequest4 = reportRequestBuilder4.build();

        assertEquals(INVALID_ARGUMENT,assertThrows(StatusRuntimeException.class,
                () -> frontend.report(reportRequest4)).getStatus().getCode());



        ClearRequest clearRequest = ClearRequest.getDefaultInstance();
        ClearResponse clearResponse = frontend.ctrlClear(clearRequest);

        frontend.get_channel().shutdownNow();
    }

}
