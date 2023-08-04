package pt.tecnico.sauron.silo.domain;

public abstract class Type {
    private String _identifier;
    private String _typeName;

    public Type(String id, String typeName){
        _identifier = id;
        _typeName = typeName;
    }

    public String get_identifier() {
        return _identifier;
    }

    public String get_typeName() {
        return _typeName;
    }
}
