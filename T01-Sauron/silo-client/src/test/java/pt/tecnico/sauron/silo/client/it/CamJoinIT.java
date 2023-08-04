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

public class CamJoinIT extends BaseIT {


    @Test
    public void addCameraTest() throws ZKNamingException {

        SiloFrontend frontend = new SiloFrontend("localhost","2181");

        InitRequest initRequest = InitRequest.newBuilder().setNumberOfReplicas(1).setInitializeReplicaTimestamp(1).build();
        InitResponse initResponse = frontend.ctrlInit(initRequest);

        CamJoinRequest camJoinRequest = CamJoinRequest.newBuilder().setName("Tagus").setLatitude(38.737613).setLongitude(-9.303164).build();
        CamJoinResponse camJoinResponse = frontend.camJoin(camJoinRequest);

        CamInfoRequest camInfoRequest = CamInfoRequest.newBuilder().setName("Tagus").build();
        CamInfoResponse camInfoResponse = frontend.camInfo(camInfoRequest);

        assertEquals(38.737613, camInfoResponse.getLatitude());
        assertEquals(-9.303164, camInfoResponse.getLongitude());

        ClearRequest clearRequest = ClearRequest.getDefaultInstance();
        ClearResponse clearResponse = frontend.ctrlClear(clearRequest);

        frontend.get_channel().shutdownNow();
    }

    @Test
    public void duplicateCameraTest() throws ZKNamingException {

        SiloFrontend frontend = new SiloFrontend("localhost","2181");

        InitRequest initRequest = InitRequest.newBuilder().setNumberOfReplicas(1).setInitializeReplicaTimestamp(1).build();
        InitResponse initResponse = frontend.ctrlInit(initRequest);

        CamJoinRequest camJoinRequest1 = CamJoinRequest.newBuilder().setName("Tagus").setLatitude(38.737613).setLongitude(-9.303164).build();
        CamJoinResponse camJoinResponse1 = frontend.camJoin(camJoinRequest1);

        CamJoinRequest camJoinRequest2 = CamJoinRequest.newBuilder().setName("Tagus").setLatitude(40.737613).setLongitude(-10.303164).build();

        assertEquals(INVALID_ARGUMENT,assertThrows(StatusRuntimeException.class,
                () -> frontend.camJoin(camJoinRequest2)).getStatus().getCode());

        ClearRequest clearRequest = ClearRequest.getDefaultInstance();
        ClearResponse clearResponse = frontend.ctrlClear(clearRequest);

        frontend.get_channel().shutdownNow();
    }

    @Test
    public void wrongNameLenTest1() throws ZKNamingException {

        SiloFrontend frontend = new SiloFrontend("localhost","2181");

        InitRequest initRequest = InitRequest.newBuilder().setNumberOfReplicas(1).setInitializeReplicaTimestamp(1).build();
        InitResponse initResponse = frontend.ctrlInit(initRequest);

        CamJoinRequest camJoinRequest = CamJoinRequest.newBuilder().setName("Ta").setLatitude(40.737613).setLongitude(-10.303164).build();

        assertEquals(INVALID_ARGUMENT,assertThrows(StatusRuntimeException.class,
                () -> frontend.camJoin(camJoinRequest)).getStatus().getCode());

        ClearRequest clearRequest = ClearRequest.getDefaultInstance();
        ClearResponse clearResponse = frontend.ctrlClear(clearRequest);

        frontend.get_channel().shutdownNow();

    }

    @Test
    public void wrongNameLenTest2() throws ZKNamingException {

        SiloFrontend frontend = new SiloFrontend("localhost","2181");

        InitRequest initRequest = InitRequest.newBuilder().setNumberOfReplicas(1).setInitializeReplicaTimestamp(1).build();
        InitResponse initResponse = frontend.ctrlInit(initRequest);

        CamJoinRequest camJoinRequest = CamJoinRequest.newBuilder().setName("Instituto superior tecnico - TagusPark").setLatitude(40.737613).setLongitude(-10.303164).build();

        assertEquals(INVALID_ARGUMENT,assertThrows(StatusRuntimeException.class,
                () -> frontend.camJoin(camJoinRequest)).getStatus().getCode());

        ClearRequest clearRequest = ClearRequest.getDefaultInstance();
        ClearResponse clearResponse = frontend.ctrlClear(clearRequest);

        frontend.get_channel().shutdownNow();
    }

    @Test
    public void alfanumericNameTest() throws ZKNamingException {

        SiloFrontend frontend = new SiloFrontend("localhost","2181");

        InitRequest initRequest = InitRequest.newBuilder().setNumberOfReplicas(1).setInitializeReplicaTimestamp(1).build();
        InitResponse initResponse = frontend.ctrlInit(initRequest);

        CamJoinRequest camJoinRequest = CamJoinRequest.newBuilder().setName("évora").setLatitude(40.737613).setLongitude(-10.303164).build();

        assertEquals(INVALID_ARGUMENT,assertThrows(StatusRuntimeException.class,
                () -> frontend.camJoin(camJoinRequest)).getStatus().getCode());

        ClearRequest clearRequest = ClearRequest.getDefaultInstance();
        ClearResponse clearResponse = frontend.ctrlClear(clearRequest);

        frontend.get_channel().shutdownNow();
    }

}
