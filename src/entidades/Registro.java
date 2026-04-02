package entidades;

import java.io.IOException;

public interface Registro {

    int getId();

    void setId(int id);

    byte[] toByteArray() throws IOException;

    void fromByteArray(byte[] ba) throws IOException;
}
