package pt.tecnico.sauron.silo.client.it;

import pt.tecnico.sauron.silo.client.BaseIT;

import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.Test;


import static io.grpc.Status.Code.INVALID_ARGUMENT;
import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.junit.jupiter.api.Assertions.assertThrows;

import pt.tecnico.sauron.silo.client.SiloFrontend;
import pt.tecnico.sauron.silo.grpc.Silo.*;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;

public class CamInfoIT extends BaseIT {

    @Test
    public void checkCoordinatesTest() throws ZKNamingException {

        SiloFrontend frontend = new SiloFrontend("localhost","2181");

        InitRequest initRequest = InitRequest.newBuilder().setNumberOfReplicas(1).setInitializeReplicaTimestamp(1).build();
        InitResponse initResponse = frontend.ctrlInit(initRequest);

        CamJoinRequest camJoinRequest = CamJoinRequest.newBuilder().setName("Tagus").setLatitude(40.737613).setLongitude(-10.303164).build();
        CamJoinResponse camJoinResponse = frontend.camJoin(camJoinRequest);

        CamInfoRequest camInfoRequest = CamInfoRequest.newBuilder().setName("Tagus").build();
        CamInfoResponse camInfoResponse = frontend.camInfo(camInfoRequest);

        assertEquals(40.737613, camInfoResponse.getLatitude());
        assertEquals(-10.303164, camInfoResponse.getLongitude());

        ClearRequest clearRequest = ClearRequest.getDefaultInstance();
        ClearResponse clearResponse = frontend.ctrlClear(clearRequest);

        frontend.get_channel().shutdownNow();
    }

    @Test
    public void emptyNameTest() throws ZKNamingException {

        SiloFrontend frontend = new SiloFrontend("localhost","2181");

        InitRequest initRequest = InitRequest.newBuilder().setNumberOfReplicas(1).build();
        InitResponse initResponse = frontend.ctrlInit(initRequest);

        CamInfoRequest camInfoRequest = CamInfoRequest.newBuilder().setName("").build();

        assertEquals(INVALID_ARGUMENT,assertThrows(StatusRuntimeException.class,
                () -> frontend.camInfo(camInfoRequest)).getStatus().getCode());

        ClearRequest clearRequest = ClearRequest.getDefaultInstance();
        ClearResponse clearResponse = frontend.ctrlClear(clearRequest);

        frontend.get_channel().shutdownNow();
    }

    @Test
    public void CameraDoesNotExistsTest() throws ZKNamingException {

        SiloFrontend frontend = new SiloFrontend("localhost","2181");

        InitRequest initRequest = InitRequest.newBuilder().setNumberOfReplicas(1).build();
        InitResponse initResponse = frontend.ctrlInit(initRequest);

        CamInfoRequest camInfoRequest = CamInfoRequest.newBuilder().setName("Tagus").build();

        assertEquals(INVALID_ARGUMENT,assertThrows(StatusRuntimeException.class,
                () -> frontend.camInfo(camInfoRequest)).getStatus().getCode());

        ClearRequest clearRequest = ClearRequest.getDefaultInstance();
        ClearResponse clearResponse = frontend.ctrlClear(clearRequest);

        frontend.get_channel().shutdownNow();
    }
}
