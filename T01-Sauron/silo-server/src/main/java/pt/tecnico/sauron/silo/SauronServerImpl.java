package pt.tecnico.sauron.silo;

import com.google.protobuf.Timestamp;
import io.grpc.stub.StreamObserver;
import pt.tecnico.sauron.silo.domain.*;
import pt.tecnico.sauron.silo.grpc.*;
import pt.tecnico.sauron.silo.grpc.Silo.*;

import java.util.ArrayList;
import java.util.List;

import static io.grpc.Status.INVALID_ARGUMENT;

public class SauronServerImpl extends SauronGrpc.SauronImplBase{


    /** Server implementation. */
    private SiloServer silo = new SiloServer();

    private List<Integer> vectorialTimestamp = new ArrayList<>();
    private int timer;
    private List<LogRecord> logRecordList = new ArrayList<>();
    private int initializedFlag = 0;

    int instanceNumber;

    public SauronServerImpl(int replicaNumber){
        instanceNumber = replicaNumber;

    }


    @Override
    public void ctrlPing(PingRequest request, StreamObserver<PingResponse> responseObserver) {

        String input = request.getInputText();
        String output = "Hello " + input + "!";

        if (input == null || input.isBlank()) {
            responseObserver.onError(INVALID_ARGUMENT.withDescription("Input cannot be empty").asRuntimeException());
        }

        PingResponse response = PingResponse.newBuilder().setOutputText(output).build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void ctrlClear(ClearRequest request, StreamObserver<ClearResponse> responseObserver) {

        silo.clear();

        for (int i = 0; i < vectorialTimestamp.size(); i++) {
            vectorialTimestamp.set(i,0);
        }
        ClearResponse response = ClearResponse.getDefaultInstance();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void ctrlInit(InitRequest request, StreamObserver<InitResponse> responseObserver) {

        initializedFlag = 1;

        for (int i = 0; i < request.getNumberOfReplicas(); i++) {
            vectorialTimestamp.add(0);
        }

        timer = request.getTimer();

        String output = "Parameters initialized on this server\n";
        InitResponse response = InitResponse.newBuilder().setOutputText(output).build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }




    @Override
    public void camJoin(CamJoinRequest request, StreamObserver<CamJoinResponse> responseObserver) {

        try {
            silo.cam_join(request.getName(), request.getLatitude(), request.getLongitude());
        } catch (IncorrectArgumentException e) {
            responseObserver.onError(INVALID_ARGUMENT.withDescription("Incorrect argument").asRuntimeException());
        } catch (DuplicateCameraNameException e) {
            responseObserver.onError(INVALID_ARGUMENT.withDescription("Camera already exists").asRuntimeException());
        }

        CamJoinResponse response = CamJoinResponse.getDefaultInstance();

        // Send a single response through the stream.
        responseObserver.onNext(response);
        // Notify the client that the operation has been completed.
        responseObserver.onCompleted();
    }

    @Override
    public void camInfo(CamInfoRequest request, StreamObserver<CamInfoResponse> responseObserver) {


        CamInfoResponse response = null;
        try {
            Coordinates retorno = silo.cam_info(request.getName());
            response = CamInfoResponse.newBuilder().setLatitude(retorno.get_latitude()).setLongitude(retorno.get_longitude()).build();
        } catch (IncorrectArgumentException e) {
            responseObserver.onError(INVALID_ARGUMENT.withDescription("Camera name cannot be empty").asRuntimeException());
        }catch (CameraDoesNotExistException e) {
            responseObserver.onError(INVALID_ARGUMENT.withDescription("Camera does not exists").asRuntimeException());
        }

        // Send a single response through the stream.
        responseObserver.onNext(response);
        // Notify the client that the operation has been completed.
        responseObserver.onCompleted();
    }

    @Override
    public void report(ReportRequest request, StreamObserver<ReportResponse> responseObserver) {

        Timestamp currentTime = Timestamp.newBuilder().setSeconds((System.currentTimeMillis() / 1000)).build();
        List<ObservationMessage> observationMessageList = request.getObservationMessageListList();

        try {
            silo.report(observationMessageList,currentTime);
        } catch (IncorrectArgumentException e) {
            responseObserver.onError(INVALID_ARGUMENT.withDescription("Incorrect argument").asRuntimeException());
        } catch (CameraDoesNotExistException e) {
            responseObserver.onError(INVALID_ARGUMENT.withDescription("Camera does not exists").asRuntimeException());
        }

        ReportResponse response = ReportResponse.newBuilder().setObsDate(currentTime).build();
        // Send a single response through the stream.
        responseObserver.onNext(response);
        // Notify the client that the operation has been completed.
        responseObserver.onCompleted();
    }

    @Override
    public void track(TrackRequest request, StreamObserver<TrackResponse> responseObserver) {

        TrackResponse response;

        Observation retorno = silo.track(request.getType(),request.getId());

        if(retorno == null) {
            response = TrackResponse.newBuilder().setCameraName("").build();
        }

        else {
            response = TrackResponse.newBuilder().setCameraName(retorno.get_cameraName()).setLatitude(retorno.get_latitude()).setLongitude(retorno.get_longitude()).setTipo(retorno.get_type().get_typeName())
                    .setIdentifier(retorno.get_type().get_identifier()).setObsDate(retorno.get_observationDate()).build();
        }

        // Send a single response through the stream.
        responseObserver.onNext(response);
        // Notify the client that the operation has been completed.
        responseObserver.onCompleted();

    }

    @Override
    public void trackMatch(TrackMatchRequest request, StreamObserver<TrackMatchResponse> responseObserver) {
        TrackMatchResponse.Builder responseBuilder = TrackMatchResponse.newBuilder();

        List<Observation> retorno = silo.trackMatch(request.getType(),request.getId());

        for(Observation obs: retorno) {
            responseBuilder.addObservationMessageList(ObservationMessage.newBuilder().setCameraName(obs.get_cameraName())
                    .setLatitude(obs.get_latitude())
                    .setLongitude(obs.get_longitude())
                    .setTipo(obs.get_type().get_typeName())
                    .setIdentifier(obs.get_type().get_identifier())
                    .setObsDate(obs.get_observationDate()).build());
        }

        // Send a single response through the stream.
        responseObserver.onNext(responseBuilder.build());
        // Notify the client that the operation has been completed.
        responseObserver.onCompleted();
    }

    @Override
    public void trace(TraceRequest request, StreamObserver<TraceResponse> responseObserver) {

        TraceResponse.Builder responseBuilder = TraceResponse.newBuilder();

        List<Observation> retorno = silo.trace(request.getType(),request.getId());

        for(Observation obs: retorno) {
            responseBuilder.addObservationMessageList(ObservationMessage.newBuilder().setCameraName(obs.get_cameraName())
                    .setLatitude(obs.get_latitude())
                    .setLongitude(obs.get_longitude())
                    .setTipo(obs.get_type().get_typeName())
                    .setIdentifier(obs.get_type().get_identifier())
                    .setObsDate(obs.get_observationDate()).build());
        }

        // Send a single response through the stream.
        responseObserver.onNext(responseBuilder.build());
        // Notify the client that the operation has been completed.
        responseObserver.onCompleted();

    }

    @Override
    public void queryOperation(QueryOperationRequest request, StreamObserver<QueryOperationResponse> responseObserver ){

        QueryOperationResponse.Builder queryOperationResponseBuilder = QueryOperationResponse.newBuilder();

        for(Integer i: vectorialTimestamp) {
            queryOperationResponseBuilder.addTimestamp(i);
        }

        // Send a single response through the stream.
        responseObserver.onNext(queryOperationResponseBuilder.build());
        // Notify the client that the operation has been completed.
        responseObserver.onCompleted();

    }

    @Override
    public void updateCamJoin(UpdateCamJoinRequest request, StreamObserver<UpdateCamJoinResponse> responseObserver ){

        int value = vectorialTimestamp.get(instanceNumber-1);
        value++;
        vectorialTimestamp.set(instanceNumber-1,value);

        List<Integer> prev = request.getTimestampList();
        List<Integer> ts = new ArrayList<>();
        ts.addAll(prev);

        ts.set(instanceNumber-1,vectorialTimestamp.get(instanceNumber-1));

        CamJoinLogRecord logRecord = new CamJoinLogRecord(instanceNumber,ts, prev, request.getName(), request.getLatitude(),
                request.getLongitude(), value);
        logRecordList.add(logRecord);

        UpdateCamJoinResponse.Builder responseBuilder = UpdateCamJoinResponse.newBuilder();

        for (Integer i: ts) {
            responseBuilder.addTimestamp(i);
        }

        // Send a single response through the stream.
        responseObserver.onNext(responseBuilder.build());
        // Notify the client that the operation has been completed.
        responseObserver.onCompleted();

    }

    @Override
    public void updateReport(UpdateReportRequest request, StreamObserver<UpdateReportResponse> responseObserver ){

        int value = vectorialTimestamp.get(instanceNumber-1);
        value++;
        vectorialTimestamp.set(instanceNumber-1,value);

        List<Integer> prev = request.getTimestampList();
        List<Integer> ts = new ArrayList<>();
        ts.addAll(prev);
        ts.set(instanceNumber-1,vectorialTimestamp.get(instanceNumber-1));

        ReportLogRecord logRecord = new ReportLogRecord(instanceNumber,ts, prev, request.getReportParamList(), value);
        logRecordList.add(logRecord);

        UpdateReportResponse.Builder responseBuilder = UpdateReportResponse.newBuilder();

        for (Integer i: ts) {
            responseBuilder.addTimestamp(i);
        }

        // Send a single response through the stream.
        responseObserver.onNext(responseBuilder.build());
        // Notify the client that the operation has been completed.
        responseObserver.onCompleted();

    }

    @Override
    public void getNumReplicas(GetNumReplicasRequest request, StreamObserver<GetNumReplicasResponse> responseObserver){

        GetNumReplicasResponse response = GetNumReplicasResponse.newBuilder().setNumReplicas(vectorialTimestamp.size()).build();

        // Send a single response through the stream.
        responseObserver.onNext(response);
        // Notify the client that the operation has been completed.
        responseObserver.onCompleted();

    }

    @Override
    public void getInitializedFlag(GetInitializedFlagRequest request, StreamObserver<GetInitializedFlagResponse> responseObserver){

        GetInitializedFlagResponse response = GetInitializedFlagResponse.newBuilder().setIntializedFlag(initializedFlag).build();

        // Send a single response through the stream.
        responseObserver.onNext(response);
        // Notify the client that the operation has been completed.
        responseObserver.onCompleted();

    }

}
