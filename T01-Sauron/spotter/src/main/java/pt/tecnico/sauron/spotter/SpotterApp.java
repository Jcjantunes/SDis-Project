package pt.tecnico.sauron.spotter;


import com.google.protobuf.Timestamp;
import pt.tecnico.sauron.silo.client.*;
import pt.tecnico.sauron.silo.grpc.Silo.*;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.*;

public class SpotterApp {

	public static void main(String[] args) throws IOException, ZKNamingException {
		System.out.println(SpotterApp.class.getSimpleName());

		// receive and print arguments
		System.out.printf("Received %d arguments%n", args.length);
		for (int i = 0; i < args.length; i++) {
			System.out.printf("arg[%d] = %s%n", i, args[i]);
		}

		// check arguments
		if (args.length < 2) {
			System.err.println("Argument(s) missing!");
			System.err.printf("Usage: java %s zooHost zooPort%n", SpotterApp.class.getName());
			return;
		}

		final String zooHost = args[0];
		final String zooPort = args[1];

		SiloFrontend frontend = null;

		if (args.length < 3) {

			frontend = new SiloFrontend(zooHost, zooPort);
		}

		else{
			final String instanceNumber =  args[2];
			frontend = new SiloFrontend(zooHost, zooPort, instanceNumber);
		}

		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		String input;
		String type;
		String identifier;

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

		while((input = reader.readLine()) != null) {

			if ((input.length() > 0)) {

				if(input.equals("quit")) {
					break;
				}

				else if (input.equals("clear")) {
					ClearRequest clearRequest = ClearRequest.getDefaultInstance();
					ClearResponse clearResponse = frontend.ctrlClear(clearRequest);

				} else if (input.equals("help")) {
					System.out.println("------------Control Operations------------");
					System.out.println();
					System.out.println("QUIT");
					System.out.println("    Description: Ends the program");
					System.out.println("    How to run: quit");
					System.out.println();
					System.out.println("PING");
					System.out.println("    Description: Check server state");
					System.out.println("    How to run: ping [Input Text]");
					System.out.println();
					System.out.println("CLEAR");
					System.out.println("    Description: Clear server state");
					System.out.println("    How to run: clear");
					System.out.println();
					System.out.println("INIT");
					System.out.println("    Description: Define server configurations");
					System.out.println("    How to run: init (default value of Number of replicas is 9 and of ServerTimestamp Flag is 1)");
					System.out.println("                init [Number of replicas]");
					System.out.println("                init [Number of replicas] [Initialize ServerTimestamp Flag]");
					System.out.println("                init [Number of replicas] [Initialize ServerTimestamp Flag] [Timer]");
					System.out.println();
					System.out.println("------------Search Operations------------");
					System.out.println();
					System.out.println("INFO");
					System.out.println("    Description: Returns the camera coordinates");
					System.out.println("    How to run: info [Camera Name]");
					System.out.println();
					System.out.println("SPOT");
					System.out.println("    Description: Search for an observation of a person or car by their full or partial identifier");
					System.out.println("    (Search for observation with the Full Identifier)");
					System.out.println("        How to run: spot [Observation Type] [Observation Full Identifier]");
					System.out.println("    (Search for observation with the first digits of the Identifier)");
					System.out.println("                    spot [Observation Type] [Observation Partial Identifier]*");
					System.out.println("    (Search for observation with the last digits of the Identifier)");
					System.out.println("                    spot [Observation Type] *[Observation Partial Identifier]");
					System.out.println("    (Search for observation with the first and last digits of the Identifier)");
					System.out.println("                    spot [Observation Type] [Observation Partial Identifier]*[Observation Partial Identifier]");
					System.out.println("TRAIL");
					System.out.println("    Description: Search for the path taken by a person or car with their full");
					System.out.println("        How to run: trail [Observation Type] [Observation Full Identifier]\n");

				} else {
					String[] inputValues = input.split(" ");

					String command = inputValues[0];

					if(command.equals("info")) {
						String cameraName = inputValues[1];

						CamInfoRequest camInfoRequest = CamInfoRequest.newBuilder().setName(cameraName).build();
						CamInfoResponse camInfoResponse = frontend.camInfo(camInfoRequest);

						System.out.printf("*%s* (%f %f)\n", cameraName, camInfoResponse.getLatitude(), camInfoResponse.getLongitude());
					}

					if (command.equals("spot")) {
						//handle spot operation

						type = inputValues[1];
						identifier = inputValues[2];

						if (identifier.contains("*")) {
							TrackMatchRequest trackMatchRequest = TrackMatchRequest.newBuilder().setType(type).setId(identifier).build();
							TrackMatchResponse trackMatchResponse = frontend.trackMatch(trackMatchRequest);

							List<ObservationMessage> obsMessageList = new ArrayList<>();

							int size = trackMatchResponse.getObservationMessageListList().size();

							if (!(size == 0)) {

								int count = 0;

								obsMessageList.addAll(trackMatchResponse.getObservationMessageListList());

								Comparator<ObservationMessage> idComparator = new Comparator<ObservationMessage>() {

									public int compare(ObservationMessage obs1, ObservationMessage obs2) {
										String id1 = obs1.getIdentifier();
										String id2 = obs2.getIdentifier();

										return id1.compareTo(id2);

									}
								};

								Collections.sort(obsMessageList, idComparator);

								for (ObservationMessage obs : obsMessageList) {

									Date date = new Date(obs.getObsDate().getSeconds() * 1000);
									String formattedDate = sdf.format(date);

									String latitude = String.format(Locale.US, "%.6f", obs.getLatitude());
									String longitude = String.format(Locale.US, "%.6f", obs.getLongitude());

									System.out.printf("%s,%s,%s,%s,%s,%s\n",
											obs.getTipo(),
											obs.getIdentifier(),
											formattedDate,
											obs.getCameraName(),
											latitude,
											longitude);

								}
							}
						} else {

							TrackRequest trackRequest = TrackRequest.newBuilder().setType(type).setId(identifier).build();
							TrackResponse trackResponse = frontend.track(trackRequest);

							if (!trackResponse.getCameraName().equals("")) {

								Timestamp timestamp = trackResponse.getObsDate();

								Date date = new Date(timestamp.getSeconds() * 1000);
								String formattedDate = sdf.format(date);

								String latitude = String.format(Locale.US, "%.6f", trackResponse.getLatitude());
								String longitude = String.format(Locale.US, "%.6f", trackResponse.getLongitude());

								System.out.printf("%s,%s,%s,%s,%s,%s\n",
										trackResponse.getTipo(),
										trackResponse.getIdentifier(),
										formattedDate,
										trackResponse.getCameraName(),
										latitude,
										longitude);
							}
						}
					}

					if (command.equals("trail")) {
						//handle trail operation

						type = inputValues[1];
						identifier = inputValues[2];
						TraceRequest traceRequest = TraceRequest.newBuilder().setType(type).setId(identifier).build();

						TraceResponse traceResponse = frontend.trace(traceRequest);

						int size = traceResponse.getObservationMessageListList().size();

						if (!(size == 0)) {
							for (ObservationMessage obs : traceResponse.getObservationMessageListList()) {

								Date date = new Date(obs.getObsDate().getSeconds() * 1000);
								String formattedDate = sdf.format(date);

								String latitude = String.format(Locale.US, "%.6f", obs.getLatitude());
								String longitude = String.format(Locale.US, "%.6f", obs.getLongitude());

								System.out.printf("%s,%s,%s,%s,%s,%s\n",
										obs.getTipo(),
										obs.getIdentifier(),
										formattedDate,
										obs.getCameraName(),
										latitude,
										longitude);

							}

						}
					}

					if (command.equals("init")) {

						int numberOfReplicas = 9;
						int timer = 30;
						int initializeReplicaTimestampFlag = 1;

						if (inputValues.length == 2) {
							numberOfReplicas = Integer.parseInt(inputValues[1]);
						}

						else if (inputValues.length == 3) {
							numberOfReplicas = Integer.parseInt(inputValues[1]);
							initializeReplicaTimestampFlag = Integer.parseInt(inputValues[2]);
						}

						if (inputValues.length > 3) {
							numberOfReplicas = Integer.parseInt(inputValues[1]);
							initializeReplicaTimestampFlag = Integer.parseInt(inputValues[2]);
							timer = Integer.parseInt(inputValues[3]) * 1000;
						}

						InitRequest initRequest = InitRequest.newBuilder().setNumberOfReplicas(numberOfReplicas).setInitializeReplicaTimestamp(initializeReplicaTimestampFlag).setTimer(timer).build();
						InitResponse initResponse = frontend.ctrlInit(initRequest);
						System.out.printf("%s", initResponse.getOutputText());
					}

					if (command.equals("ping")) {
						String text = inputValues[1];

						PingRequest pingRequest = PingRequest.newBuilder().setInputText(text).build();
						PingResponse pingResponse = frontend.ctrlPing(pingRequest);
						System.out.printf("%s", pingResponse.getOutputText());

					}


				}
			}
		}

		frontend.get_channel().shutdownNow();
	}
}
