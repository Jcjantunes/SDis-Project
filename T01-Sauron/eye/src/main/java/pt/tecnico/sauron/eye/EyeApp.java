package pt.tecnico.sauron.eye;

import pt.tecnico.sauron.silo.client.*;
import pt.tecnico.sauron.silo.grpc.Silo.*;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class EyeApp {

	public static void main(String[] args) throws IOException, InterruptedException, ZKNamingException {
		System.out.println(EyeApp.class.getSimpleName());

		// receive and print arguments
		System.out.printf("Received %d arguments%n", args.length);
		for (int i = 0; i < args.length; i++) {
			System.out.printf("arg[%d] = %s%n", i, args[i]);
		}


		// check arguments
		if (args.length < 5) {
			System.err.println("Argument(s) missing!");
			System.err.printf("Usage: java %s zooHost zooPort%n", EyeApp.class.getName());
			return;
		}

		final String zooHost = args[0];
		final String zooPort = args[1];

		SiloFrontend frontend;

		if (args.length < 6) {

			frontend = new SiloFrontend(zooHost, zooPort);
		}

		else{
			final String instanceNumber =  args[5];
			frontend = new SiloFrontend(zooHost, zooPort, instanceNumber);
		}

		GetNumReplicasRequest getNumReplicasRequest = GetNumReplicasRequest.getDefaultInstance();
		GetNumReplicasResponse getNumReplicasResponse = frontend.getNumReplicas(getNumReplicasRequest);

		frontend.initializeFrontEndTimestamp();

		CamJoinRequest camJoinRequest = CamJoinRequest.newBuilder().setName(args[2]).setLatitude(Double.parseDouble(args[3])).setLongitude(Double.parseDouble(args[4])).build();
		CamJoinResponse camJoinResponse = frontend.camJoin(camJoinRequest);


		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		String input;
		ReportRequest.Builder requestBuilder = ReportRequest.newBuilder();

		while(true) {

			if((input = reader.readLine()) == null) {

				if(requestBuilder.build().getObservationMessageListList().size() > 0) {
					ReportResponse response = frontend.report(requestBuilder.build());
					requestBuilder.clearObservationMessageList();
				}

				break;
			}

			if((input.length() > 0) && (input.charAt(0) != '#')) {

				String[] inputValues = input.split(",");
				String first_half = inputValues[0];
				String second_half = inputValues[1];

				if(first_half.equals("zzz")) {
					Thread.sleep(Integer.parseInt(second_half));
				}

				else {
					ObservationMessage obs = ObservationMessage.newBuilder()
							.setCameraName(args[2])
							.setLatitude(Double.parseDouble(args[3]))
							.setLongitude(Double.parseDouble(args[4]))
							.setTipo(first_half)
							.setIdentifier(second_half).build();

					requestBuilder.addObservationMessageList(obs);
				}
			}

			else if(input.length() == 0){
				if(requestBuilder.build().getObservationMessageListList().size() > 0) {
					ReportResponse response = frontend.report(requestBuilder.build());
					requestBuilder.clearObservationMessageList();

				}
			}
		}

		frontend.get_channel().shutdownNow();
	}

}
