package pt.tecnico.sauron.silo.domain;

public class CameraDoesNotExistException extends Exception {

    public CameraDoesNotExistException(String errorMessage) {
        super(errorMessage);
    }
}
