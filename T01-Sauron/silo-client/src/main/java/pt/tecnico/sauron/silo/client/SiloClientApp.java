package pt.tecnico.sauron.silo.client;

import pt.tecnico.sauron.silo.grpc.Silo.*;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;

public class SiloClientApp {

	public static void main(String[] args) throws ZKNamingException {
		System.out.println(SiloClientApp.class.getSimpleName());

		// receive and print arguments
		System.out.printf("Received %d arguments%n", args.length);
		for (int i = 0; i < args.length; i++) {
			System.out.printf("arg[%d] = %s%n", i, args[i]);
		}

		// check arguments
		if (args.length < 2) {
			System.err.println("Argument(s) missing!");
			System.err.printf("Usage: java %s zooHost zooPort%n", SiloClientApp.class.getName());
			return;
		}

		final String zooHost = args[0];
		final String zooPort = args[1];

		SiloFrontend frontend;

		if (args.length < 3) {

			frontend = new SiloFrontend(zooHost, zooPort);
		}

		else{
			final String instanceNumber =  args[2];
			final String path = "/grpc/sauron/silo/" + instanceNumber;
			frontend = new SiloFrontend(zooHost, zooPort, path);
		}


		CamJoinRequest request1 = CamJoinRequest.newBuilder().setName("Tagus").setLatitude(38.737613).setLongitude(-9.303164).build();
		CamJoinResponse response1 = frontend.camJoin(request1);

		CamInfoRequest request2 = CamInfoRequest.newBuilder().setName("Tagus").build();
		CamInfoResponse response2 = frontend.camInfo(request2);
		System.out.println(response2.getLatitude());
		System.out.println(response2.getLongitude());

		frontend.get_channel().shutdownNow();


	}

}
