package entidades.pessoas;
import aed3.InterfaceHashExtensivel;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ParCpfId implements InterfaceHashExtensivel {
    
    private String cpf;  // chave
    private int id;      // valor
    private final short TAMANHO = 15;  // tamanho em bytes

    public ParCpfId() throws Exception {
        this.cpf = "00000000000";
        this.id = -1;
    }

    public ParCpfId(String cpf, int id) throws Exception {
        if(!cpf.matches("\\d{11}")) 
            throw new Exception("CPF inválido!");
        this.cpf = cpf;
        this.id = id;
    }

    public String getCpf() {
        return cpf;
    }

    public int getId() {
        return id;
    }


    @Override
    public int hashCode() {
        return Math.abs(this.cpf.hashCode());
    }

    public short size() {
        return this.TAMANHO;
    }

    public String toString() {
        return "("+this.cpf + ";" + this.id+")";
    }

    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.write(cpf.getBytes());
        dos.writeInt(this.id);
        return baos.toByteArray();
    }

    public void fromByteArray(byte[] ba) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(ba);
        DataInputStream dis = new DataInputStream(bais);
        byte[] vb = new byte[11];
        dis.read(vb);
        this.cpf = new String(vb);
        this.id = dis.readInt();
    }

}
