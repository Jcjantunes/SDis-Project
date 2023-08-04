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

public class PingIT extends BaseIT {

    @Test
    public void pingOKTest() throws ZKNamingException {

        SiloFrontend frontend = new SiloFrontend("localhost","2181");

        InitRequest initRequest = InitRequest.newBuilder().setNumberOfReplicas(1).setInitializeReplicaTimestamp(1).build();
        InitResponse initResponse = frontend.ctrlInit(initRequest);

        PingRequest request = PingRequest.newBuilder().setInputText("friend").build();
        PingResponse response = frontend.ctrlPing(request);
        assertEquals("Hello friend!", response.getOutputText());

        frontend.get_channel().shutdownNow();
    }

    @Test
    public void emptyPingTest() throws ZKNamingException {

        SiloFrontend frontend = new SiloFrontend("localhost","2181");

        InitRequest initRequest = InitRequest.newBuilder().setNumberOfReplicas(1).setInitializeReplicaTimestamp(1).build();
        InitResponse initResponse = frontend.ctrlInit(initRequest);

        PingRequest request = PingRequest.newBuilder().setInputText("").build();

        assertEquals(INVALID_ARGUMENT, assertThrows(StatusRuntimeException.class,
                () -> frontend.ctrlPing(request)).getStatus().getCode());

        frontend.get_channel().shutdownNow();
    }


}
