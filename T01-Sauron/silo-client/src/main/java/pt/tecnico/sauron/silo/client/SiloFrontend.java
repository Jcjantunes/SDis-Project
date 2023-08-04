package pt.tecnico.sauron.silo.client;

import io.grpc.Status;
import pt.tecnico.sauron.silo.grpc.Silo.*;
import pt.tecnico.sauron.silo.grpc.SauronGrpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.ulisboa.tecnico.sdis.zk.ZKNaming;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;
import pt.ulisboa.tecnico.sdis.zk.ZKRecord;

import java.util.*;

public class SiloFrontend {
    private ManagedChannel _channel;
    private SauronGrpc.SauronBlockingStub _blockingStub;
    private List<Integer> frontendTimestamp = new ArrayList<>();
    private HashMap<String,CacheEntry> cache = new HashMap<>();
    int numReplicas;
    int _replicaNumber;

    ZKNaming zkNaming;

    public SiloFrontend(String zooHost, String zooPort) throws ZKNamingException {

        String path = "/grpc/sauron/silo";
        zkNaming = new ZKNaming(zooHost, zooPort);
        List<ZKRecord> recordsList = new ArrayList<>();
        recordsList.addAll(zkNaming.listRecords(path));

        Random random = new Random();
        int index = random.nextInt(recordsList.size());

        ZKRecord record = recordsList.get(index);
        String target = record.getURI();


        _channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
        _blockingStub = SauronGrpc.newBlockingStub(_channel);

        String pathAux = record.getPath();

        _replicaNumber = pathAux.charAt(pathAux.length()-1) - '0';

        System.out.printf("Connected to Replica: %c\n", pathAux.charAt(pathAux.length()-1));
    }

    public SiloFrontend(String zooHost, String zooPort, String instanceNumber) throws ZKNamingException {

        ZKRecord record = null;

        zkNaming = new ZKNaming(zooHost, zooPort);

        String path = "/grpc/sauron/silo/" + instanceNumber;

        try{

            // Channel is the abstraction to connect to a service endpoint.
            // Let us use plaintext communication because we do not have certificates.
            // lookup

            record = zkNaming.lookup(path);

            _replicaNumber = Integer.parseInt(instanceNumber);
            System.out.printf("Connected to Replica: %s\n", instanceNumber);
        }catch(pt.ulisboa.tecnico.sdis.zk.ZKNamingException e){

            System.out.printf("Replica: %s not available\n", instanceNumber);
            System.out.println("Reconnecting to another available replica...");

            path = "/grpc/sauron/silo";
            List<ZKRecord> recordsList = new ArrayList<>();
            recordsList.addAll(zkNaming.listRecords(path));

            Random random = new Random();
            int index = random.nextInt(recordsList.size());

            record = recordsList.get(index);

            String pathAux = record.getPath();

            _replicaNumber = pathAux.charAt(pathAux.length()-1);
            System.out.printf("Connected to Replica: %c\n", pathAux.charAt(pathAux.length()-1));
        }

        String target = record.getURI();

        _channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
        _blockingStub = SauronGrpc.newBlockingStub(_channel);
    }


    public PingResponse ctrlPing(PingRequest request) throws ZKNamingException {
        PingResponse response = null;

        try {
            response = _blockingStub.ctrlPing(request);

        }catch (io.grpc.StatusRuntimeException e) {
            if (e.getStatus().getCode() == Status.Code.UNAVAILABLE) {

                System.out.printf("Replica: %d not available\n", _replicaNumber);
                System.out.println("Reconnecting to another available replica...");

                String path = "/grpc/sauron/silo";

                List<ZKRecord> recordsList = new ArrayList<>();

                recordsList.addAll(zkNaming.listRecords(path));

                Random random = new Random();
                int index = random.nextInt(recordsList.size());

                ZKRecord record = recordsList.get(index);
                String target = record.getURI();

                _channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
                _blockingStub = SauronGrpc.newBlockingStub(_channel);

                String pathAux = record.getPath();
                _replicaNumber = pathAux.charAt(pathAux.length()-1);
                System.out.printf("Connected to Replica: %c\n", pathAux.charAt(pathAux.length()-1));

                GetInitializedFlagRequest getInitializedFlagRequest = GetInitializedFlagRequest.getDefaultInstance();
                GetInitializedFlagResponse getInitializedFlagResponse = _blockingStub.getInitializedFlag(getInitializedFlagRequest);

                if(getInitializedFlagResponse.getIntializedFlag() == 0) {
                    InitRequest initRequest = InitRequest.newBuilder().setNumberOfReplicas(numReplicas).build();
                    InitResponse initResponse = _blockingStub.ctrlInit(initRequest);
                }

                response = _blockingStub.ctrlPing(request);
            }
            else if (e.getStatus().getCode() == Status.Code.INVALID_ARGUMENT){
                throw e;
            }
        }
        return response;
    }

    public ClearResponse ctrlClear(ClearRequest request) throws ZKNamingException {
        ClearResponse response = null;
        try {
            response = _blockingStub.ctrlClear(request);
            InitRequest initRequest = InitRequest.newBuilder().setNumberOfReplicas(numReplicas).build();
            InitResponse initResponse = _blockingStub.ctrlInit(initRequest);
        }catch (io.grpc.StatusRuntimeException e) {
            if(e.getStatus().getCode() == Status.Code.UNAVAILABLE) {

                System.out.printf("Replica: %d not available\n", _replicaNumber);
                System.out.println("Reconnecting to another available replica...");

                String path = "/grpc/sauron/silo";

                List<ZKRecord> recordsList = new ArrayList<>();

                recordsList.addAll(zkNaming.listRecords(path));

                Random random = new Random();
                int index = random.nextInt(recordsList.size());

                ZKRecord record = recordsList.get(index);
                String target = record.getURI();

                _channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
                _blockingStub = SauronGrpc.newBlockingStub(_channel);

                String pathAux = record.getPath();
                _replicaNumber = pathAux.charAt(pathAux.length()-1);
                System.out.printf("Connected to Replica: %c\n", pathAux.charAt(pathAux.length()-1));

                GetInitializedFlagRequest getInitializedFlagRequest = GetInitializedFlagRequest.getDefaultInstance();
                GetInitializedFlagResponse getInitializedFlagResponse = _blockingStub.getInitializedFlag(getInitializedFlagRequest);

                if(getInitializedFlagResponse.getIntializedFlag() == 0) {
                    InitRequest initRequest = InitRequest.newBuilder().setNumberOfReplicas(numReplicas).build();
                    InitResponse initResponse = _blockingStub.ctrlInit(initRequest);
                }

                response = _blockingStub.ctrlClear(request);
                InitRequest initRequest = InitRequest.newBuilder().setNumberOfReplicas(numReplicas).build();
                InitResponse initResponse = _blockingStub.ctrlInit(initRequest);
            }
        }

        return response;
    }

    public InitResponse ctrlInit(InitRequest request) throws ZKNamingException {

        InitResponse initResponse = null;
        try {
            numReplicas = request.getNumberOfReplicas();

            for (int i = 0; i < numReplicas; i++) {
                frontendTimestamp.add(0);
            }

            if(request.getInitializeReplicaTimestamp() == 1) {
                initResponse = _blockingStub.ctrlInit(request);
            }

            else {
                initResponse = InitResponse.newBuilder().setOutputText("Parameters initialized on this server\n").build();
            }

        }catch (io.grpc.StatusRuntimeException e) {
            if (e.getStatus().getCode() == Status.Code.UNAVAILABLE) {

                System.out.printf("Replica: %d not available\n", _replicaNumber);
                System.out.println("Reconnecting to another available replica...");

                String path = "/grpc/sauron/silo";

                List<ZKRecord> recordsList = new ArrayList<>();

                recordsList.addAll(zkNaming.listRecords(path));

                Random random = new Random();
                int index = random.nextInt(recordsList.size());

                ZKRecord record = recordsList.get(index);
                String target = record.getURI();

                _channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
                _blockingStub = SauronGrpc.newBlockingStub(_channel);

                String pathAux = record.getPath();
                _replicaNumber = pathAux.charAt(pathAux.length()-1);
                System.out.printf("Connected to Replica: %c\n", pathAux.charAt(pathAux.length()-1));

                numReplicas = request.getNumberOfReplicas();

                for (int i = 0; i < numReplicas; i++) {
                    frontendTimestamp.add(0);
                }

                if(request.getInitializeReplicaTimestamp() == 1) {
                    initResponse = _blockingStub.ctrlInit(request);
                }

                else {
                    initResponse = InitResponse.newBuilder().setOutputText("Parameters initialized on this server\n").build();
                }

            } else if (e.getStatus().getCode() == Status.Code.INVALID_ARGUMENT) {
                throw e;
            }
        }

        return initResponse;
    }


    public CamJoinResponse camJoin (CamJoinRequest request) throws ZKNamingException {

        List<Integer> newTs = new ArrayList<>();

        UpdateCamJoinRequest.Builder updateCamJoinRequestBuilder = UpdateCamJoinRequest.newBuilder();
        updateCamJoinRequestBuilder
                .setName(request.getName())
                .setLatitude(request.getLatitude())
                .setLongitude(request.getLongitude());

        for (int i = 0; i < frontendTimestamp.size(); i++){
            updateCamJoinRequestBuilder.addTimestamp(frontendTimestamp.get(i));
        }

        UpdateCamJoinRequest updateCamJoinRequest = updateCamJoinRequestBuilder.build();

        CamJoinResponse response = null;

        try {
            newTs.addAll(_blockingStub.updateCamJoin(updateCamJoinRequest).getTimestampList());

            mergeTimestamps(frontendTimestamp, newTs);

            response = _blockingStub.camJoin(request);
        }catch (io.grpc.StatusRuntimeException e) {
            if(e.getStatus().getCode() == Status.Code.UNAVAILABLE) {

                System.out.printf("Replica: %d not available\n", _replicaNumber);
                System.out.println("Reconnecting to another available replica...");

                String path = "/grpc/sauron/silo";

                List<ZKRecord> recordsList = new ArrayList<>();

                recordsList.addAll(zkNaming.listRecords(path));

                Random random = new Random();
                int index = random.nextInt(recordsList.size());

                ZKRecord record = recordsList.get(index);
                String target = record.getURI();

                _channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
                _blockingStub = SauronGrpc.newBlockingStub(_channel);

                String pathAux = record.getPath();
                _replicaNumber = pathAux.charAt(pathAux.length()-1);
                System.out.printf("Connected to Replica: %c\n", pathAux.charAt(pathAux.length()-1));

                GetInitializedFlagRequest getInitializedFlagRequest = GetInitializedFlagRequest.getDefaultInstance();
                GetInitializedFlagResponse getInitializedFlagResponse = _blockingStub.getInitializedFlag(getInitializedFlagRequest);

                if(getInitializedFlagResponse.getIntializedFlag() == 0) {
                    InitRequest initRequest = InitRequest.newBuilder().setNumberOfReplicas(numReplicas).build();
                    InitResponse initResponse = _blockingStub.ctrlInit(initRequest);
                }

                newTs.addAll(_blockingStub.updateCamJoin(updateCamJoinRequest).getTimestampList());

                mergeTimestamps(frontendTimestamp, newTs);

                response = _blockingStub.camJoin(request);
            }

            else if (e.getStatus().getCode() == Status.Code.INVALID_ARGUMENT){
                throw e;
            }
        }

        return response;
    }

    public CamInfoResponse camInfo (CamInfoRequest request) throws ZKNamingException {

        CamInfoResponse response = null;
        try {
            response = _blockingStub.camInfo(request);
        }catch (io.grpc.StatusRuntimeException e) {
            if(e.getStatus().getCode() == Status.Code.UNAVAILABLE) {

                System.out.printf("Replica: %d not available\n", _replicaNumber);
                System.out.println("Reconnecting to another available replica...");

                String path = "/grpc/sauron/silo";

                List<ZKRecord> recordsList = new ArrayList<>();

                recordsList.addAll(zkNaming.listRecords(path));

                Random random = new Random();
                int index = random.nextInt(recordsList.size());

                ZKRecord record = recordsList.get(index);
                String target = record.getURI();

                _channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
                _blockingStub = SauronGrpc.newBlockingStub(_channel);

                GetInitializedFlagRequest getInitializedFlagRequest = GetInitializedFlagRequest.getDefaultInstance();
                GetInitializedFlagResponse getInitializedFlagResponse = _blockingStub.getInitializedFlag(getInitializedFlagRequest);

                if(getInitializedFlagResponse.getIntializedFlag() == 0) {
                    InitRequest initRequest = InitRequest.newBuilder().setNumberOfReplicas(numReplicas).build();
                    InitResponse initResponse = _blockingStub.ctrlInit(initRequest);
                }

                String pathAux = record.getPath();
                _replicaNumber = pathAux.charAt(pathAux.length()-1);
                System.out.printf("Connected to Replica: %c\n", pathAux.charAt(pathAux.length()-1));

                response = _blockingStub.camInfo(request);
            }
            else if (e.getStatus().getCode() == Status.Code.INVALID_ARGUMENT){
                throw e;
            }
        }

        return response;
    }

    public ReportResponse report (ReportRequest request) throws ZKNamingException {

        List<Integer> newTs = new ArrayList<>();

        UpdateReportRequest.Builder updateReportRequestBuilder = UpdateReportRequest.newBuilder();

        ReportResponse reportResponse = null;

        try {
            reportResponse = _blockingStub.report(request);

            for (int i = 0; i < request.getObservationMessageListList().size(); i++) {

                updateReportRequestBuilder.addReportParam(ObservationMessage.newBuilder()
                        .setCameraName(request.getObservationMessageListList().get(i).getCameraName())
                        .setTipo(request.getObservationMessageListList().get(i).getTipo())
                        .setIdentifier(request.getObservationMessageListList().get(i).getIdentifier())
                        .setObsDate(reportResponse.getObsDate()));
            }

            for (int i = 0; i < frontendTimestamp.size(); i++) {
                updateReportRequestBuilder.addTimestamp(frontendTimestamp.get(i));
            }

            UpdateReportRequest updateReportRequest = updateReportRequestBuilder.build();

            newTs.addAll(_blockingStub.updateReport(updateReportRequest).getTimestampList());

            mergeTimestamps(frontendTimestamp, newTs);

        }catch (io.grpc.StatusRuntimeException e) {
            if(e.getStatus().getCode() == Status.Code.UNAVAILABLE) {

                System.out.printf("Replica: %d not available\n", _replicaNumber);
                System.out.println("Reconnecting to another available replica...");

                String path = "/grpc/sauron/silo";

                List<ZKRecord> recordsList = new ArrayList<>();

                recordsList.addAll(zkNaming.listRecords(path));

                Random random = new Random();
                int index = random.nextInt(recordsList.size());

                ZKRecord record = recordsList.get(index);
                String target = record.getURI();

                _channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
                _blockingStub = SauronGrpc.newBlockingStub(_channel);

                GetInitializedFlagRequest getInitializedFlagRequest = GetInitializedFlagRequest.getDefaultInstance();
                GetInitializedFlagResponse getInitializedFlagResponse = _blockingStub.getInitializedFlag(getInitializedFlagRequest);

                if(getInitializedFlagResponse.getIntializedFlag() == 0) {
                    InitRequest initRequest = InitRequest.newBuilder().setNumberOfReplicas(numReplicas).build();
                    InitResponse initResponse = _blockingStub.ctrlInit(initRequest);
                }

                String pathAux = record.getPath();
                _replicaNumber = pathAux.charAt(pathAux.length()-1);
                System.out.printf("Connected to Replica: %c\n", pathAux.charAt(pathAux.length()-1));

                reportResponse = _blockingStub.report(request);

                for (int i = 0; i < request.getObservationMessageListList().size(); i++) {

                    updateReportRequestBuilder.addReportParam(ObservationMessage.newBuilder()
                            .setCameraName(request.getObservationMessageListList().get(i).getCameraName())
                            .setTipo(request.getObservationMessageListList().get(i).getTipo())
                            .setIdentifier(request.getObservationMessageListList().get(i).getIdentifier())
                            .setObsDate(reportResponse.getObsDate()));
                }

                for (int i = 0; i < frontendTimestamp.size(); i++) {
                    updateReportRequestBuilder.addTimestamp(frontendTimestamp.get(i));
                }

                UpdateReportRequest updateReportRequest = updateReportRequestBuilder.build();

                newTs.addAll(_blockingStub.updateReport(updateReportRequest).getTimestampList());

                mergeTimestamps(frontendTimestamp, newTs);
            }
            else if (e.getStatus().getCode() == Status.Code.INVALID_ARGUMENT){
                throw e;
            }
        }


        return reportResponse;
    }

    public TrackResponse track (TrackRequest request) throws ZKNamingException {

        TrackResponse response = null;
        try {
            QueryOperationRequest queryOperationRequest = QueryOperationRequest.getDefaultInstance();

            QueryOperationResponse queryOperationResponse = _blockingStub.queryOperation(queryOperationRequest);

            mergeTimestamps(frontendTimestamp,queryOperationResponse.getTimestampList());

            response = _blockingStub.track(request);

            if(cache.size() == 0 || !response.getCameraName().equals("")) {

                CacheEntry cacheEntry = new CacheEntry(frontendTimestamp, ObservationMessage.newBuilder()
                        .setCameraName(response.getCameraName())
                        .setLatitude(response.getLatitude())
                        .setLongitude(response.getLatitude())
                        .setTipo(response.getTipo())
                        .setIdentifier(response.getIdentifier())
                        .setObsDate(response.getObsDate()).build());

                cache.put(response.getIdentifier(),cacheEntry);
            }

            else if(cache.containsKey(request.getId())) {

                int count = 0;

                for(int i = 0; i < numReplicas; i++) {

                    if(cache.get(request.getId()).get_timestamp().get(i) <= queryOperationResponse.getTimestampList().get(i)) {
                        count++;
                    }
                }

                if(count == numReplicas) {

                    cache.replace(request.getId(), new CacheEntry(frontendTimestamp, ObservationMessage.newBuilder()
                            .setCameraName(response.getCameraName())
                            .setLatitude(response.getLatitude())
                            .setLongitude(response.getLatitude())
                            .setTipo(response.getTipo())
                            .setIdentifier(response.getIdentifier())
                            .setObsDate(response.getObsDate()).build()));
                }

                else {
                    response = TrackResponse.newBuilder()
                            .setCameraName(cache.get(request.getId()).get_observationMessages().get(0).getCameraName())
                            .setLatitude(cache.get(request.getId()).get_observationMessages().get(0).getLatitude())
                            .setLongitude(cache.get(request.getId()).get_observationMessages().get(0).getLongitude())
                            .setTipo(cache.get(request.getId()).get_observationMessages().get(0).getTipo())
                            .setIdentifier(cache.get(request.getId()).get_observationMessages().get(0).getIdentifier())
                            .setObsDate(cache.get(request.getId()).get_observationMessages().get(0).getObsDate()).build();
                }
            }

            else {
                if(!response.getCameraName().equals("")) {
                    CacheEntry newCacheEntry = new CacheEntry(frontendTimestamp, ObservationMessage.newBuilder()
                            .setCameraName(response.getCameraName())
                            .setLatitude(response.getLatitude())
                            .setLongitude(response.getLatitude())
                            .setTipo(response.getTipo())
                            .setIdentifier(response.getIdentifier())
                            .setObsDate(response.getObsDate()).build());

                    cache.put(response.getIdentifier(), newCacheEntry);
                }
            }
        }catch (io.grpc.StatusRuntimeException e) {
            if(e.getStatus().getCode() == Status.Code.UNAVAILABLE) {

                System.out.printf("Replica: %d not available\n", _replicaNumber);
                System.out.println("Reconnecting to another available replica...");

                String path = "/grpc/sauron/silo";

                List<ZKRecord> recordsList = new ArrayList<>();

                recordsList.addAll(zkNaming.listRecords(path));

                Random random = new Random();
                int index = random.nextInt(recordsList.size());

                ZKRecord record = recordsList.get(index);
                String target = record.getURI();

                _channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
                _blockingStub = SauronGrpc.newBlockingStub(_channel);

                String pathAux = record.getPath();
                _replicaNumber = pathAux.charAt(pathAux.length()-1);
                System.out.printf("Connected to Replica: %c\n", pathAux.charAt(pathAux.length()-1));

                GetInitializedFlagRequest getInitializedFlagRequest = GetInitializedFlagRequest.getDefaultInstance();
                GetInitializedFlagResponse getInitializedFlagResponse = _blockingStub.getInitializedFlag(getInitializedFlagRequest);

                if(getInitializedFlagResponse.getIntializedFlag() == 0) {
                    InitRequest initRequest = InitRequest.newBuilder().setNumberOfReplicas(numReplicas).build();
                    InitResponse initResponse = _blockingStub.ctrlInit(initRequest);
                }

                QueryOperationRequest queryOperationRequest = QueryOperationRequest.getDefaultInstance();

                QueryOperationResponse queryOperationResponse = _blockingStub.queryOperation(queryOperationRequest);

                mergeTimestamps(frontendTimestamp, queryOperationResponse.getTimestampList());

                response = _blockingStub.track(request);

                if (cache.size() == 0 || !response.getCameraName().equals("")) {

                    CacheEntry cacheEntry = new CacheEntry(frontendTimestamp, ObservationMessage.newBuilder()
                            .setCameraName(response.getCameraName())
                            .setLatitude(response.getLatitude())
                            .setLongitude(response.getLatitude())
                            .setTipo(response.getTipo())
                            .setIdentifier(response.getIdentifier())
                            .setObsDate(response.getObsDate()).build());

                    cache.put(response.getIdentifier(), cacheEntry);
                } else if (cache.containsKey(request.getId())) {

                    int count = 0;

                    for (int i = 0; i < numReplicas; i++) {

                        if (cache.get(request.getId()).get_timestamp().get(i) <= queryOperationResponse.getTimestampList().get(i)) {
                            count++;
                        }
                    }

                    if (count == numReplicas) {

                        cache.replace(request.getId(), new CacheEntry(frontendTimestamp, ObservationMessage.newBuilder()
                                .setCameraName(response.getCameraName())
                                .setLatitude(response.getLatitude())
                                .setLongitude(response.getLatitude())
                                .setTipo(response.getTipo())
                                .setIdentifier(response.getIdentifier())
                                .setObsDate(response.getObsDate()).build()));
                    } else {
                        response = TrackResponse.newBuilder()
                                .setCameraName(cache.get(request.getId()).get_observationMessages().get(0).getCameraName())
                                .setTipo(cache.get(request.getId()).get_observationMessages().get(0).getTipo())
                                .setLatitude(cache.get(request.getId()).get_observationMessages().get(0).getLatitude())
                                .setLongitude(cache.get(request.getId()).get_observationMessages().get(0).getLongitude())
                                .setIdentifier(cache.get(request.getId()).get_observationMessages().get(0).getIdentifier())
                                .setObsDate(cache.get(request.getId()).get_observationMessages().get(0).getObsDate()).build();
                    }
                } else {
                    if (!response.getCameraName().equals("")) {
                        CacheEntry newCacheEntry = new CacheEntry(frontendTimestamp, ObservationMessage.newBuilder()
                                .setCameraName(response.getCameraName())
                                .setLatitude(response.getLatitude())
                                .setLongitude(response.getLatitude())
                                .setTipo(response.getTipo())
                                .setIdentifier(response.getIdentifier())
                                .setObsDate(response.getObsDate()).build());

                        cache.put(response.getIdentifier(), newCacheEntry);
                    }
                }
            }
        }

        return response;
    }

    public TrackMatchResponse trackMatch (TrackMatchRequest request) throws ZKNamingException {

        TrackMatchResponse response = null;

        try {

            QueryOperationRequest queryOperationRequest = QueryOperationRequest.getDefaultInstance();
            QueryOperationResponse queryOperationResponse = _blockingStub.queryOperation(queryOperationRequest);

            mergeTimestamps(frontendTimestamp, queryOperationResponse.getTimestampList());

            response = _blockingStub.trackMatch(request);
            int obsListSize = response.getObservationMessageListList().size();

            List<ObservationMessage> observationMessageList;

            if (cache.size() == 0 || !(obsListSize == 0)) {

                CacheEntry cacheEntry = new CacheEntry(frontendTimestamp);
                for (int i = 0; i < obsListSize; i++) {
                    ObservationMessage obs = response.getObservationMessageListList().get(i);
                    cacheEntry.set_observationMessages(obs);
                }

                cache.put(request.getId(), cacheEntry);
            }
            else if((observationMessageList = trackMatchAux(request.getType(), request.getId())).size() != 0) {

                TrackMatchResponse.Builder responseBuilder = TrackMatchResponse.newBuilder();
                int count2 = 0;
                for(ObservationMessage observationMessage: observationMessageList) {

                    if (cache.containsKey(observationMessage.getIdentifier())) {
                        int count = 0;
                        count2++;
                        for (int i = 0; i < numReplicas; i++) {

                            if (cache.get(observationMessage.getIdentifier()).get_timestamp().get(i) <= queryOperationResponse.getTimestampList().get(i)) {
                                count++;
                            }
                        }

                        if (count == numReplicas) {

                            CacheEntry cacheEntry = new CacheEntry(frontendTimestamp);
                            for (int i = 0; i < obsListSize; i++) {
                                ObservationMessage obs = response.getObservationMessageListList().get(i);
                                cacheEntry.set_observationMessages(obs);
                            }

                            cache.replace(observationMessage.getIdentifier(), cacheEntry);
                        } else {

                            responseBuilder.addObservationMessageList(cache
                                    .get(observationMessage.getIdentifier())
                                    .get_observationMessages().get(0));
                        }
                    }
                }
                if(count2 == observationMessageList.size())
                    response = responseBuilder.build();
            }

            else {
                if (obsListSize != 0) {
                    CacheEntry cacheEntry = new CacheEntry(frontendTimestamp);
                    for (int i = 0; i < obsListSize; i++) {
                        ObservationMessage obs = response.getObservationMessageListList().get(i);
                        cacheEntry.set_observationMessages(obs);
                    }

                    cache.put(request.getId(), cacheEntry);
                }
            }
        }catch (io.grpc.StatusRuntimeException e) {
            if (e.getStatus().getCode() == Status.Code.UNAVAILABLE) {

                System.out.printf("Replica: %d not available\n", _replicaNumber);
                System.out.println("Reconnecting to another available replica...");

                String path = "/grpc/sauron/silo";

                List<ZKRecord> recordsList = new ArrayList<>();

                recordsList.addAll(zkNaming.listRecords(path));

                Random random = new Random();
                int index = random.nextInt(recordsList.size());

                ZKRecord record = recordsList.get(index);
                String target = record.getURI();

                _channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
                _blockingStub = SauronGrpc.newBlockingStub(_channel);

                GetInitializedFlagRequest getInitializedFlagRequest = GetInitializedFlagRequest.getDefaultInstance();
                GetInitializedFlagResponse getInitializedFlagResponse = _blockingStub.getInitializedFlag(getInitializedFlagRequest);

                if(getInitializedFlagResponse.getIntializedFlag() == 0) {
                    InitRequest initRequest = InitRequest.newBuilder().setNumberOfReplicas(numReplicas).build();
                    InitResponse initResponse = _blockingStub.ctrlInit(initRequest);
                }

                QueryOperationRequest queryOperationRequest = QueryOperationRequest.getDefaultInstance();
                QueryOperationResponse queryOperationResponse = _blockingStub.queryOperation(queryOperationRequest);

                mergeTimestamps(frontendTimestamp, queryOperationResponse.getTimestampList());

                response = _blockingStub.trackMatch(request);

                String pathAux = record.getPath();
                _replicaNumber = pathAux.charAt(pathAux.length()-1);
                System.out.printf("Connected to Replica: %c\n", pathAux.charAt(pathAux.length()-1));

                int obsListSize = response.getObservationMessageListList().size();

                List<ObservationMessage> observationMessageList;

                if (cache.size() == 0 || !(obsListSize == 0)) {

                    CacheEntry cacheEntry = new CacheEntry(frontendTimestamp);
                    for (int i = 0; i < obsListSize; i++) {
                        ObservationMessage obs = response.getObservationMessageListList().get(i);
                        cacheEntry.set_observationMessages(obs);
                    }

                    cache.put(request.getId(), cacheEntry);
                }
                else if((observationMessageList = trackMatchAux(request.getType(), request.getId())).size() != 0) {

                    TrackMatchResponse.Builder responseBuilder = TrackMatchResponse.newBuilder();
                    int count2 = 0;
                    for(ObservationMessage observationMessage: observationMessageList) {
                        if (cache.containsKey(observationMessage.getIdentifier())) {

                            count2++;
                            int count = 0;

                            for (int i = 0; i < numReplicas; i++) {

                                if (cache.get(observationMessage.getIdentifier()).get_timestamp().get(i) <= queryOperationResponse.getTimestampList().get(i)) {
                                    count++;
                                }
                            }

                            if (count == numReplicas) {

                                CacheEntry cacheEntry = new CacheEntry(frontendTimestamp);
                                for (int i = 0; i < obsListSize; i++) {
                                    ObservationMessage obs = response.getObservationMessageListList().get(i);
                                    cacheEntry.set_observationMessages(obs);
                                }

                                cache.replace(observationMessage.getIdentifier(), cacheEntry);
                            } else {
                                responseBuilder.addObservationMessageList(cache
                                        .get(observationMessage.getIdentifier())
                                        .get_observationMessages().get(0));
                            }
                        }
                    }

                    if(count2 == observationMessageList.size())
                        response = responseBuilder.build();
                }

                else {
                    if (obsListSize != 0) {
                        CacheEntry cacheEntry = new CacheEntry(frontendTimestamp);
                        for (int i = 0; i < obsListSize; i++) {
                            ObservationMessage obs = response.getObservationMessageListList().get(i);
                            cacheEntry.set_observationMessages(obs);
                        }

                        cache.put(request.getId(), cacheEntry);
                    }
                }

            }
        }

        return response;
    }

    public TraceResponse trace (TraceRequest request) throws ZKNamingException {

        TraceResponse response = null;
        try {
            QueryOperationRequest queryOperationRequest = QueryOperationRequest.getDefaultInstance();
            QueryOperationResponse queryOperationResponse = _blockingStub.queryOperation(queryOperationRequest);

            mergeTimestamps(frontendTimestamp, queryOperationResponse.getTimestampList());

            response = _blockingStub.trace(request);

            int obsListSize = response.getObservationMessageListList().size();

            if (cache.size() == 0 || !(obsListSize == 0)) {

                CacheEntry cacheEntry = new CacheEntry(frontendTimestamp);
                for (int i = 0; i < obsListSize; i++) {
                    ObservationMessage obs = response.getObservationMessageListList().get(i);
                    cacheEntry.set_observationMessages(obs);
                }

                cache.put(request.getId(), cacheEntry);

            } else if (cache.containsKey(request.getId())) {

                int count = 0;

                for (int i = 0; i < numReplicas; i++) {

                    if (cache.get(request.getId()).get_timestamp().get(i) <= queryOperationResponse.getTimestampList().get(i)) {
                        count++;
                    }
                }

                if (count == numReplicas) {

                    CacheEntry cacheEntry = new CacheEntry(frontendTimestamp);
                    for (int i = 0; i < obsListSize; i++) {
                        ObservationMessage obs = response.getObservationMessageListList().get(i);
                        cacheEntry.set_observationMessages(obs);
                    }

                    cache.replace(request.getId(), cacheEntry);

                } else {

                    TraceResponse.Builder responseBuilder = TraceResponse.newBuilder();
                    for (ObservationMessage obs : cache.get(request.getId()).get_observationMessages()) {
                        responseBuilder.addObservationMessageList(obs);
                    }
                    response = responseBuilder.build();
                }

            } else {
                if (obsListSize != 0) {
                    CacheEntry cacheEntry = new CacheEntry(frontendTimestamp);
                    for (int i = 0; i < obsListSize; i++) {
                        ObservationMessage obs = response.getObservationMessageListList().get(i);
                        cacheEntry.set_observationMessages(obs);
                    }

                    cache.put(request.getId(), cacheEntry);
                }
            }
        }catch ( io.grpc.StatusRuntimeException e) {
            if (e.getStatus().getCode() == Status.Code.UNAVAILABLE ) {

                System.out.printf("Replica: %d not available\n", _replicaNumber);
                System.out.println("Reconnecting to another available replica...");

                String path = "/grpc/sauron/silo";

                List<ZKRecord> recordsList = new ArrayList<>();

                recordsList.addAll(zkNaming.listRecords(path));

                Random random = new Random();
                int index = random.nextInt(recordsList.size());

                ZKRecord record = recordsList.get(index);
                String target = record.getURI();

                _channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
                _blockingStub = SauronGrpc.newBlockingStub(_channel);

                GetInitializedFlagRequest getInitializedFlagRequest = GetInitializedFlagRequest.getDefaultInstance();
                GetInitializedFlagResponse getInitializedFlagResponse = _blockingStub.getInitializedFlag(getInitializedFlagRequest);

                if(getInitializedFlagResponse.getIntializedFlag() == 0) {
                    InitRequest initRequest = InitRequest.newBuilder().setNumberOfReplicas(numReplicas).build();
                    InitResponse initResponse = _blockingStub.ctrlInit(initRequest);
                }

                QueryOperationRequest queryOperationRequest = QueryOperationRequest.getDefaultInstance();
                QueryOperationResponse queryOperationResponse = _blockingStub.queryOperation(queryOperationRequest);

                mergeTimestamps(frontendTimestamp, queryOperationResponse.getTimestampList());

                response = _blockingStub.trace(request);

                String pathAux = record.getPath();
                _replicaNumber = pathAux.charAt(pathAux.length()-1);
                System.out.printf("Connected to Replica: %c\n", pathAux.charAt(pathAux.length()-1));

                int obsListSize = response.getObservationMessageListList().size();

                if (cache.size() == 0 || !(obsListSize == 0)) {

                    CacheEntry cacheEntry = new CacheEntry(frontendTimestamp);
                    for (int i = 0; i < obsListSize; i++) {
                        ObservationMessage obs = response.getObservationMessageListList().get(i);
                        cacheEntry.set_observationMessages(obs);
                    }

                    cache.put(request.getId(), cacheEntry);

                } else if (cache.containsKey(request.getId())) {

                    int count = 0;

                    for (int i = 0; i < numReplicas; i++) {

                        if (cache.get(request.getId()).get_timestamp().get(i) <= queryOperationResponse.getTimestampList().get(i)) {
                            count++;
                        }
                    }

                    if (count == numReplicas) {

                        CacheEntry cacheEntry = new CacheEntry(frontendTimestamp);
                        for (int i = 0; i < obsListSize; i++) {
                            ObservationMessage obs = response.getObservationMessageListList().get(i);
                            cacheEntry.set_observationMessages(obs);
                        }

                        cache.replace(request.getId(), cacheEntry);

                    } else {

                        TraceResponse.Builder responseBuilder = TraceResponse.newBuilder();
                        for (ObservationMessage obs : cache.get(request.getId()).get_observationMessages()) {
                            responseBuilder.addObservationMessageList(obs);
                        }
                        response = responseBuilder.build();
                    }

                } else {
                    if (obsListSize != 0) {
                        CacheEntry cacheEntry = new CacheEntry(frontendTimestamp);
                        for (int i = 0; i < obsListSize; i++) {
                            ObservationMessage obs = response.getObservationMessageListList().get(i);
                            cacheEntry.set_observationMessages(obs);
                        }

                        cache.put(request.getId(), cacheEntry);
                    }
                }
            }
        }

        return response;
    }

    public GetNumReplicasResponse getNumReplicas(GetNumReplicasRequest request) throws ZKNamingException {

        GetNumReplicasResponse getNumReplicasResponse = null;

        try {
            getNumReplicasResponse = _blockingStub.getNumReplicas(request);
            numReplicas = getNumReplicasResponse.getNumReplicas();
        }catch ( io.grpc.StatusRuntimeException e) {
            if (e.getStatus().getCode() == Status.Code.UNAVAILABLE) {

                System.out.printf("Replica: %d not available\n", _replicaNumber);
                System.out.println("Reconnecting to another available replica...");

                String path = "/grpc/sauron/silo";

                List<ZKRecord> recordsList = new ArrayList<>();

                recordsList.addAll(zkNaming.listRecords(path));

                Random random = new Random();
                int index = random.nextInt(recordsList.size());

                ZKRecord record = recordsList.get(index);
                String target = record.getURI();

                _channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
                _blockingStub = SauronGrpc.newBlockingStub(_channel);

                String pathAux = record.getPath();
                _replicaNumber = pathAux.charAt(pathAux.length()-1);
                System.out.printf("Connected to Replica: %c\n", pathAux.charAt(pathAux.length()-1));

                GetInitializedFlagRequest getInitializedFlagRequest = GetInitializedFlagRequest.getDefaultInstance();
                GetInitializedFlagResponse getInitializedFlagResponse = _blockingStub.getInitializedFlag(getInitializedFlagRequest);

                if(getInitializedFlagResponse.getIntializedFlag() == 0) {
                    InitRequest initRequest = InitRequest.newBuilder().setNumberOfReplicas(numReplicas).build();
                    InitResponse initResponse = _blockingStub.ctrlInit(initRequest);
                }

                getNumReplicasResponse = _blockingStub.getNumReplicas(request);
                numReplicas = getNumReplicasResponse.getNumReplicas();
            }
        }

        return getNumReplicasResponse;
    }

    public ManagedChannel get_channel() {

        return _channel;
    }

    public void mergeTimestamps (List<Integer> prevTs, List<Integer> newTs){

        for( int i = 0; i < prevTs.size(); i++ ){

            //System.out.println("i: " + i);
            if(newTs.get(i) > prevTs.get(i) ){
                prevTs.set(i, newTs.get(i));
            }
        }
    }

    public void initializeFrontEndTimestamp() {
        for(int i = 0; i < numReplicas; i++){

            frontendTimestamp.add(0);
        }
    }

    public List<ObservationMessage> trackMatchAux(String type, String partialIdentifier) {
        List<ObservationMessage> obsList = new ArrayList<>();

        for (Map.Entry<String, CacheEntry> entry : cache.entrySet()) {
            CacheEntry cacheEntry = entry.getValue();

            Collections.sort(cacheEntry.get_observationMessages(), new CacheEntryObservationMessageCompare());
        }

        HashMap<String,ObservationMessage> obsHashMap= new HashMap<>();

        int len = partialIdentifier.length();

        if(partialIdentifier.charAt(0) == ('*')) {
            String idEnd = partialIdentifier.substring(1, len);

            for (Map.Entry<String, CacheEntry> entry : cache.entrySet()) {
                CacheEntry cacheEntry = entry.getValue();

                int firstOccurrenceLen;

                if (cacheEntry.get_observationMessages().get(0).getTipo().equals(type)) {
                    String fullId = cacheEntry.get_observationMessages().get(0).getIdentifier();

                    firstOccurrenceLen = fullId.lastIndexOf(idEnd);

                    if (firstOccurrenceLen == fullId.length() - idEnd.length()) {
                        if (!obsHashMap.containsKey(fullId)) {
                            obsHashMap.put(fullId, cacheEntry.get_observationMessages().get(0));
                        }
                    }
                }
            }
        }

        else if(partialIdentifier.charAt(len-1) == ('*')) {
            String idBeginning = partialIdentifier.substring(0, len-1);

            for (Map.Entry<String, CacheEntry> entry : cache.entrySet()) {
                CacheEntry cacheEntry = entry.getValue();

                int firstOccurrenceLen;


                if (cacheEntry.get_observationMessages().get(0).getTipo().equals(type)) {
                    String fullId = cacheEntry.get_observationMessages().get(0).getIdentifier();

                    firstOccurrenceLen = fullId.indexOf(idBeginning);

                    if (firstOccurrenceLen == 0) {
                        if (!obsHashMap.containsKey(fullId)) {
                            obsHashMap.put(fullId, cacheEntry.get_observationMessages().get(0));
                        }
                    }
                }

            }
        }

        else {

            String[] inputValues = partialIdentifier.split("\\*");
            String idBeginning = inputValues[0];
            String idEnd = inputValues[1];

            for (Map.Entry<String, CacheEntry> entry : cache.entrySet()) {
                CacheEntry cacheEntry = entry.getValue();

                int firstOccurrenceLen1;
                int firstOccurrenceLen2;

                if (cacheEntry.get_observationMessages().get(0).getTipo().equals(type)) {

                    String fullId = cacheEntry.get_observationMessages().get(0).getIdentifier();

                    firstOccurrenceLen1 = fullId.indexOf(idBeginning);

                    if (firstOccurrenceLen1 == 0) {
                        firstOccurrenceLen2 = fullId.lastIndexOf(idEnd);

                        if (firstOccurrenceLen2 == fullId.length() - idEnd.length()) {

                            if (!obsHashMap.containsKey(fullId)) {
                                obsHashMap.put(fullId, cacheEntry.get_observationMessages().get(0));
                            }
                        }
                    }
                }
            }
        }

        for (Map.Entry<String, ObservationMessage> entry : obsHashMap.entrySet()) {
            obsList.add(entry.getValue());
        }

        return obsList;
    }
}
