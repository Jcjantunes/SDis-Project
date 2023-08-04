package pt.tecnico.sauron.silo.domain;

public class IncorrectArgumentException extends Exception {


    public IncorrectArgumentException(String errorMessage) {
            super(errorMessage);
    }

}
