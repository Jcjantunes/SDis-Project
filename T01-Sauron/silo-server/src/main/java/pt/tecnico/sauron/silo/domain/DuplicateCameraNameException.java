package pt.tecnico.sauron.silo.domain;

public class DuplicateCameraNameException  extends Exception {
    public DuplicateCameraNameException(String errorMessage) {
        super(errorMessage);
    }
}
