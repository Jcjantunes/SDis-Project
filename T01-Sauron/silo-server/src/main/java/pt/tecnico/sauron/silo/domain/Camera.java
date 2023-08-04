package pt.tecnico.sauron.silo.domain;

public class Camera {
    private String _cameraName;
    private Coordinates _coordinates;

    public Camera(String cameraName, double latitude, double longitude) {
        _cameraName = cameraName;
        _coordinates = new Coordinates(latitude,longitude);
    }

    public String getCameraName() {
        return _cameraName;
    }

    public Coordinates get_coordinates() {
        return _coordinates;
    }
}
