package pt.tecnico.sauron.silo.domain;


import com.google.common.base.CharMatcher;
import com.google.protobuf.Timestamp;
import io.grpc.netty.shaded.io.netty.util.internal.ConcurrentSet;
import pt.tecnico.sauron.silo.grpc.Silo.*;

import java.util.*;
import java.util.concurrent.*;


public class SiloServer {

    private ConcurrentHashMap<String,Camera> synchronizedCameraHashMap = new ConcurrentHashMap<>();
    private ArrayList<Observation> observationArrayList = new ArrayList<>();
    private List<Observation> synchronizedObservationArrayList = Collections.synchronizedList(observationArrayList);

    public void clear(){
        synchronizedCameraHashMap.clear();
        synchronizedObservationArrayList.clear();
    }



    public void cam_join(String cameraName, double latitude, double longitude) throws IncorrectArgumentException, DuplicateCameraNameException {

        if(!(CharMatcher.ascii().matchesAllOf(cameraName))) {
            throw new IncorrectArgumentException("The camera name must have all ascii characters");
        }

        if(cameraName.length() < 3 || cameraName.length() > 15) {
            throw new IncorrectArgumentException("The camera name must be between 3 and 15 characters long");
        }

        if(synchronizedCameraHashMap.containsKey(cameraName) && (synchronizedCameraHashMap.get(cameraName).get_coordinates().get_latitude() != latitude ||
                synchronizedCameraHashMap.get(cameraName).get_coordinates().get_longitude() != longitude)) {
            throw new DuplicateCameraNameException("Duplicate camera name");
        }

        Camera camera = new Camera(cameraName, latitude, longitude);
        synchronizedCameraHashMap.put(cameraName,camera);
    }


    public Coordinates cam_info(String cameraName) throws IncorrectArgumentException, CameraDoesNotExistException {

        if (cameraName == null || cameraName.isBlank()) {
            throw new IncorrectArgumentException("Camera name can not be empty.");
        }

        if(!(synchronizedCameraHashMap.containsKey(cameraName))) {
            throw new CameraDoesNotExistException("Camera does not exist");
        }

        Camera camera = synchronizedCameraHashMap.get(cameraName);

        return camera.get_coordinates();
    }

    public void report(List<ObservationMessage> observationList , Timestamp currentTime) throws IncorrectArgumentException, CameraDoesNotExistException {


        for(ObservationMessage observationElement: observationList) {


            if(!(synchronizedCameraHashMap.containsKey(observationElement.getCameraName()))) {
                throw new CameraDoesNotExistException("Camera does not exist");
            }

            if(observationElement.getTipo().equals("person")){

                long personId = Long.parseLong(observationElement.getIdentifier());

                if((personId < 1 || personId > Math.pow(2,63)) && (personId == (long)personId)) {
                    throw new IncorrectArgumentException("Identifier must be between 1 and 63 bits long");
                }

                else {
                    Observation observation = new Observation(observationElement.getCameraName(),observationElement.getLatitude(),observationElement.getLongitude(), observationElement.getTipo(),observationElement.getIdentifier(), currentTime);
                    synchronizedObservationArrayList.add(observation);
                }
            }
            if(observationElement.getTipo().equals("car")) {

                String carId = observationElement.getIdentifier();

                if(!(carId.matches("\\d{2}[A-Z][A-Z][A-Z][A-Z]") || carId.matches("[A-Z][A-Z]\\d{2}[A-Z][A-Z]") || carId.matches("[A-Z][A-Z][A-Z][A-Z]\\d{2}")
                        ||carId.matches("[A-Z][A-Z]\\d{2}\\d{2}") || carId.matches("\\d{2}[A-Z][A-Z]\\d{2}") || carId.matches("\\d{2}\\d{2}[A-Z][A-Z]"))) {
                    throw new IncorrectArgumentException("Identifier must be in portuguese car plate format");
                }
                else{
                    Observation observation = new Observation(observationElement.getCameraName(),observationElement.getLatitude(),observationElement.getLongitude(),observationElement.getTipo(),carId, currentTime);
                    synchronizedObservationArrayList.add(observation);

                }

            }

        }

        /*System.out.println(synchronizedObservationArrayList.size());
        for(Observation obs: synchronizedObservationArrayList) {
            Date date = new Date(obs.get_observationDate().getSeconds()*1000);
            SimpleDateFormat sdf = sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            String formattedDate = sdf.format(date);

            System.out.printf("cameraName: %s, tipo: %s, identifier: %s, observationDate: %s\n", obs.get_cameraName(), obs.get_type().get_typeName(), obs.get_type().get_identifier() ,formattedDate);
        }
        System.out.println("fim report");*/


    }

    public Observation track(String tipo, String identifier) {

        Collections.sort(synchronizedObservationArrayList, new ObservationCompare());

        Observation observation = null;

        for(Observation obs: synchronizedObservationArrayList) {
            if(obs.get_type().get_identifier().equals(identifier) && obs.get_type().get_typeName().equals(tipo)) {
                observation = obs;
                break;
            }
        }

        /*System.out.println(synchronizedObservationArrayList.size());
        for(Observation obs: synchronizedObservationArrayList) {
            Date date = new Date(obs.get_observationDate().getSeconds()*1000);
            SimpleDateFormat sdf = sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            String formattedDate = sdf.format(date);

            System.out.printf("cameraName: %s, tipo: %s, identifier: %s, observationDate: %s\n", obs.get_cameraName(), obs.get_type().get_typeName(), obs.get_type().get_identifier() ,formattedDate);
        }*/

        return observation;
    }

    public List<Observation> trackMatch(String type, String partialIdentifier) {
        List<Observation> obsList = new ArrayList<>();

        List<Observation> synchronizedObsList = Collections.synchronizedList(obsList);

        Collections.sort(synchronizedObservationArrayList, new ObservationCompare());

        ConcurrentHashMap<String,Observation> obsHashMap= new ConcurrentHashMap<>();

        int len = partialIdentifier.length();

        if(partialIdentifier.charAt(0) == ('*')) {
            String idEnd = partialIdentifier.substring(1, len);

            //System.out.printf("partialIdentifier: %s\n", partialIdentifier);
            //System.out.printf("idEnd: %s\n", idEnd);

            int firstOccurrenceLen;
            for(Observation obs: synchronizedObservationArrayList) {
                if(obs.get_type().get_typeName().equals(type)) {
                    String fullId = obs.get_type().get_identifier();

                    //System.out.printf("fullId: %s\n", fullId);

                    firstOccurrenceLen = fullId.lastIndexOf(idEnd);

                    //System.out.printf("first occurence:%d\n", firstOccurrenceLen);

                    if (firstOccurrenceLen == fullId.length() - idEnd.length()) {
                        if (!obsHashMap.containsKey(fullId)) {
                            obsHashMap.put(fullId, obs);
                        }
                    }
                }
            }
        }

        else if(partialIdentifier.charAt(len-1) == ('*')) {
            String idBeginning = partialIdentifier.substring(0, len-1);

            int firstOccurrenceLen;

            for(Observation obs: synchronizedObservationArrayList) {
                if(obs.get_type().get_typeName().equals(type)) {
                    String fullId = obs.get_type().get_identifier();

                    firstOccurrenceLen = fullId.indexOf(idBeginning);

                    if (firstOccurrenceLen == 0) {
                        if (!obsHashMap.containsKey(fullId)) {
                            obsHashMap.put(fullId, obs);
                        }
                    }
                }
            }
        }

        else {

            //System.out.printf("partialIdentifier: %s\n", partialIdentifier);

            String[] inputValues = partialIdentifier.split("\\*");
            String idBeginning = inputValues[0];
            String idEnd = inputValues[1];

            //System.out.printf("idBeginning: %s, idEnd: %s\n", idBeginning, idEnd);

            int firstOccurrenceLen1;
            int firstOccurrenceLen2;



            for(Observation obs: synchronizedObservationArrayList) {

                if(obs.get_type().get_typeName().equals(type)) {

                    String fullId = obs.get_type().get_identifier();

                    //System.out.printf("fullId: %s\n", fullId);

                    firstOccurrenceLen1 = fullId.indexOf(idBeginning);
                    //System.out.printf("first occurence:%d\n", firstOccurrenceLen1);
                    if (firstOccurrenceLen1 == 0) {

                        //System.out.printf("secondHalf: %s\n",secondHalf);

                        firstOccurrenceLen2 = fullId.lastIndexOf(idEnd);
                        //System.out.printf("first occurence2:%d\n", firstOccurrenceLen2);

                        if (firstOccurrenceLen2 == fullId.length() - idEnd.length()) {

                            if (!obsHashMap.containsKey(fullId)) {
                                obsHashMap.put(fullId, obs);
                            }
                        }
                    }
                }
            }
        }

        for (Map.Entry<String, Observation> entry : obsHashMap.entrySet()) {
            synchronizedObsList.add(entry.getValue());
        }

        return synchronizedObsList;
    }

    public List<Observation> trace(String type, String id) {

        List<Observation> obsList = new ArrayList<>();

        List<Observation> synchronizedObsList = Collections.synchronizedList(obsList);

        for(Observation obs: synchronizedObservationArrayList) {
            if(obs.get_type().get_identifier().equals(id) && obs.get_type().get_typeName().equals(type)) {
                synchronizedObsList.add(obs);
            }
        }

        Collections.sort(synchronizedObsList, new ObservationCompare());

        /*for(Observation obs: obsList) {
            Date date = new Date(obs.get_observationDate().getSeconds()*1000);
            SimpleDateFormat sdf = sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            String formattedDate = sdf.format(date);

            System.out.printf("cameraName: %s, tipo: %s, identifier: %s, observationDate: %s\n", obs.get_cameraName(), obs.get_type().get_typeName(), obs.get_type().get_identifier() ,formattedDate);
        }*/

        return synchronizedObsList;
    }
}
